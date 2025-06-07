package kr.jaehwan.auth.domain.user.repository;

import kr.jaehwan.auth.domain.user.entity.User;
import kr.jaehwan.auth.domain.user.entity.UserAuth;
import kr.jaehwan.auth.domain.user.entity.type.AuthType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAuthRepository extends JpaRepository<UserAuth, Long> {
    Optional<UserAuth> findByUser(User user);
    Optional<UserAuth> findByUserEmail(String email);
    Optional<UserAuth> findByAuthTypeAndOauthProviderId(
            AuthType authType,
            String oauthProviderId
    );
}
