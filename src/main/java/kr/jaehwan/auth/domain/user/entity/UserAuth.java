package kr.jaehwan.auth.domain.user.entity;

import jakarta.persistence.*;
import kr.jaehwan.auth.domain.user.entity.type.AuthType;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "user_auth",
        uniqueConstraints = @UniqueConstraint(
                columnNames = { "oauthProvider", "oauthProviderId" }
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class UserAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long authId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthType authType;

    @Column(length = 100)
    private String passwordHash;

    @Column(length = 50)
    private String oauthProvider;

    @Column(length = 200)
    private String oauthProviderId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 1:N 중 1:1 역할을 하도록 unique=true를 걸고 ManyToOne으로 매핑
     * 이제 UserAuth.user를 통해 User가 LAZY로 로딩됩니다.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void changePasswordHash(String newHash) {
        if (authType != AuthType.LOCAL) {
            throw new IllegalStateException("Only LOCAL user can change password");
        }
        this.passwordHash = Objects.requireNonNull(newHash, "newHash must not be null");
    }
}
