package kr.jaehwan.auth.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    REFRESH_TOKEN_NOT_FOUND(404, "refresh token not found"),
    USER_NOT_FOUND(404, "user not found"),
    FACT_NOT_FOUND(404, "fact not found"),
    FACT_UPDATE_NOT_FOUND(404, "fact update not found"),
    ;


    private final int status;
    private final String message;
}