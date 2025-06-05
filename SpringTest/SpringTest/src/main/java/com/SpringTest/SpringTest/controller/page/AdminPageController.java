package com.SpringTest.SpringTest.controller.page;

import com.SpringTest.SpringTest.service.DichVuService;
import com.SpringTest.SpringTest.service.MayTinhService;
import com.SpringTest.SpringTest.service.PhienSuDungService;
import com.SpringTest.SpringTest.service.TaiKhoanService;
import com.SpringTest.SpringTest.dto.DichVuDTO;
import com.SpringTest.SpringTest.dto.request.CreateTaiKhoanRequest;
import com.SpringTest.SpringTest.dto.response.TaiKhoanInfoResponse;
import com.SpringTest.SpringTest.dto.request.NapTienRequest;
import com.SpringTest.SpringTest.entity.*;
import com.SpringTest.SpringTest.service.*;
import com.SpringTest.SpringTest.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid; // Cho validation
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/admin") // Tất cả các URL trong controller này sẽ bắt đầu bằng /admin
@PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')") // Bảo vệ toàn bộ controller này
public class AdminPageController {

    @Autowired
    private MayTinhService mayTinhService;

    @Autowired
    private DichVuService dichVuService;

    @Autowired
    private TaiKhoanService taiKhoanService; // Giả sử bạn có hàm getAllTaiKhoan() hoặc tương tự

    @Autowired
    private PhienSuDungService phienSuDungService;

    @Autowired
    private NhanVienService nhanVienService;

    @Autowired
    private ChucVuService chucVuService;

    @Autowired
    private LoaiMayService loaiMayService;

    @Autowired
    private LoaiKHService loaiKHService;

    @Autowired
    private UuDaiService uuDaiService;


    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model) {
        try {
            model.addAttribute("soLuongMayTinh", mayTinhService.getAllMayTinh().size());
            model.addAttribute("soLuongTaiKhoan", taiKhoanService.countAllActiveAccounts()); // Cần hàm này
            model.addAttribute("soLuongDichVu", dichVuService.getAllDichVu().size());
            model.addAttribute("soPhienDangHoatDong", phienSuDungService.getActiveSessions().size());
        } catch (Exception e) {
            // Xử lý lỗi nếu không lấy được dữ liệu, ví dụ:
            model.addAttribute("dashboardError", "Không thể tải dữ liệu tổng quan: " + e.getMessage());
        }
        return "admin/admin-dashboard";
    }

    // --- Quản Lý Máy Tính ---
    @GetMapping("/computers")
    public String showManageComputersPage(Model model, @PageableDefault(size = 10) Pageable pageable) {
        // Giả sử MayTinhService có phương thức hỗ trợ Pageable
        // Page<MayTinh> mayTinhPage = mayTinhService.getAllMayTinhPageable(pageable);
        // model.addAttribute("mayTinhPage", mayTinhPage);
        model.addAttribute("danhSachMayTinh", mayTinhService.getAllMayTinh()); // Dùng tạm nếu chưa có phân trang service
        return "admin/manage-computers";
    }

    // Form thêm máy tính (GET)
    @GetMapping("/computers/add")
    public String showAddComputerForm(Model model) {
        model.addAttribute("computerForm", new MayTinh()); // Hoặc MayTinhFormDTO
        model.addAttribute("danhSachLoaiMay", loaiMayService.getAllLoaiMay()); // Cần LoaiMayService
        model.addAttribute("isEditMode", false);
        return "admin/computer-form";
    }
    @PostMapping("/computers/save")
    public String saveComputer(@Valid @ModelAttribute("computerForm") MayTinh computerForm, // Thay MayTinh bằng MayTinhFormDTO
                               BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("danhSachLoaiMay", loaiMayService.getAllLoaiMay());
            model.addAttribute("isEditMode", false);
            return "admin/computer-form";
        }
        try {
            // Nếu dùng DTO, cần chuyển đổi sang Entity trước khi gọi service.addMayTinh
            // Ví dụ: MayTinh mayTinhEntity = convertToEntity(computerForm);
            // mayTinhService.addMayTinh(mayTinhEntity);
            mayTinhService.addMayTinh(computerForm); // Giả sử service nhận Entity hoặc DTO tương ứng
            redirectAttributes.addFlashAttribute("successMessage", "Thêm máy tính thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm máy tính: " + e.getMessage());
            model.addAttribute("danhSachLoaiMay", loaiMayService.getAllLoaiMay());
            model.addAttribute("isEditMode", false);
            model.addAttribute("computerForm", computerForm); // Giữ lại dữ liệu đã nhập
            return "admin/computer-form";
        }
        return "redirect:/admin/computers";
    }

    // Form sửa máy tính (GET)
    @GetMapping("/computers/edit/{maMay}")
    public String showEditComputerForm(@PathVariable String maMay, Model model) {
        try {
            MayTinh mayTinh = mayTinhService.getMayTinhById(maMay);
            // MayTinhFormDTO formDTO = convertToFormDTO(mayTinh); // Nếu dùng DTO
            model.addAttribute("computerForm", mayTinh); // Hoặc formDTO
            model.addAttribute("danhSachLoaiMay", loaiMayService.getAllLoaiMay());
            model.addAttribute("isEditMode", true);
        } catch (ResourceNotFoundException e) {
            model.addAttribute("errorMessage", "Không tìm thấy máy tính với mã: " + maMay);
            return "redirect:/admin/computers"; // Hoặc một trang lỗi riêng
        }
        return "admin/computer-form";
    }
    @PostMapping("/computers/update")
    public String updateComputer(@Valid @ModelAttribute("computerForm") MayTinh computerForm, // Thay MayTinh bằng MayTinhFormDTO
                                 BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("danhSachLoaiMay", loaiMayService.getAllLoaiMay());
            model.addAttribute("isEditMode", true);
            return "admin/computer-form";
        }
        try {
            // MayTinh mayTinhEntity = convertToEntity(computerForm);
            // mayTinhService.updateMayTinh(computerForm.getMaMay(), mayTinhEntity);
            mayTinhService.updateMayTinh(computerForm.getMaMay(), computerForm);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật máy tính thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật máy tính: " + e.getMessage());
            model.addAttribute("danhSachLoaiMay", loaiMayService.getAllLoaiMay());
            model.addAttribute("isEditMode", true);
            model.addAttribute("computerForm", computerForm);
            return "admin/computer-form";
        }
        return "redirect:/admin/computers";
    }

    // --- Quản Lý Dịch Vụ ---
    @GetMapping("/services")
    public String showManageServicesPage(Model model, @PageableDefault(size = 10) Pageable pageable) {
        // Page<DichVuDTO> dichVuPage = dichVuService.getAllDichVuPageable(pageable); // Cần service hỗ trợ
        // model.addAttribute("dichVuPage", dichVuPage);
        model.addAttribute("danhSachDichVu", dichVuService.getAllDichVu());
        return "admin/manage-services";
    }

    @GetMapping("/services/add")
    public String showAddServiceForm(Model model) {
        model.addAttribute("serviceForm", new DichVuDTO()); // DTO này đã có và phù hợp
        model.addAttribute("isEditMode", false);
        return "admin/service-form";
    }

    @PostMapping("/services/save")
    public String saveService(@Valid @ModelAttribute("serviceForm") DichVuDTO serviceForm,
                              BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEditMode", false);
            return "admin/service-form";
        }
        try {
            dichVuService.addDichVu(serviceForm);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm dịch vụ thành công!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi thêm dịch vụ: " + e.getMessage());
            model.addAttribute("isEditMode", false);
            model.addAttribute("serviceForm", serviceForm);
            return "admin/service-form";
        }
        return "redirect:/admin/services";
    }

    @GetMapping("/services/edit/{maDV}")
    public String showEditServiceForm(@PathVariable String maDV, Model model) {
        try {
            model.addAttribute("serviceForm", dichVuService.getDichVuByMaDV(maDV));
            model.addAttribute("isEditMode", true);
        } catch (ResourceNotFoundException e) {
            model.addAttribute("errorMessage", "Không tìm thấy dịch vụ: " + maDV);
            return "redirect:/admin/services";
        }
        return "admin/service-form";
    }

    @PostMapping("/services/update")
    public String updateService(@Valid @ModelAttribute("serviceForm") DichVuDTO serviceForm,
                                BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEditMode", true);
            return "admin/service-form";
        }
        try {
            dichVuService.updateDichVu(serviceForm.getMaDV(), serviceForm);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật dịch vụ thành công!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi cập nhật dịch vụ: " + e.getMessage());
            model.addAttribute("isEditMode", true);
            model.addAttribute("serviceForm", serviceForm);
            return "admin/service-form";
        }
        return "redirect:/admin/services";
    }


    // --- Quản Lý Tài Khoản Khách Hàng ---
    @GetMapping("/accounts")
    public String showManageAccountsPage(Model model, @PageableDefault(size = 10) Pageable pageable) {
        Page<TaiKhoanInfoResponse> taiKhoanPage = taiKhoanService.getAllKhachHangTaiKhoanPageable(pageable); // Cần hàm này
        model.addAttribute("taiKhoanPage", taiKhoanPage);
        return "admin/manage-accounts";
    }

    @GetMapping("/accounts/add")
    public String showAddAccountForm(Model model) {
        model.addAttribute("accountForm", new CreateTaiKhoanRequest()); // DTO này đã có
        model.addAttribute("danhSachLoaiKH", loaiKHService.getAllLoaiKH()); // Cần LoaiKHService
        model.addAttribute("isEditMode", false);
        return "admin/account-form";
    }

    @PostMapping("/accounts/save")
    public String saveAccount(@Valid @ModelAttribute("accountForm") CreateTaiKhoanRequest accountForm,
                              BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("danhSachLoaiKH", loaiKHService.getAllLoaiKH());
            model.addAttribute("isEditMode", false);
            return "admin/account-form";
        }
        try {
            taiKhoanService.createTaiKhoanKhachHang(accountForm);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo tài khoản khách hàng thành công!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi tạo tài khoản: " + e.getMessage());
            model.addAttribute("danhSachLoaiKH", loaiKHService.getAllLoaiKH());
            model.addAttribute("isEditMode", false);
            model.addAttribute("accountForm", accountForm);
            return "admin/account-form";
        }
        return "redirect:/admin/accounts";
    }

    // Sửa tài khoản khách hàng có thể bao gồm sửa thông tin KhachHang và Loại KH
    // Việc thay đổi mật khẩu hoặc các thông tin nhạy cảm khác cần cân nhắc kỹ
    @GetMapping("/accounts/edit/{maTK}")
    public String showEditAccountForm(@PathVariable String maTK, Model model) {
        try {
            TaiKhoanInfoResponse tkInfo = taiKhoanService.getTaiKhoanInfo(maTK);
            // Cần một DTO phù hợp để chỉnh sửa, CreateTaiKhoanRequest có thể không hoàn toàn phù hợp
            // Hoặc load Entity KhachHang và TaiKhoan rồi map sang một FormDTO
            CreateTaiKhoanRequest form = new CreateTaiKhoanRequest(); // Tạm dùng, cần DTO tốt hơn
            TaiKhoan tk = taiKhoanService.findEntityByMaTK(maTK); // Cần service trả về Entity
            if (tk != null && tk.getKhachHang() != null) {
                BeanUtils.copyProperties(tk.getKhachHang(), form);
                form.setHoTenKH(tk.getKhachHang().getHoTen()); // Đảm bảo copy đúng
                form.setSoDienThoaiKH(tk.getKhachHang().getSoDienThoai());
                form.setGioiTinhKH(tk.getKhachHang().getGioiTinh());
                if (tk.getKhachHang().getLoaiKH() != null) {
                    form.setMaLoaiKH(tk.getKhachHang().getLoaiKH().getMaLoaiKH());
                }
            }
            form.setTenTK(tkInfo.getTenTK());
            // form.setSoTienConLai(tkInfo.getSoTienConLai()); // Không nên sửa trực tiếp ở đây

            model.addAttribute("accountForm", form);
            model.addAttribute("currentBalance", tkInfo.getSoTienConLai());
            model.addAttribute("maTK", maTK); // Để dùng cho action nạp tiền hoặc update
            model.addAttribute("danhSachLoaiKH", loaiKHService.getAllLoaiKH());
            model.addAttribute("isEditMode", true);
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy tài khoản: " + maTK);
            return "redirect:/admin/accounts";
        }
        return "admin/account-form"; // Trang này cần hiển thị thông tin và có thể có nút "Nạp tiền"
    }

    // POST /accounts/update - Cập nhật thông tin khách hàng (ví dụ: loại KH)
    // Cần DTO và Service phù hợp cho việc update này.

    @GetMapping("/accounts/deposit/{maTK}")
    public String showDepositForm(@PathVariable String maTK, Model model, RedirectAttributes redirectAttributes) {
        try {
            TaiKhoanInfoResponse tkInfo = taiKhoanService.getTaiKhoanInfo(maTK);
            model.addAttribute("taiKhoanInfo", tkInfo);
            model.addAttribute("napTienRequest", new NapTienRequest(maTK, null));
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy tài khoản: " + maTK);
            return "redirect:/admin/accounts";
        }
        return "admin/deposit-form"; // Tạo trang deposit-form.html
    }

    @PostMapping("/accounts/deposit/save")
    public String saveDeposit(@Valid @ModelAttribute("napTienRequest") NapTienRequest napTienRequest,
                              BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            try { // Cần load lại thông tin tài khoản để hiển thị form
                TaiKhoanInfoResponse tkInfo = taiKhoanService.getTaiKhoanInfo(napTienRequest.getMaTK());
                model.addAttribute("taiKhoanInfo", tkInfo);
            } catch (ResourceNotFoundException e) { /* Bỏ qua, lỗi binding chính hơn */ }
            return "admin/deposit-form";
        }
        try {
            taiKhoanService.napTien(napTienRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Nạp tiền thành công cho tài khoản " + napTienRequest.getMaTK());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi nạp tiền: " + e.getMessage());
            // Quay lại trang nạp tiền hoặc trang chi tiết tài khoản
            return "redirect:/admin/accounts/deposit/" + napTienRequest.getMaTK();
        }
        return "redirect:/admin/accounts/edit/" + napTienRequest.getMaTK(); // Hoặc /admin/accounts
    }

    // --- Xem Phiên Sử Dụng ---
    @GetMapping("/sessions")
    public String showSessionsPage(Model model, @PageableDefault(size = 10, sort = "thoiGianBatDau") Pageable pageable) {
        model.addAttribute("phienSuDungPage", phienSuDungService.getAllSessionHistory(pageable));
        return "admin/view-sessions";
    }

    // --- Quản Lý Nhân Viên ---
    @GetMapping("/employees")
    public String showManageEmployeesPage(Model model, @PageableDefault(size = 10) Pageable pageable) {
        Page<NhanVien> employeePage = nhanVienService.getAllNhanVien(pageable); // Cần hàm này
        model.addAttribute("employeePage", employeePage);
        // model.addAttribute("danhSachNhanVien", nhanVienService.getAllNhanVienList()); // Nếu không phân trang
        return "admin/manage-employees";
    }

    @GetMapping("/employees/add")
    public String showAddEmployeeForm(Model model) {
        model.addAttribute("employeeForm", new NhanVien()); // Hoặc NhanVienFormDTO
        model.addAttribute("danhSachChucVu", chucVuService.getAllChucVu()); // Cần ChucVuService
        model.addAttribute("isEditMode", false);
        return "admin/employee-form";
    }

    @PostMapping("/employees/save")
    public String saveEmployee(@Valid @ModelAttribute("employeeForm") NhanVien employeeForm, // Thay bằng DTO
                               BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("danhSachChucVu", chucVuService.getAllChucVu());
            model.addAttribute("isEditMode", false);
            return "admin/employee-form";
        }
        try {
            // Cần logic gán ChucVu entity vào NhanVien nếu employeeForm.getChucVu() chỉ là maChucVu
            // ChucVu cv = chucVuService.findById(employeeForm.getMaChucVu());
            // employeeForm.setChucVu(cv);
            nhanVienService.saveNhanVien(employeeForm); // Service nên nhận DTO và xử lý
            redirectAttributes.addFlashAttribute("successMessage", "Thêm nhân viên thành công!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi thêm nhân viên: " + e.getMessage());
            model.addAttribute("danhSachChucVu", chucVuService.getAllChucVu());
            model.addAttribute("isEditMode", false);
            model.addAttribute("employeeForm", employeeForm);
            return "admin/employee-form";
        }
        return "redirect:/admin/employees";
    }

    @GetMapping("/employees/edit/{maNV}")
    public String showEditEmployeeForm(@PathVariable String maNV, Model model, RedirectAttributes redirectAttributes) {
        try {
            NhanVien nv = nhanVienService.getNhanVienById(maNV);
            // Map sang DTO nếu cần
            // NhanVienFormDTO formDTO = convertToNhanVienFormDTO(nv);
            // if (nv.getChucVu() != null) formDTO.setMaChucVu(nv.getChucVu().getMaChucVu());
            model.addAttribute("employeeForm", nv); // Hoặc formDTO
            model.addAttribute("danhSachChucVu", chucVuService.getAllChucVu());
            model.addAttribute("isEditMode", true);
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy nhân viên: " + maNV);
            return "redirect:/admin/employees";
        }
        return "admin/employee-form";
    }

    @PostMapping("/employees/update")
    public String updateEmployee(@Valid @ModelAttribute("employeeForm") NhanVien employeeForm, // Thay bằng DTO
                                 BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("danhSachChucVu", chucVuService.getAllChucVu());
            model.addAttribute("isEditMode", true);
            return "admin/employee-form";
        }
        try {
            // Tương tự như save, cần xử lý ChucVu
            nhanVienService.updateNhanVien(employeeForm.getMaNV(), employeeForm); // Service nên nhận DTO
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật nhân viên thành công!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi cập nhật nhân viên: " + e.getMessage());
            model.addAttribute("danhSachChucVu", chucVuService.getAllChucVu());
            model.addAttribute("isEditMode", true);
            model.addAttribute("employeeForm", employeeForm);
            return "admin/employee-form";
        }
        return "redirect:/admin/employees";
    }

    // --- Quản Lý Ưu Đãi ---
    @GetMapping("/promotions")
    public String showManagePromotionsPage(Model model, @PageableDefault(size = 10) Pageable pageable) {
        Page<UuDai> promotionPage = uuDaiService.getAllUuDai(pageable); // Cần hàm này
        model.addAttribute("promotionPage", promotionPage);
        // model.addAttribute("danhSachUuDai", uuDaiService.getAllUuDaiList());
        return "admin/manage-promotions";
    }

    @GetMapping("/promotions/add")
    public String showAddPromotionForm(Model model) {
        model.addAttribute("promotionForm", new UuDai()); // Hoặc UuDaiFormDTO
        model.addAttribute("isEditMode", false);
        return "admin/promotion-form";
    }

    @PostMapping("/promotions/save")
    public String savePromotion(@Valid @ModelAttribute("promotionForm") UuDai promotionForm, // Thay bằng DTO
                                BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEditMode", false);
            return "admin/promotion-form";
        }
        try {
            uuDaiService.saveUuDai(promotionForm); // Service nên nhận DTO
            redirectAttributes.addFlashAttribute("successMessage", "Thêm ưu đãi thành công!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi thêm ưu đãi: " + e.getMessage());
            model.addAttribute("isEditMode", false);
            model.addAttribute("promotionForm", promotionForm);
            return "admin/promotion-form";
        }
        return "redirect:/admin/promotions";
    }

    @GetMapping("/promotions/edit/{maUuDai}")
    public String showEditPromotionForm(@PathVariable String maUuDai, Model model, RedirectAttributes redirectAttributes) {
        try {
            UuDai ud = uuDaiService.getUuDaiById(maUuDai);
            model.addAttribute("promotionForm", ud); // Hoặc DTO
            model.addAttribute("isEditMode", true);
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy ưu đãi: " + maUuDai);
            return "redirect:/admin/promotions";
        }
        return "admin/promotion-form";
    }

    @PostMapping("/promotions/update")
    public String updatePromotion(@Valid @ModelAttribute("promotionForm") UuDai promotionForm, // Thay bằng DTO
                                  BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEditMode", true);
            return "admin/promotion-form";
        }
        try {
            uuDaiService.updateUuDai(promotionForm.getMaUuDai(), promotionForm); // Service nên nhận DTO
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật ưu đãi thành công!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi cập nhật ưu đãi: " + e.getMessage());
            model.addAttribute("isEditMode", true);
            model.addAttribute("promotionForm", promotionForm);
            return "admin/promotion-form";
        }
        return "redirect:/admin/promotions";
    }

    // Bạn sẽ cần tạo các file HTML tương ứng cho các trang trên
    // (manage-computers.html, computer-form.html, manage-services.html, etc.)
    // bên trong thư mục src/main/resources/templates/admin/
}