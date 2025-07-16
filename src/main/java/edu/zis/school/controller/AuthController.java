package edu.zis.school.controller;

import edu.zis.school.dto.JWTAuthResponse;
import edu.zis.school.dto.LoginDto;
import edu.zis.school.dto.RegisterDto;
import edu.zis.school.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterDto registerDto){
        log.info("AuthController | Request received to register with username {} & email : {}", registerDto.getUsername(), registerDto.getEmail());
        return new ResponseEntity<>(authService.register(registerDto), HttpStatus.CREATED);
    }
    
    @PostMapping("/login")
    public ResponseEntity<JWTAuthResponse> login(@Valid @RequestBody LoginDto loginDto){
        log.info("AuthController | Request received to login with usernameOrEmail: {}", loginDto.getUsernameOrEmail());
        return new ResponseEntity<>(authService.login(loginDto), HttpStatus.OK);
    }
}
