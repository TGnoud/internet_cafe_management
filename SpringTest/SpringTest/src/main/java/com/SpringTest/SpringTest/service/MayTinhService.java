package com.SpringTest.SpringTest.service;

import com.SpringTest.SpringTest.entity.MayTinh;

import java.util.List;

public interface MayTinhService {
    List<MayTinh> getAllMayTinh();
    MayTinh getMayTinhById(String maMay);
    MayTinh updateTrangThaiMay(String maMay, String trangThaiMoi);
    MayTinh addMayTinh(MayTinh mayTinh); // Cho Manager
    void deleteMayTinh(String maMay); // Cho Manager
    MayTinh updateMayTinh(String maMay, MayTinh mayTinhDetails); // Cho Manager
}