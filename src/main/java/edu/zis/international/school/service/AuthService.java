package edu.zis.international.school.service;


import edu.zis.international.school.dto.JWTAuthResponse;
import edu.zis.international.school.dto.LoginDto;
import edu.zis.international.school.dto.RegisterDto;

public interface AuthService {

    String register(RegisterDto registerDto);
    JWTAuthResponse login(LoginDto loginDto);
}
