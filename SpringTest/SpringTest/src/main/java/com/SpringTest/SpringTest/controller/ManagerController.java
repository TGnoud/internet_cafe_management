package com.SpringTest.SpringTest.controller;

import com.SpringTest.SpringTest.entity.MayTinh;
import com.SpringTest.SpringTest.service.MayTinhService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manager")
// @PreAuthorize("hasRole('MANAGER')")
public class ManagerController {

    @Autowired
    private MayTinhService mayTinhService;

    @PostMapping("/computers")
    public ResponseEntity<MayTinh> addMayTinh(@RequestBody MayTinh mayTinh) { // Nên dùng DTO
        return new ResponseEntity<>(mayTinhService.addMayTinh(mayTinh), HttpStatus.CREATED);
    }

    @GetMapping("/computers")
    public ResponseEntity<List<MayTinh>> getAllMayTinh() {
        return ResponseEntity.ok(mayTinhService.getAllMayTinh());
    }

    @GetMapping("/computers/{maMay}")
    public ResponseEntity<MayTinh> getMayTinhById(@PathVariable String maMay) {
        return ResponseEntity.ok(mayTinhService.getMayTinhById(maMay));
    }

    @PutMapping("/computers/{maMay}")
    public ResponseEntity<MayTinh> updateMayTinh(@PathVariable String maMay, @RequestBody MayTinh mayTinhDetails) { // Nên dùng DTO
        return ResponseEntity.ok(mayTinhService.updateMayTinh(maMay, mayTinhDetails));
    }

    @DeleteMapping("/computers/{maMay}")
    public ResponseEntity<String> deleteMayTinh(@PathVariable String maMay) {
        mayTinhService.deleteMayTinh(maMay);
        return ResponseEntity.ok("Đã xóa máy tính: " + maMay);
    }

    // TODO: Thêm API cho quản lý nhân viên, quản lý loại máy, ưu đãi, etc.
}