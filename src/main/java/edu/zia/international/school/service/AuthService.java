package edu.zia.international.school.service;


import edu.zia.international.school.dto.auth.JWTAuthResponse;
import edu.zia.international.school.dto.auth.LoginDto;
import edu.zia.international.school.dto.auth.RegisterDto;

public interface AuthService {

    String register(RegisterDto registerDto);
    JWTAuthResponse login(LoginDto loginDto);
}
