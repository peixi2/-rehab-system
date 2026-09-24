package com.rehab.system.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    public static final int CODE_OK = 200;
    public static final int CODE_BAD_REQUEST = 400;
    public static final int CODE_FORBIDDEN = 403;
    public static final int CODE_ERROR = 500;

    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(CODE_OK, "success", data);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(CODE_OK, "success", null);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
