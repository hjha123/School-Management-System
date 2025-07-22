package edu.zia.international.school.controller;

import edu.zia.international.school.dto.auth.JWTAuthResponse;
import edu.zia.international.school.dto.auth.ResetPasswordRequest;
import edu.zia.international.school.entity.User;
import edu.zia.international.school.repository.UserRepository;
import edu.zia.international.school.service.AuthService;
import edu.zia.international.school.dto.auth.LoginDto;
import edu.zia.international.school.dto.auth.RegisterDto;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private AuthService authService;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthService authService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterDto registerDto){
        log.info("AuthController | Request received to register with username {} & email : {}", registerDto.getUsername(), registerDto.getEmail());
        return new ResponseEntity<>(authService.register(registerDto), HttpStatus.CREATED);
    }
    
    @PostMapping("/login")
    public ResponseEntity<JWTAuthResponse> login(@Valid @RequestBody LoginDto loginDto){
        log.info("AuthController | Request received to login with usernameOrEmail: {}", loginDto.getUsernameOrEmail());
        return new ResponseEntity<>(authService.login(loginDto), HttpStatus.OK);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        log.info("Reset password attempt with token: {}", request.getToken());

        User user = userRepository.findByPasswordResetToken(request.getToken())
                .orElse(null);

        if (user == null) {
            log.warn("Invalid reset token.");
            return ResponseEntity.badRequest().body("Invalid token.");
        }

        if (user.getPasswordResetExpiry().before(new Date())) {
            log.warn("Expired reset token for user: {}", user.getUsername());
            return ResponseEntity.badRequest().body("Token expired.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        user.setPasswordReset(true);
        userRepository.save(user);

        log.info("Password reset successful for user: {}", user.getUsername());
        return ResponseEntity.ok("Password reset successful.");
    }
}
