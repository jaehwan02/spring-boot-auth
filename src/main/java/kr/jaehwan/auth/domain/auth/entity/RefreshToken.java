package kr.jaehwan.auth.domain.auth.entity;

import kr.jaehwan.auth.domain.user.entity.type.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@RedisHash(value="refreshToken", timeToLive = 86400L)
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RefreshToken implements Serializable {

    @Id
    private String refreshToken;

    private String email;
    private Role userRole;
}