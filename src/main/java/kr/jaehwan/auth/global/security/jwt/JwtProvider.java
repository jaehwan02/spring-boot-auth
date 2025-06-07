package kr.jaehwan.auth.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import kr.jaehwan.auth.domain.auth.dto.response.TokenResponse;
import kr.jaehwan.auth.domain.auth.entity.RefreshToken;
import kr.jaehwan.auth.domain.auth.repository.RefreshTokenRepository;
import kr.jaehwan.auth.domain.user.entity.type.Role;
import kr.jaehwan.auth.global.config.properties.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProps;
    private final RefreshTokenRepository refreshTokenRepository;

    private SecretKey signingKey;

    @PostConstruct
    private void init() {
        signingKey = Keys.hmacShaKeyFor(jwtProps.secretKey().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(String email, Role role) {
        return generateToken(email, role, TokenType.ACCESS);
    }

    public String createRefreshToken(String email, Role role) {
        String refreshToken = generateToken(email, role, TokenType.REFRESH);
        persistRefreshToken(refreshToken, email, role);
        return refreshToken;
    }

    public TokenResponse createTokenResponse(String email, Role role) {
        String accessToken = createAccessToken(email, role);
        String refreshToken = createRefreshToken(email, role);
        return new TokenResponse(accessToken, refreshToken);
    }

    private String generateToken(String email, Role role, TokenType type) {
        Instant now = Instant.now();
        long validity = (type == TokenType.ACCESS
                ? jwtProps.accessTime()
                : jwtProps.refreshTime());

        return Jwts.builder()
                .setHeaderParam("typ", type.name())
                .setSubject(email)
                .claim("role", role.getRoleKey())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(validity)))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private void persistRefreshToken(String token, String email, Role role) {
        refreshTokenRepository.save(new RefreshToken(token, email, role));
    }

    public String resolveToken(HttpServletRequest httpServletRequest) {
        var header = httpServletRequest.getHeader(jwtProps.header());
        if (header != null && header.startsWith(jwtProps.prefix())) {
            return header.substring(jwtProps.prefix().length());
        }
        return null;
    }

    public UsernamePasswordAuthenticationToken toAuthentication(String token) {
        var claims = parseClaims(token);
        String email = claims.getSubject();
        String roleKey = claims.get("role", String.class);

        var authorities = List.of(new SimpleGrantedAuthority(roleKey));
        var principal   = new org.springframework.security.core.userdetails.User(
                email, "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            throw new SecurityException("Invalid JWT token", e);
        }
    }

    private enum TokenType {
        ACCESS, REFRESH
    }
}
