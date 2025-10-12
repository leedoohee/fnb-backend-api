package com.fnb.front.backend.Service;

import com.fnb.front.backend.controller.domain.Member;
import com.fnb.front.backend.controller.domain.request.LoginRequest;
import com.fnb.front.backend.controller.domain.request.SignInRequest;
import com.fnb.front.backend.repository.MemberRepository;
import com.fnb.front.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final PasswordEncoder encoder;

    public String login(LoginRequest loginRequest) {
        String memberId = loginRequest.getMemberId();
        String password = loginRequest.getPassword();

        Member member = memberRepository.findMember(memberId);

        assert member != null : "사용자가 존재하지 않습니다.";
        assert encoder.matches(password, member.getPassword()) : "비밀번호가 일치하지 않습니다.";

        return jwtUtil.createAccessToken(member);
    }

    public boolean signUp(SignInRequest signInRequest) {
        Member member = memberRepository.findMember(signInRequest.getMemberId());

        assert member == null : "이미 가입된 회원아이디 입니다";

        this.memberRepository.insertMember(Member.builder()
                                        .memberId(signInRequest.getMemberId())
                                        .email(signInRequest.getEmail())
                                        .password(encoder.encode(signInRequest.getPassword()))
                                        .phoneNumber(signInRequest.getPhone())
                                        .address(signInRequest.getAddress())
                                        .joinDate(LocalDate.now())
                                        .build());

        return true;
    }
}
