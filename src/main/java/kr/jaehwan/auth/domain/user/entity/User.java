package kr.jaehwan.auth.domain.user.entity;

import jakarta.persistence.*;
import kr.jaehwan.auth.domain.user.dto.AdditionalInfoRequest;
import kr.jaehwan.auth.domain.user.entity.type.Gender;
import kr.jaehwan.auth.domain.user.entity.type.Role;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String imageUrl;

    @Column
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, columnDefinition = "tinyint(1) default 1")
    private boolean active;

    @Column(nullable = false, columnDefinition = "tinyint(1) default 0")
    private boolean deleted;

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public static User of(String email, String name, String imageUrl) {
        return User.builder()
                .email(email)
                .name(name)
                .imageUrl(imageUrl)
                .role(Role.GUEST)
                .active(true)
                .deleted(false)
                .build();
    }

    public void addAdditionalInfo(AdditionalInfoRequest req) {
        Objects.requireNonNull(req, "AdditionalInfoRequest must not be null");
        if (this.role != Role.GUEST) {
            throw new IllegalStateException("Already registered or invalid role");
        }
        this.age    = req.age();
        this.gender = req.gender();
    }

    public void completeRegistration() {
        if (this.role != Role.GUEST) {
            throw new IllegalStateException("Cannot complete registration: role=" + this.role);
        }
        this.role = Role.USER;
    }

    public void deactivate() {
        if (!this.active) {
            throw new IllegalStateException("Already inactive");
        }
        this.active = false;
    }

    public void deleteSoft() {
        if (this.deleted) {
            throw new IllegalStateException("Already deleted");
        }
        this.deleted = true;
    }
}
