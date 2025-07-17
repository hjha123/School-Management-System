package edu.zia.international.school.service;


import edu.zia.international.school.dto.JWTAuthResponse;
import edu.zia.international.school.dto.LoginDto;
import edu.zia.international.school.dto.RegisterDto;

public interface AuthService {

    String register(RegisterDto registerDto);
    JWTAuthResponse login(LoginDto loginDto);
}
