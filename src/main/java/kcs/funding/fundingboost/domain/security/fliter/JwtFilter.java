package kcs.funding.fundingboost.domain.security.fliter;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.util.StringUtils.hasText;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import kcs.funding.fundingboost.domain.security.provider.JwtAuthenticationProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

@Slf4j
public class JwtFilter extends GenericFilterBean {

    private final JwtAuthenticationProvider jwtAuthenticationProvider;

    public JwtFilter(JwtAuthenticationProvider jwtAuthenticationProvider) {
        this.jwtAuthenticationProvider = jwtAuthenticationProvider;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String jwt = resolveToken(httpServletRequest); // Bearer 뒤, 토큰 부분만 파싱

        /**
         * access token 재발급을 요청하는 경우
         */
        if (httpServletRequest.getRequestURI().equals("/api/v1/auth/access-reissue")) {
            String refreshToken = httpServletRequest.getHeader(AUTHORIZATION);
        }

        if (hasText(jwt)) {
            // Bearer 텍스트로 갖는 accessToken을 갖는다면 authentication을 만들어 검증을 시도
            UsernamePasswordAuthenticationToken requestAuthentication = new UsernamePasswordAuthenticationToken(
                    jwt, "");
            // AuthenticationProvider를 이용해 검증
            Authentication authentication = jwtAuthenticationProvider.authenticate(requestAuthentication);

            // 생성된 Authentication을 SecurityContextHolder에 있는 SecurityContext에 저장
            SecurityContextHolder.getContextHolderStrategy().getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * accessToken을 반환
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION);
        if (hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}