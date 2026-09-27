package com.pallet.backend.exception;

import com.pallet.backend.dto.response.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SỬA LẠI so với bản gốc: bản gốc bắt TOÀN BỘ RuntimeException -> luôn trả 400,
 * điều này che mất bug thật (NullPointerException, lỗi code sai...) đáng lẽ phải
 * là 500 để dev biết mà sửa, đồng thời không phân biệt được lỗi "không tìm thấy" (404)
 * với lỗi "vi phạm nghiệp vụ" (409) - hai loại lỗi rất khác nhau về ý nghĩa với người dùng.
 *
 * Quy tắc mới:
 * - ResourceNotFoundException      -> 404
 * - BusinessException              -> 409 (vi phạm quy tắc nghiệp vụ, VD chặn xóa cứng, hóa đơn đã chốt sổ...)
 * - MethodArgumentNotValidException (lỗi @Valid trên DTO request) -> 400, kèm chi tiết từng field sai
 * - DataIntegrityViolationException (vi phạm UNIQUE/FK/CHECK constraint dưới DB,
 *   bao gồm cả trigger fn_block_hard_delete / fn_block_edit_hoadon_chotso đã tạo ở V1__init_schema.sql)
 *   -> 409, không lộ chi tiết SQL thô cho client
 * - Mọi Exception khác không lường trước -> 500, LOG lại đầy đủ stacktrace (không được nuốt lỗi)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(HttpStatus.CONFLICT.value(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> loiTungTruong = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                loiTungTruong.put(fe.getField(), fe.getDefaultMessage()));
        ApiResponse<Object> response = ApiResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Dữ liệu gửi lên không hợp lệ")
                .data(loiTungTruong)
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        // Message gốc của trigger (VD "Không được xóa cứng bản ghi...") thường nằm trong
        // getMostSpecificCause() - lấy ra để hiển thị thay vì thông báo SQL dài dòng khó hiểu.
        String raw = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();
        String message = (raw != null && raw.contains("ERROR:"))
                ? raw.substring(raw.indexOf("ERROR:") + 6).split("\n")[0].trim()
                : "Thao tác vi phạm ràng buộc dữ liệu (trùng mã, khóa ngoại, hoặc quy tắc nghiệp vụ).";
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(HttpStatus.CONFLICT.value(), message));
    }

    // Bắt mọi lỗi hệ thống không lường trước (500) - đây mới là nơi RuntimeException
    // "lạ" (bug thật) nên rơi vào, KHÔNG map cứng thành 400 như bản gốc.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception ex) {
        // TODO: thay System.err bằng logger thật (SLF4J) khi tích hợp logging tập trung.
        ex.printStackTrace();
        ApiResponse<Object> response = ApiResponse.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), "Lỗi hệ thống, vui lòng thử lại sau.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
