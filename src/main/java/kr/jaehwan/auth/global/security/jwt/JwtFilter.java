package kr.jaehwan.auth.global.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.jaehwan.auth.global.dto.ResponseDto;
import kr.jaehwan.auth.global.util.ApiUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest httpServletRequest, @NonNull HttpServletResponse httpServletResponse, @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = jwtProvider.resolveToken(httpServletRequest);
            if (token != null) {
                Authentication authentication = jwtProvider.toAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } catch (ExpiredJwtException e) {
            httpServletResponse.setStatus(403);
            httpServletResponse.setContentType("application/json");
            ResponseDto<Void> responseDto = ApiUtil.fail(403, "access token 시간 만료");
            httpServletResponse.getWriter().write(
                    new ObjectMapper().writeValueAsString(responseDto)
            );
        }
    }
}