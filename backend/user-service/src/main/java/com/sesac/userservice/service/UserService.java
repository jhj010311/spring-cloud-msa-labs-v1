package com.sesac.userservice.service;

import com.sesac.userservice.dto.LoginRequest;
import com.sesac.userservice.dto.LoginResponse;
import com.sesac.userservice.entity.User;
import com.sesac.userservice.repository.UserRepository;
import com.sesac.userservice.security.JwtTokenProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    public LoginResponse login(LoginRequest loginRequest) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // 패스워드 검증
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("incorrect password");
        }
        // 토큰 발행
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getId());
        
        // 응답 생성
        return new LoginResponse(token, user.getId(), user.getEmail(), user.getName());
    }
}
