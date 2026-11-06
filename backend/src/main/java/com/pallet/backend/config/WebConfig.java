package com.pallet.backend.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cho phép truy cập ảnh đã upload qua URL /uploads/<tên-file>.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path thuMuc = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(thuMuc);
        } catch (IOException ex) {
            throw new IllegalStateException("Không tạo được thư mục lưu ảnh: " + thuMuc, ex);
        }

        String location = thuMuc.toUri().toString();
        if (!location.endsWith("/")) {
            location += "/";
        }

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location)
                .setCachePeriod(3600);
    }
}
