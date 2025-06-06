package com.SpringTest.SpringTest.service.impl;

import com.SpringTest.SpringTest.entity.ChucVu;
import com.SpringTest.SpringTest.entity.NhanVien;
import com.SpringTest.SpringTest.exception.BadRequestException;
import com.SpringTest.SpringTest.exception.ResourceNotFoundException;
import com.SpringTest.SpringTest.repository.ChucVuRepository;
import com.SpringTest.SpringTest.repository.NhanVienRepository;
import com.SpringTest.SpringTest.service.NhanVienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class NhanVienServiceImpl implements NhanVienService {

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private ChucVuRepository chucVuRepository;
    // Nếu nhân viên có tài khoản đăng nhập hệ thống, có thể cần TaiKhoanRepository
    // @Autowired private TaiKhoanRepository taiKhoanRepository;

    @Override
    public Page<NhanVien> getAllNhanVien(Pageable pageable) {
        return nhanVienRepository.findAll(pageable);
    }

    @Override
    public List<NhanVien> getAllNhanVienList() {
        return nhanVienRepository.findAll();
    }

    @Override
    public NhanVien getNhanVienById(String maNV) {
        return nhanVienRepository.findById(maNV)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên với mã: " + maNV));
    }

    @Override
    @Transactional // Quan trọng
    public NhanVien saveNhanVien(NhanVien nhanVien) { // Trong thực tế, nên nhận NhanVienFormDTO
        if (nhanVienRepository.existsById(nhanVien.getMaNV())) {
            throw new BadRequestException("Mã nhân viên " + nhanVien.getMaNV() + " đã tồn tại.");
        }
        // Validate và lấy ChucVu Entity
        if (nhanVien.getChucVu() == null || nhanVien.getChucVu().getMaChucVu() == null) {
            throw new BadRequestException("Chức vụ không được để trống.");
        }
        ChucVu chucVu = chucVuRepository.findById(nhanVien.getChucVu().getMaChucVu())
                .orElseThrow(() -> new ResourceNotFoundException("Chức vụ với mã " + nhanVien.getChucVu().getMaChucVu() + " không tồn tại."));
        nhanVien.setChucVu(chucVu);

        // Xử lý nếu nhân viên có tài khoản hệ thống (tạo TaiKhoan entity)
        // if (nhanVienFormDTO.getTenTK() != null) { ... }

        return nhanVienRepository.save(nhanVien);
    }

    @Override
    @Transactional // Quan trọng
    public NhanVien updateNhanVien(String maNV, NhanVien nhanVienDetails) { // Tương tự, nên nhận DTO
        NhanVien existingNhanVien = getNhanVienById(maNV);

        existingNhanVien.setHoTen(nhanVienDetails.getHoTen());
        existingNhanVien.setSoDienThoai(nhanVienDetails.getSoDienThoai());
        existingNhanVien.setGioiTinh(nhanVienDetails.getGioiTinh());
        existingNhanVien.setNgaySinh(nhanVienDetails.getNgaySinh());

        if (nhanVienDetails.getChucVu() != null && nhanVienDetails.getChucVu().getMaChucVu() != null) {
            ChucVu chucVu = chucVuRepository.findById(nhanVienDetails.getChucVu().getMaChucVu())
                    .orElseThrow(() -> new ResourceNotFoundException("Chức vụ với mã " + nhanVienDetails.getChucVu().getMaChucVu() + " không tồn tại."));
            existingNhanVien.setChucVu(chucVu);
        } else {
            throw new BadRequestException("Chức vụ không được để trống khi cập nhật.");
        }
        // Xử lý cập nhật tài khoản hệ thống của nhân viên nếu có

        return nhanVienRepository.save(existingNhanVien);
    }

    @Override
    @Transactional // Quan trọng
    public void deleteNhanVien(String maNV) {
        NhanVien nhanVien = getNhanVienById(maNV);
        // Kiểm tra các ràng buộc trước khi xóa, ví dụ: nhân viên có đang trong hóa đơn nào không,
        // hoặc có tài khoản đang hoạt động không.
        // if (hoaDonDVRepository.existsByNhanVien(nhanVien)) {
        //    throw new BadRequestException("Không thể xóa nhân viên vì đã có trong hóa đơn.");
        // }
        nhanVienRepository.delete(nhanVien);
    }
    public BigDecimal getEmployeeShiftSalary(String maNV, String maCaLamViec) {
        return nhanVienRepository.calculateShiftSalary(maNV, maCaLamViec); // Gọi phương thức từ repository
    }
}