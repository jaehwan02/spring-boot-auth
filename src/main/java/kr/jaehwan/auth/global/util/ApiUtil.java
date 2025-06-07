package kr.jaehwan.auth.global.util;

import kr.jaehwan.auth.global.dto.ResponseDto;

public class ApiUtil {
    public static <T> ResponseDto<T> success(int status, String message, T data) {
        return new ResponseDto<>(status, message, data);
    }

    public static ResponseDto<Void> fail(int status, String message) {
        return new ResponseDto<>(status, message, null);
    }
}
