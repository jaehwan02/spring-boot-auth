package kr.jaehwan.auth.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LocalRegisterRequest(
        @Email(message = "이메일 형식이 아닙니다")
        @NotBlank(message = "이메일은 필수입니다")
        String email,
        @NotBlank(message = "비밀번호는 필수입니다")
        String password,
        @NotBlank(message = "이름은 필수입니다")
        String name
) {}
