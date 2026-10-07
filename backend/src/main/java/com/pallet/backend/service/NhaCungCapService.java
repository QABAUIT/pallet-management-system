package com.pallet.backend.service;

import com.pallet.backend.dto.request.NhaCungCapRequest;
import com.pallet.backend.dto.response.NhaCungCapResponse;
import com.pallet.backend.dto.response.dto.PageResponse;

public interface NhaCungCapService {
    
    PageResponse getAll(int page, int size, String keyword, String nhomHang);

    NhaCungCapResponse getById(Long id);

    NhaCungCapResponse create(NhaCungCapRequest request);

    NhaCungCapResponse update(Long id, NhaCungCapRequest request);

    void delete(Long id);
}