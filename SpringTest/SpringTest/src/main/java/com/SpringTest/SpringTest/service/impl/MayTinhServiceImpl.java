package com.SpringTest.SpringTest.service.impl;

import com.SpringTest.SpringTest.entity.LoaiMay;
import com.SpringTest.SpringTest.entity.MayTinh;
import com.SpringTest.SpringTest.exception.BadRequestException;
import com.SpringTest.SpringTest.exception.ResourceNotFoundException;
import com.SpringTest.SpringTest.repository.LoaiMayRepository;
import com.SpringTest.SpringTest.repository.MayTinhRepository;
import com.SpringTest.SpringTest.repository.PhienSuDungRepository;
import com.SpringTest.SpringTest.service.MayTinhService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager; // Thêm import này
import jakarta.persistence.ParameterMode; // Thêm import này
import jakarta.persistence.PersistenceContext; // Thêm import này
import jakarta.persistence.StoredProcedureQuery; // Thêm import này
import java.util.Arrays;
import java.util.List;

@Service
public class MayTinhServiceImpl implements MayTinhService {

    @Autowired
    private MayTinhRepository mayTinhRepository;
    @Autowired
    private PhienSuDungRepository phienSuDungRepository;
    @Autowired
    private LoaiMayRepository loaiMayRepository;

    @PersistenceContext // Injecct EntityManager
    private EntityManager entityManager;
    private static final List<String> VALID_STATUSES = Arrays.asList("Khả dụng", "Bảo trì", "Đang sử dụng");

    public String getActualMachineStatusFromDb(String maMay) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("KiemTraTrangThaiMay");
        query.registerStoredProcedureParameter("p_MaMay", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_TrangThai", String.class, ParameterMode.OUT); // Tham số OUT

        query.setParameter("p_MaMay", maMay);
        query.execute();

        return (String) query.getOutputParameterValue("p_TrangThai");
    }
    @Override
    public List<MayTinh> getAllMayTinh() { //
        return mayTinhRepository.findAll(); //
    }

    @Override
    public MayTinh getMayTinhById(String maMay) { //
        return mayTinhRepository.findById(maMay) //
                .orElseThrow(() -> new ResourceNotFoundException("Máy tính không tìm thấy: " + maMay)); //
    }

    @Override
    @Transactional
    public MayTinh updateTrangThaiMay(String maMay, String trangThaiMoi) { //
        if (!VALID_STATUSES.contains(trangThaiMoi)) { //
            throw new BadRequestException("Trạng thái không hợp lệ: " + trangThaiMoi + //
                    ". Các trạng thái hợp lệ: " + VALID_STATUSES); //
        }

        MayTinh mayTinh = getMayTinhById(maMay); //

        // Lấy trạng thái thực tế từ DB (bao gồm cả việc check phiên sử dụng)
        // String actualStatusFromDb = getActualMachineStatusFromDb(maMay); // Gọi procedure

        // Logic kiểm tra hiện tại của bạn đã khá tốt và trực tiếp sử dụng JPA repository.
        // Việc dùng procedure KiemTraTrangThaiMay ở đây có thể là một lựa chọn nếu logic
        // trong procedure phức tạp hơn hoặc bạn muốn tập trung logic kiểm tra trạng thái ở DB.
        // Ví dụ nếu dùng procedure:
        // if ("Đang sử dụng".equals(actualStatusFromDb)) {
        //    if ("Khả dụng".equalsIgnoreCase(trangThaiMoi) || "Bảo trì".equalsIgnoreCase(trangThaiMoi)) {
        //        throw new BadRequestException("Máy " + maMay + " đang được sử dụng (theo DB), không thể đặt thành '" + trangThaiMoi + "'.");
        //    }
        // } else if (!"Đang sử dụng".equals(actualStatusFromDb) && "Đang sử dụng".equalsIgnoreCase(trangThaiMoi)) {
        //     // Logic này trong procedure KiemTraTrangThaiMay đã set là "Đang sử dụng" nếu có phiên.
        //     // Việc cố gắng set thủ công sang "Đang sử dụng" khi không có phiên cần được xử lý cẩn thận.
        //     // Logic hiện tại của bạn:
        //     if (!phienSuDungRepository.findByMayTinhAndThoiGianKetThucIsNull(mayTinh).isPresent()){
        //         throw new BadRequestException("Không thể đặt máy " + maMay + " thành 'Đang sử dụng' thủ công. Trạng thái này được cập nhật tự động khi đăng nhập.");
        //     }
        // }


        // Giữ lại logic kiểm tra hiện tại của bạn vì nó rõ ràng và sử dụng JPA trực tiếp:
        if ("Khả dụng".equalsIgnoreCase(trangThaiMoi) && //
                phienSuDungRepository.findByMayTinhAndThoiGianKetThucIsNull(mayTinh).isPresent()) { //
            throw new BadRequestException("Không thể đặt máy " + maMay + " thành 'Khả dụng' khi đang có phiên sử dụng."); //
        }

        if ("Bảo trì".equalsIgnoreCase(trangThaiMoi) && //
                phienSuDungRepository.findByMayTinhAndThoiGianKetThucIsNull(mayTinh).isPresent()) { //
            throw new BadRequestException("Không thể đặt máy " + maMay + " thành 'Bảo trì' khi đang có phiên sử dụng."); //
        }

        if ("Đang sử dụng".equalsIgnoreCase(trangThaiMoi) && //
                !phienSuDungRepository.findByMayTinhAndThoiGianKetThucIsNull(mayTinh).isPresent()) { //
            throw new BadRequestException("Không thể đặt máy " + maMay + " thành 'Đang sử dụng' thủ công. Trạng thái này được cập nhật tự động khi đăng nhập."); //
        }


        mayTinh.setTrangThai(trangThaiMoi); //
        return mayTinhRepository.save(mayTinh); //
    }

    @Override
    @Transactional
    public MayTinh addMayTinh(MayTinh mayTinh) { // DTO sẽ tốt hơn
        if(mayTinhRepository.existsById(mayTinh.getMaMay())){
            throw new BadRequestException("Mã máy " + mayTinh.getMaMay() + " đã tồn tại.");
        }
        // Kiểm tra LoaiMay có tồn tại không
        LoaiMay loaiMay = loaiMayRepository.findById(mayTinh.getLoaiMay().getMaLoaiMay())
                .orElseThrow(() -> new ResourceNotFoundException("Loại máy " + mayTinh.getLoaiMay().getMaLoaiMay() + " không tồn tại."));
        mayTinh.setLoaiMay(loaiMay); // Đảm bảo object LoaiMay được managed
        if (mayTinh.getTrangThai() == null || mayTinh.getTrangThai().isEmpty()) {
            mayTinh.setTrangThai("Khả dụng"); // Mặc định
        }
        return mayTinhRepository.save(mayTinh);
    }

    @Override
    @Transactional
    public void deleteMayTinh(String maMay) {
        MayTinh mayTinh = getMayTinhById(maMay);
        // Kiểm tra ràng buộc, ví dụ: không xóa máy đang có phiên hoạt động
        if (phienSuDungRepository.findByMayTinhAndThoiGianKetThucIsNull(mayTinh).isPresent()) {
            throw new BadRequestException("Không thể xóa máy " + maMay + " khi đang có phiên sử dụng.");
        }
        mayTinhRepository.delete(mayTinh);
    }
    @Override
    @Transactional
    public MayTinh updateMayTinh(String maMay, MayTinh mayTinhDetails) { // DTO sẽ tốt hơn
        MayTinh existingMayTinh = getMayTinhById(maMay);
        existingMayTinh.setTenMay(mayTinhDetails.getTenMay());
        // Cập nhật LoaiMay
        if (mayTinhDetails.getLoaiMay() != null && mayTinhDetails.getLoaiMay().getMaLoaiMay() != null) {
            LoaiMay loaiMay = loaiMayRepository.findById(mayTinhDetails.getLoaiMay().getMaLoaiMay())
                    .orElseThrow(() -> new ResourceNotFoundException("Loại máy " + mayTinhDetails.getLoaiMay().getMaLoaiMay() + " không tồn tại."));
            existingMayTinh.setLoaiMay(loaiMay);
        }
        // Cẩn thận khi cập nhật trạng thái ở đây, nên có API riêng như updateTrangThaiMay
        // existingMayTinh.setTrangThai(mayTinhDetails.getTrangThai());
        return mayTinhRepository.save(existingMayTinh);
    }
}