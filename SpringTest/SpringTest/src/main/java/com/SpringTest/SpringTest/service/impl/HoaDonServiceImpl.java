package com.SpringTest.SpringTest.service.impl;

import com.SpringTest.SpringTest.dto.HoaDonDTO;
import com.SpringTest.SpringTest.dto.request.CreateHoaDonRequest;
import com.SpringTest.SpringTest.dto.request.OrderServiceRequest;
import com.SpringTest.SpringTest.entity.*;
import com.SpringTest.SpringTest.exception.BadRequestException;
import com.SpringTest.SpringTest.exception.ResourceNotFoundException;
import com.SpringTest.SpringTest.repository.*;
import com.SpringTest.SpringTest.service.HoaDonService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HoaDonServiceImpl implements HoaDonService {

    @Autowired
    private HoaDonDVRepository hoaDonDVRepository;
    @Autowired
    private ChiTietHoaDonDVRepository chiTietHoaDonDVRepository;
    @Autowired
    private TaiKhoanRepository taiKhoanRepository;
    @Autowired
    private NhanVienRepository nhanVienRepository;
    @Autowired
    private DichVuRepository dichVuRepository;
    @Autowired
    private UuDaiRepository uuDaiRepository;

    public BigDecimal tinhTongTienDichVuCuaHoaDon(Integer maHoaDon) {
        return hoaDonDVRepository.getTongTienDichVu(maHoaDon);
    }

    public BigDecimal getServiceBillTotalCost(String maHD, boolean applyDiscount) {
        // Kiểm tra xem hóa đơn có tồn tại không nếu cần
        // HoaDonDV hoaDon = hoaDonDVRepository.findById(maHD)
        // .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn " + maHD + " không tìm thấy."));

        return hoaDonDVRepository.calculateServiceBillCost(maHD, applyDiscount);
    }

    @Override
    @Transactional
    public HoaDonDTO createHoaDon(CreateHoaDonRequest request) {
        HoaDonDV hoaDonDV = new HoaDonDV();
        hoaDonDV.setMaHD("HD-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        hoaDonDV.setThoiDiemThanhToan(LocalDateTime.now());

        TaiKhoan taiKhoan = null;
        if (request.getMaTK() != null && !request.getMaTK().isEmpty()) {
            taiKhoan = taiKhoanRepository.findById(request.getMaTK())
                    .orElseThrow(() -> new ResourceNotFoundException("Tài khoản " + request.getMaTK() + " không tồn tại."));
            hoaDonDV.setTaiKhoan(taiKhoan);
        }

        NhanVien nhanVien = nhanVienRepository.findById(request.getMaNV())
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên " + request.getMaNV() + " không tồn tại."));
        hoaDonDV.setNhanVien(nhanVien);

        UuDai uuDai = null;
        if (request.getMaUuDai() != null && !request.getMaUuDai().isEmpty()) {
            uuDai = uuDaiRepository.findById(request.getMaUuDai())
                    .orElseThrow(() -> new ResourceNotFoundException("Ưu đãi " + request.getMaUuDai() + " không tồn tại."));
            hoaDonDV.setUuDai(uuDai);
        }

        HoaDonDV savedHoaDon = hoaDonDVRepository.save(hoaDonDV); // Lưu hóa đơn trước để có MaHD

        BigDecimal tongTienTruocGiam = BigDecimal.ZERO;
        List<ChiTietHoaDonDV> chiTietList = new ArrayList<>();

        for (OrderServiceRequest.OrderItemRequest item : request.getItems()) {
            DichVu dichVu = dichVuRepository.findById(item.getMaDV())
                    .orElseThrow(() -> new ResourceNotFoundException("Dịch vụ " + item.getMaDV() + " không tồn tại."));

            // Kiểm tra trạng thái dịch vụ hoặc số lượng tồn nếu cần
            if (!"Còn hàng".equalsIgnoreCase(dichVu.getTrangThaiDichVu())) {
                throw new BadRequestException("Dịch vụ " + dichVu.getTenDV() + " hiện không khả dụng.");
            }

            ChiTietHoaDonDVId ctId = new ChiTietHoaDonDVId(savedHoaDon.getMaHD(), dichVu.getMaDV());
            ChiTietHoaDonDV chiTiet = new ChiTietHoaDonDV();
            chiTiet.setId(ctId);
            chiTiet.setHoaDonDV(savedHoaDon);
            chiTiet.setDichVu(dichVu);
            chiTiet.setSoLuong(item.getSoLuong());
            // Cần thêm cột DonGiaLucBan và ThanhTien vào Entity CT_HoaDonDV
            // chiTiet.setDonGiaLucBan(dichVu.getDonGia());
            // chiTiet.setThanhTien(dichVu.getDonGia().multiply(BigDecimal.valueOf(item.getSoLuong())));

            chiTietList.add(chiTiet);
            tongTienTruocGiam = tongTienTruocGiam.add(dichVu.getDonGia().multiply(BigDecimal.valueOf(item.getSoLuong())));
        }
        chiTietHoaDonDVRepository.saveAll(chiTietList);

        BigDecimal soTienGiam = BigDecimal.ZERO;
        if (uuDai != null && uuDai.getMucUuDai() != null) {
            soTienGiam = tongTienTruocGiam.multiply(uuDai.getMucUuDai().divide(BigDecimal.valueOf(100)))
                    .setScale(0, RoundingMode.HALF_UP); // Làm tròn
        }
        BigDecimal tongTienSauGiam = tongTienTruocGiam.subtract(soTienGiam);

        // Nếu thanh toán bằng tài khoản, trừ tiền
        if (taiKhoan != null) {
            if (taiKhoan.getSoTienConLai().compareTo(tongTienSauGiam) < 0) {
                throw new BadRequestException("Số dư tài khoản " + taiKhoan.getTenTK() + " không đủ. Cần " + tongTienSauGiam);
            }
            taiKhoan.setSoTienConLai(taiKhoan.getSoTienConLai().subtract(tongTienSauGiam));
            taiKhoanRepository.save(taiKhoan);
        }
        // Nếu không có tài khoản, đây là thanh toán tiền mặt, không cần trừ.

        // Cập nhật HoaDonDV với tổng tiền (nếu có các cột này trong entity HoaDonDV)
        // savedHoaDon.setTongTienTruocGiam(tongTienTruocGiam);
        // savedHoaDon.setSoTienGiam(soTienGiam);
        // savedHoaDon.setTongTienHD(tongTienSauGiam); // Cột này đã có trong DDL là MaHD
        // hoaDonDVRepository.save(savedHoaDon); // Lưu lại

        return mapToHoaDonDTO(savedHoaDon, tongTienTruocGiam, soTienGiam, tongTienSauGiam);
    }

    @Override
    public HoaDonDTO getHoaDonById(String maHD) {
        HoaDonDV hoaDonDV = hoaDonDVRepository.findById(maHD)
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn " + maHD + " không tìm thấy."));
        return mapToHoaDonDTO(hoaDonDV, null, null, null); // Cần tính lại tổng tiền nếu không lưu
    }

    @Override
    public List<HoaDonDTO> getHoaDonByMaTK(String maTK) {
        TaiKhoan taiKhoan = taiKhoanRepository.findById(maTK)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản " + maTK + " không tìm thấy."));
        return hoaDonDVRepository.findByTaiKhoan(taiKhoan).stream()
                .map(hd -> mapToHoaDonDTO(hd, null, null, null)) // Cần tính lại tổng tiền
                .collect(Collectors.toList());
    }

    @Override
    public List<HoaDonDTO> getHoaDonByMaNV(String maNV) {
        NhanVien nhanVien = nhanVienRepository.findById(maNV)
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên " + maNV + " không tìm thấy."));
        return hoaDonDVRepository.findByNhanVien(nhanVien).stream()
                .map(hd -> mapToHoaDonDTO(hd, null, null, null)) // Cần tính lại tổng tiền
                .collect(Collectors.toList());
    }


    // Helper method to map Entity to DTO
    private HoaDonDTO mapToHoaDonDTO(HoaDonDV hoaDonDV, BigDecimal ttTruocGiam, BigDecimal tienGiam, BigDecimal ttSauGiam) {
        HoaDonDTO dto = new HoaDonDTO();
        BeanUtils.copyProperties(hoaDonDV, dto); // Copy các trường khớp tên

        if (hoaDonDV.getTaiKhoan() != null) {
            dto.setMaTK(hoaDonDV.getTaiKhoan().getMaTK());
            dto.setTenTK(hoaDonDV.getTaiKhoan().getTenTK());
        }
        if (hoaDonDV.getNhanVien() != null) {
            dto.setMaNV(hoaDonDV.getNhanVien().getMaNV());
            dto.setTenNV(hoaDonDV.getNhanVien().getHoTen());
        }
        if (hoaDonDV.getUuDai() != null) {
            dto.setMaUuDai(hoaDonDV.getUuDai().getMaUuDai());
        }

        List<HoaDonDTO.ChiTietHoaDonDTO> chiTietDTOs = new ArrayList<>();
        BigDecimal calculatedTongTienTruocGiam = BigDecimal.ZERO;

        List<ChiTietHoaDonDV> chiTietList = chiTietHoaDonDVRepository.findByHoaDonDV(hoaDonDV);

        for (ChiTietHoaDonDV ct : chiTietList) {
            HoaDonDTO.ChiTietHoaDonDTO ctDto = new HoaDonDTO.ChiTietHoaDonDTO();
            ctDto.setMaDV(ct.getDichVu().getMaDV());
            ctDto.setTenDV(ct.getDichVu().getTenDV());
            ctDto.setSoLuong(ct.getSoLuong());
            // Giả sử CT_HoaDonDV không có DonGiaLucBan và ThanhTien, ta lấy từ DichVu
            BigDecimal donGiaLucBan = ct.getDichVu().getDonGia(); // Nên lấy từ CT_HoaDonDV nếu có
            ctDto.setDonGiaLucBan(donGiaLucBan);
            BigDecimal thanhTien = donGiaLucBan.multiply(BigDecimal.valueOf(ct.getSoLuong()));
            ctDto.setThanhTien(thanhTien);
            chiTietDTOs.add(ctDto);
            calculatedTongTienTruocGiam = calculatedTongTienTruocGiam.add(thanhTien);
        }
        dto.setChiTiet(chiTietDTOs);

        // Nếu tổng tiền không được truyền vào (khi get), thì tính lại
        if (ttTruocGiam == null) {
            dto.setTongTienTruocGiam(calculatedTongTienTruocGiam);
            BigDecimal calculatedSoTienGiam = BigDecimal.ZERO;
            if (hoaDonDV.getUuDai() != null && hoaDonDV.getUuDai().getMucUuDai() != null) {
                calculatedSoTienGiam = calculatedTongTienTruocGiam
                        .multiply(hoaDonDV.getUuDai().getMucUuDai().divide(BigDecimal.valueOf(100)))
                        .setScale(0, RoundingMode.HALF_UP);
            }
            dto.setSoTienGiam(calculatedSoTienGiam);
            dto.setTongTienSauGiam(calculatedTongTienTruocGiam.subtract(calculatedSoTienGiam));
        } else {
            dto.setTongTienTruocGiam(ttTruocGiam);
            dto.setSoTienGiam(tienGiam);
            dto.setTongTienSauGiam(ttSauGiam);
        }

        return dto;
    }

    @Override
    @Transactional
    public HoaDonDV createHoaDonDV(CreateHoaDonRequest request) {
        // 1. Lấy các đối tượng liên quan
        TaiKhoan taiKhoan = taiKhoanRepository.findById(request.getMaTK())
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản không tồn tại"));
        NhanVien nhanVien = nhanVienRepository.findById(request.getMaNV())
                .orElseThrow(() -> new ResourceNotFoundException("Nhân viên không tồn tại"));

        // 2. Tạo hóa đơn chính
        HoaDonDV hoaDonDV = new HoaDonDV();
        hoaDonDV.setTaiKhoan(taiKhoan);
        hoaDonDV.setNhanVien(nhanVien);
        hoaDonDV.setNgayLap(LocalDateTime.now());
        // Giả sử chưa thanh toán
        // hoaDonDV.setTrangThaiThanhToan(false);

        HoaDonDV savedHoaDon = hoaDonDVRepository.save(hoaDonDV);

        // 3. Tạo các chi tiết hóa đơn
        for (CreateHoaDonRequest.OrderItemDTO item : request.getItems()) {
            DichVu dichVu = dichVuRepository.findById(item.getMaDV())
                    .orElseThrow(() -> new ResourceNotFoundException("Dịch vụ " + item.getMaDV() + " không tồn tại"));

            ChiTietHoaDonDVId chiTietId = new ChiTietHoaDonDVId(savedHoaDon.getMaHD(), dichVu.getMaDV());
            ChiTietHoaDonDV chiTiet = new ChiTietHoaDonDV();
            chiTiet.setId(chiTietId);
            chiTiet.setHoaDonDV(savedHoaDon);
            chiTiet.setDichVu(dichVu);
            chiTiet.setSoLuong(item.getSoLuong());
            chiTiet.setDonGia(dichVu.getDonGia()); // Lấy giá từ DB để đảm bảo chính xác

            chiTietHoaDonDVRepository.save(chiTiet);
        }

        return savedHoaDon;
    }

    public BigDecimal calculateBillTotal(String maHD, boolean applyDiscount) {
        return hoaDonDVRepository.calculateServiceBillCost(maHD, applyDiscount);
    }
}