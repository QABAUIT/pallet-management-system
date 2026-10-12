package com.pallet.backend.service.impl;

import com.pallet.backend.dto.request.NhaCungCapRequest;
import com.pallet.backend.dto.response.NhaCungCapResponse;
import com.pallet.backend.dto.response.dto.PageResponse;
import com.pallet.backend.entity.NhaCungCap;
import com.pallet.backend.exception.BusinessException;
import com.pallet.backend.exception.ResourceNotFoundException;
import com.pallet.backend.repository.NhaCungCapRepository;
import com.pallet.backend.service.NhaCungCapService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NhaCungCapServiceImpl implements NhaCungCapService {

    private final NhaCungCapRepository nhaCungCapRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse getAll(int page, int size, String keyword, String nhomHang) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

String kw = keyword == null ? "" : keyword.trim();
String nhom = (nhomHang == null || nhomHang.isBlank()) ? "" : nhomHang.trim();

Page<NhaCungCap> pageResult = nhaCungCapRepository.searchAndFilter(kw, nhom, pageable);

        List<NhaCungCapResponse> content = pageResult.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.builder()
                .content(content)
                .pageNumber(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public NhaCungCapResponse getById(Long id) {
        NhaCungCap nhaCungCap = nhaCungCapRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Nhà cung cấp", id)); 
        return mapToResponse(nhaCungCap);
    }

    @Override
    @Transactional
    public NhaCungCapResponse create(NhaCungCapRequest request) {
        if (nhaCungCapRepository.findByMaNcc(request.getMaNcc()).isPresent()) {
            throw new BusinessException("Mã nhà cung cấp đã tồn tại: " + request.getMaNcc());
        }

        String maNcc = String.format("NCC-%03d", nhaCungCapRepository.findMaxMaNccNumber() + 1);

        NhaCungCap nhaCungCap = NhaCungCap.builder()
        .maNcc(maNcc)
                .tenNcc(request.getTenNcc())
                .mst(request.getMst())
                .diaChi(request.getDiaChi())
                .sdt(request.getSdt())
                .email(request.getEmail())
                .nguoiLienHe(request.getNguoiLienHe())
                .nhomHang(request.getNhomHang())
                .ghiChu(request.getGhiChu())
                .trangThai(request.getTrangThai())
                .createdAt(LocalDateTime.now())
                .build();

        nhaCungCap = nhaCungCapRepository.save(nhaCungCap);
        return mapToResponse(nhaCungCap);
    }

    @Override
    @Transactional
    public NhaCungCapResponse update(Long id, NhaCungCapRequest request) {
        NhaCungCap nhaCungCap = nhaCungCapRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Nhà cung cấp", id));


        nhaCungCap.setTenNcc(request.getTenNcc());
        nhaCungCap.setMst(request.getMst());
        nhaCungCap.setDiaChi(request.getDiaChi());
        nhaCungCap.setSdt(request.getSdt());
        nhaCungCap.setEmail(request.getEmail());
        nhaCungCap.setNguoiLienHe(request.getNguoiLienHe());
        nhaCungCap.setNhomHang(request.getNhomHang());
        nhaCungCap.setGhiChu(request.getGhiChu());
        nhaCungCap.setTrangThai(request.getTrangThai());

        nhaCungCap = nhaCungCapRepository.save(nhaCungCap);
        return mapToResponse(nhaCungCap);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        NhaCungCap nhaCungCap = nhaCungCapRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Nhà cung cấp", id));
        
        nhaCungCap.setTrangThai("ngung_hop_tac"); 
        nhaCungCapRepository.save(nhaCungCap);
    }

    private NhaCungCapResponse mapToResponse(NhaCungCap entity) {
        return NhaCungCapResponse.builder()
                .id(entity.getId())
                .maNcc(entity.getMaNcc())
                .tenNcc(entity.getTenNcc())
                .mst(entity.getMst())
                .diaChi(entity.getDiaChi())
                .sdt(entity.getSdt())
                .email(entity.getEmail())
                .nguoiLienHe(entity.getNguoiLienHe())
                .nhomHang(entity.getNhomHang())
                .ghiChu(entity.getGhiChu())
                .trangThai(entity.getTrangThai())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy() != null ? entity.getCreatedBy().getId().toString() : null) 
                .build();
    }
}