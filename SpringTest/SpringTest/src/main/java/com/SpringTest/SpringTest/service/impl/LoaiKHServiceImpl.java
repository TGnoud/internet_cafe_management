package com.SpringTest.SpringTest.service.impl;

import com.SpringTest.SpringTest.entity.LoaiKH;
import com.SpringTest.SpringTest.exception.ResourceNotFoundException;
import com.SpringTest.SpringTest.repository.LoaiKHRepository;
import com.SpringTest.SpringTest.service.LoaiKHService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoaiKHServiceImpl implements LoaiKHService {

    @Autowired
    private LoaiKHRepository loaiKHRepository;

    @Override
    public List<LoaiKH> getAllLoaiKH() {
        return loaiKHRepository.findAll();
    }

    @Override
    public LoaiKH getLoaiKHById(String maLoaiKH) {
        return loaiKHRepository.findById(maLoaiKH)
                .orElseThrow(() -> new ResourceNotFoundException("Loại khách hàng không tồn tại: " + maLoaiKH));
    }

    // Implement các phương thức CRUD khác cho LoaiKH nếu Admin có thể quản lý
}