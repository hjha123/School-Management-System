package edu.zia.international.school.controller;

import edu.zia.international.school.config.AppProperties;
import edu.zia.international.school.dto.auth.JWTAuthResponse;
import edu.zia.international.school.dto.auth.ResetPasswordRequest;
import edu.zia.international.school.entity.User;
import edu.zia.international.school.repository.UserRepository;
import edu.zia.international.school.service.AuthService;
import edu.zia.international.school.dto.auth.LoginDto;
import edu.zia.international.school.dto.auth.RegisterDto;
import edu.zia.international.school.service.EmailService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private AuthService authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private EmailService emailService;
    private final AppProperties appProperties;

    public AuthController(AuthService authService, UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService, AppProperties appProperties) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.appProperties = appProperties;
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
        log.info("Password reset attempt with token: {}", request.getToken());

        User user = userRepository.findByPasswordResetToken(request.getToken())
                .orElse(null);

        if (user == null || user.getPasswordResetExpiry().before(new Date())) {
            log.warn("Invalid or expired token: {}", request.getToken());
            return ResponseEntity.badRequest().body("Invalid or expired token.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        user.setPasswordReset(true);
        userRepository.save(user);

        log.info("Password successfully reset for user: {}", user.getUsername());
        return ResponseEntity.ok("Password has been successfully reset.");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            log.warn("Email not found: {}", email);
            return ResponseEntity.badRequest().body("No user registered with this email.");
        }

        User user = optionalUser.get();
        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetExpiry(Date.from(Instant.now().plus(15, ChronoUnit.MINUTES)));
        userRepository.save(user);

        String resetLink = appProperties.getResetBaseUrl() + "?token=" + token;
        String emailBody = String.format("""
            Dear %s,
            
            Click the link to reset your password:
            %s

            This link expires in 15 minutes.
            """, user.getName(), resetLink);

        emailService.sendEmail(user.getEmail(), "Password Reset Request", emailBody);
        log.info("Password reset link sent to {}", user.getEmail());
        return ResponseEntity.ok("Password reset link has been sent to your email.");
    }

}
