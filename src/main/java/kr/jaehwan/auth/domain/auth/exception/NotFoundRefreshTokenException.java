package kr.jaehwan.auth.domain.auth.exception;

import kr.jaehwan.auth.global.exception.BaseException;
import kr.jaehwan.auth.global.exception.ErrorCode;

public class NotFoundRefreshTokenException extends BaseException {
  public NotFoundRefreshTokenException() {
    super(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
  }

  static class Holder {
    private static final NotFoundRefreshTokenException instance = new NotFoundRefreshTokenException();
  }

  public static NotFoundRefreshTokenException getInstance() {
    return Holder.instance;
  }
}