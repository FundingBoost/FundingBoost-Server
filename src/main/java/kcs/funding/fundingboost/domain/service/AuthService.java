package kcs.funding.fundingboost.domain.service;

import kcs.funding.fundingboost.domain.dto.common.CommonSuccessDto;
import kcs.funding.fundingboost.domain.dto.request.login.LoginDto;
import kcs.funding.fundingboost.domain.dto.request.login.SignupDto;
import kcs.funding.fundingboost.domain.dto.response.login.UsernamePasswordJwtDto;
import kcs.funding.fundingboost.domain.entity.member.Member;
import kcs.funding.fundingboost.domain.entity.token.RefreshToken;
import kcs.funding.fundingboost.domain.repository.MemberRepository;
import kcs.funding.fundingboost.domain.repository.token.RefreshTokenRepository;
import kcs.funding.fundingboost.domain.security.provider.SimpleAuthenticationProvider;
import kcs.funding.fundingboost.domain.security.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SimpleAuthenticationProvider authenticationProvider;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * 토큰이 없는 사용자는 SimpleAuthenticationProvider가 검증을 하고 검증에 성공하면 Jwt Token을 생성해서 반환
     */
    public UsernamePasswordJwtDto createJwtToken(LoginDto loginDto) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                loginDto.nickName(),
                loginDto.password());

        // username과 password를 검증
        Authentication authenticate = authenticationProvider.authenticate(authentication);

        // username과 password를 이용해 token 생성
        String accessToken = JwtUtils.createAccessToken(authenticate);
        RefreshToken refreshToken = JwtUtils.createRefreshToken(authentication);

        // redis에 refresh token 저장
        refreshTokenRepository.save(refreshToken);

        return UsernamePasswordJwtDto.fromEntity(accessToken, refreshToken.getToken());
    }

    /**
     * 회원 가입
     */
    @Transactional
    public CommonSuccessDto signup(SignupDto signupDto) {
        String encodedPassword = passwordEncoder.encode(signupDto.password());

        Member member = Member.createSignUpMember(signupDto.nickName(), encodedPassword, signupDto.email());
        memberRepository.save(member);

        return CommonSuccessDto.fromEntity(true);
    }
}
