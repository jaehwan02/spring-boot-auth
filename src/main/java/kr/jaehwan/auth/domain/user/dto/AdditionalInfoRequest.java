package kr.jaehwan.auth.domain.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import kr.jaehwan.auth.domain.user.entity.type.Gender;

public record AdditionalInfoRequest(
        @NotNull
        String name,
        @NotNull
        @Min(0)
        @Max(100)
        Integer age,
        @NotNull
        Gender gender
) {
}
