package edu.zis.school.service;


import edu.zis.school.dto.JWTAuthResponse;
import edu.zis.school.dto.LoginDto;
import edu.zis.school.dto.RegisterDto;

public interface AuthService {

    String register(RegisterDto registerDto);
    JWTAuthResponse login(LoginDto loginDto);
}
