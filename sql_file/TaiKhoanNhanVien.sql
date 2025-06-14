USE quan_ly_quan_net;

CREATE TABLE TaiKhoanNhanVien (
    TenDangNhap VARCHAR(50) PRIMARY KEY,

    MatKhau VARCHAR(255) NOT NULL,

    MaNV VARCHAR(10) NOT NULL UNIQUE,
    -- Tạo khóa ngoại
    CONSTRAINT fk_taikhoannv_nhanvien
        FOREIGN KEY (MaNV) REFERENCES NhanVien(MaNV)
        ON DELETE CASCADE
);
INSERT INTO TaiKhoanNhanVien (TenDangNhap, MatKhau, MaNV) VALUES
('quanly_an_001', SHA2('pass_nv001', 256), 'NV001'),
('nhanvien_binh_002', SHA2('pass_nv002', 256), 'NV002'),
('nhanvien_cuong_003', SHA2('pass_nv003', 256), 'NV003'),
('nhanvien_duyen_004', SHA2('pass_nv004', 256), 'NV004'),
('nhanvien_em_005', SHA2('pass_nv005', 256), 'NV005'),
('quanly_huong_006', SHA2('pass_nv006', 256), 'NV006'),
('quanly_khang_007', SHA2('pass_nv007', 256), 'NV007'),
('nhanvien_loan_008', SHA2('pass_nv008', 256), 'NV008'),
('nhanvien_minh_009', SHA2('pass_nv009', 256), 'NV009'),
('nhanvien_ngoc_010', SHA2('pass_nv010', 256), 'NV010'),
('quanly_phat_011', SHA2('pass_nv011', 256), 'NV011'),
('quanly_quyen_012', SHA2('pass_nv012', 256), 'NV012'),
-- Giả sử các nhân viên từ 13 đến 48 đã có trong bảng NhanVien
('nhanvien_sample_013', SHA2('pass_nv013', 256), 'NV013'),
('quanly_sample_014', SHA2('pass_nv014', 256), 'NV014'),
('nhanvien_sample_015', SHA2('pass_nv015', 256), 'NV015'),
('nhanvien_sample_016', SHA2('pass_nv016', 256), 'NV016'),
('quanly_sample_017', SHA2('pass_nv017', 256), 'NV017'),
('nhanvien_sample_018', SHA2('pass_nv018', 256), 'NV018'),
('nhanvien_sample_019', SHA2('pass_nv019', 256), 'NV019'),
('quanly_sample_020', SHA2('pass_nv020', 256), 'NV020'),
('nhanvien_sample_021', SHA2('pass_nv021', 256), 'NV021'),
('nhanvien_sample_022', SHA2('pass_nv022', 256), 'NV022'),
('quanly_sample_023', SHA2('pass_nv023', 256), 'NV023'),
('nhanvien_sample_024', SHA2('pass_nv024', 256), 'NV024'),
('nhanvien_sample_025', SHA2('pass_nv025', 256), 'NV025'),
('nhanvien_sample_026', SHA2('pass_nv026', 256), 'NV026'),
('quanly_sample_027', SHA2('pass_nv027', 256), 'NV027'),
('nhanvien_sample_028', SHA2('pass_nv028', 256), 'NV028'),
('nhanvien_sample_029', SHA2('pass_nv029', 256), 'NV029'),
('nhanvien_sample_030', SHA2('pass_nv030', 256), 'NV030'),
('quanly_sample_031', SHA2('pass_nv031', 256), 'NV031'),
('nhanvien_sample_032', SHA2('pass_nv032', 256), 'NV032'),
('nhanvien_sample_033', SHA2('pass_nv033', 256), 'NV033'),
('nhanvien_sample_034', SHA2('pass_nv034', 256), 'NV034'),
('quanly_sample_035', SHA2('pass_nv035', 256), 'NV035'),
('nhanvien_sample_036', SHA2('pass_nv036', 256), 'NV036'),
('nhanvien_sample_037', SHA2('pass_nv037', 256), 'NV037'),
('nhanvien_sample_038', SHA2('pass_nv038', 256), 'NV038'),
('quanly_sample_039', SHA2('pass_nv039', 256), 'NV039'),
('nhanvien_sample_040', SHA2('pass_nv040', 256), 'NV040'),
('quanly_sample_041', SHA2('pass_nv041', 256), 'NV041'),
('nhanvien_sample_042', SHA2('pass_nv042', 256), 'NV042'),
('nhanvien_sample_043', SHA2('pass_nv043', 256), 'NV043'),
('quanly_sample_044', SHA2('pass_nv044', 256), 'NV044'),
('nhanvien_sample_045', SHA2('pass_nv045', 256), 'NV045'),
('nhanvien_sample_046', SHA2('pass_nv046', 256), 'NV046'),
('quanly_sample_047', SHA2('pass_nv047', 256), 'NV047'),
('nhanvien_sample_048', SHA2('pass_nv048', 256), 'NV048'),
-- Dữ liệu bạn cung cấp trong hình ảnh
('nhanvien_giang_049', SHA2('pass_nv049', 256), 'NV049'),
('nhanvien_thanh_050', SHA2('pass_nv050', 256), 'NV050'),
('nhanvien_quang_051', SHA2('pass_nv051', 256), 'NV051'),
('nhanvien_viet_052', SHA2('pass_nv052', 256), 'NV052'),
('nhanvien_binh_053', SHA2('pass_nv053', 256), 'NV053'),
('quanly_thuy_054', SHA2('pass_nv054', 256), 'NV054'),
('nhanvien_thuy_055', SHA2('pass_nv055', 256), 'NV055'),
('nhanvien_hoa_056', SHA2('pass_nv056', 256), 'NV056'),
('nhanvien_dat_057', SHA2('pass_nv057', 256), 'NV057'),
('nhanvien_son_058', SHA2('pass_nv058', 256), 'NV058'),
('nhanvien_long_059', SHA2('pass_nv059', 256), 'NV059'),
('nhanvien_tuan_060', SHA2('pass_nv060', 256), 'NV060'),
('nhanvien_thao_061', SHA2('pass_nv061', 256), 'NV061'),
('nhanvien_huong_062', SHA2('pass_nv062', 256), 'NV062'),
('nhanvien_ngoc_063', SHA2('pass_nv063', 256), 'NV063'),
('nhanvien_khoa_064', SHA2('pass_nv064', 256), 'NV064'),
('nhanvien_phuong_065', SHA2('pass_nv065', 256), 'NV065'),
('nhanvien_oanh_066', SHA2('pass_nv066', 256), 'NV066'),
('nhanvien_tho_067', SHA2('pass_nv067', 256), 'NV067'),
('nhanvien_Trung_068', SHA2('pass_nv068', 256), 'NV068'),
('nhanvien_yen_069', SHA2('pass_nv069', 256), 'NV069'),
('nhanvien_mai_070', SHA2('pass_nv070', 256), 'NV070');