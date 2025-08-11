package com.sesac.userservice.controller;

import com.sesac.userservice.dto.LoginRequest;
import com.sesac.userservice.dto.LoginResponse;
import com.sesac.userservice.entity.User;
import com.sesac.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "유저 컨트롤러")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @Operation(summary = "사용자 조회", description = "ID로 사용자 정보를 조회합니다")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }
    
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 패스워드로 로그인하고 JWT 토큰을 발급받습니다")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
