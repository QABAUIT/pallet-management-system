package com.pallet.backend.exception;

/** Ném ra khi không tìm thấy bản ghi theo id/mã -> map sang HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String entityName, Object id) {
        return new ResourceNotFoundException("Không tìm thấy " + entityName + " với id/mã = " + id);
    }
}
