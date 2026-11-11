package com.pallet.backend.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.pallet.backend.dto.response.ApiResponse;
import com.pallet.backend.dto.response.UploadResponse;
import com.pallet.backend.exception.BusinessException;

/**
 * Nhận ảnh sản phẩm từ frontend, lưu vào thư mục cấu hình bởi app.upload-dir
 * và trả về URL tuyệt đối để lưu vào cột mat_hang.hinh_anh (VARCHAR 255).
 */
@RestController
@RequestMapping("/api/v1/files")
@PreAuthorize("hasAnyRole('GD','PGD','NVKHO','NVBANHANG')")
public class FileUploadController {

    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UploadResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Vui lòng chọn ảnh để tải lên");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BusinessException("Ảnh tối đa 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BusinessException("Chỉ chấp nhận file ảnh (PNG, JPG, WEBP)");
        }

        String extension = layPhanMoRong(file.getOriginalFilename());
        if (extension.isEmpty()) {
            extension = switch (contentType.toLowerCase(Locale.ROOT)) {
                case "image/png" -> "png";
                case "image/webp" -> "webp";
                default -> "jpg";
            };
        }
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("Định dạng ảnh không hợp lệ, chỉ hỗ trợ PNG, JPG, WEBP");
        }

        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;

        try {
            Path thuMuc = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(thuMuc);
            Path dich = thuMuc.resolve(fileName);
            Files.copy(file.getInputStream(), dich, StandardCopyOption.REPLACE_EXISTING);

            String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(fileName)
                    .toUriString();

            return ApiResponse.success(
                    UploadResponse.builder().fileName(fileName).url(url).build(),
                    "Tải ảnh lên thành công");
        } catch (IOException ex) {
            throw new BusinessException("Không lưu được ảnh, vui lòng thử lại");
        }
    }

    private String layPhanMoRong(String tenFile) {
        if (tenFile == null) {
            return "";
        }
        int dauCham = tenFile.lastIndexOf('.');
        if (dauCham < 0 || dauCham == tenFile.length() - 1) {
            return "";
        }
        return tenFile.substring(dauCham + 1).toLowerCase(Locale.ROOT);
    }
}
