package com.SpringTest.SpringTest.service;

import com.SpringTest.SpringTest.dto.response.PhienSuDungInfoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface PhienSuDungService {
    long getThoiGianConLaiDuKienPhut(int maPhien);
    List<PhienSuDungInfoResponse> getActiveSessions();
    Page<PhienSuDungInfoResponse> getSessionHistoryByMachine(String maMay, Pageable pageable);
    Page<PhienSuDungInfoResponse> getAllSessionHistory(Pageable pageable);

    BigDecimal getTotalRevenueToday();

    PhienSuDungInfoResponse endSessionAndCalculateCost(Integer maPhien, boolean apDungUuDai);

    PhienSuDungInfoResponse endSessionAndFinalize(Integer maPhien, boolean apDungUuDai);
}