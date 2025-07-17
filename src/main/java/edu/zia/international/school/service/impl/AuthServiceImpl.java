package edu.zia.international.school.service.impl;

import edu.zia.international.school.dto.JWTAuthResponse;
import edu.zia.international.school.exception.AppRuntimeException;
import edu.zia.international.school.repository.RoleRepository;
import edu.zia.international.school.repository.UserRepository;
import edu.zia.international.school.security.JWTTokenProvider;
import edu.zia.international.school.service.AuthService;
import edu.zia.international.school.dto.LoginDto;
import edu.zia.international.school.dto.RegisterDto;
import edu.zia.international.school.entity.Role;
import edu.zia.international.school.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {
    private static final String DEFAULT_ROLE = "STUDENT";
    private static final Set<String> ALLOWED_SIGNUP_ROLES = Set.of("STUDENT", "PARENT");

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JWTTokenProvider jwtTokenProvider;

    public AuthServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                           JWTTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public String register(RegisterDto registerDto) {
        // Check if user already exists with the given username
        if(userRepository.existsByUsername(registerDto.getUsername())){
            throw new AppRuntimeException(HttpStatus.BAD_REQUEST, "User already exists by given username : " + registerDto.getUsername());
        }

        //Check if user already exists with the given email
        if(userRepository.existsByEmail(registerDto.getEmail())){
            throw new AppRuntimeException(HttpStatus.BAD_REQUEST, "User already exists by given email id : " + registerDto.getEmail());
        }

        // 1. Determine the role that will actually be saved
        String roleName = (registerDto.getRole() == null || registerDto.getRole().isBlank())
                ? DEFAULT_ROLE
                : registerDto.getRole().toUpperCase();

        // 2. Whitelist check — prevents someone from self‑registering as ADMIN
        if (!ALLOWED_SIGNUP_ROLES.contains(roleName))
            throw new RuntimeException("Role not allowed for self‑registration");

        User user = new User();
        user.setName(registerDto.getName());
        user.setUsername(registerDto.getUsername());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        Role userRole = roleRepository.findByName(roleName).orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        user.setRole(userRole);

        userRepository.save(user);

        return "User registered successfully!";
    }

    @Override
    public JWTAuthResponse login(LoginDto loginDto) {
        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDto.getUsernameOrEmail(), loginDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authenticate);

        String token = jwtTokenProvider.getToken(authenticate);

        Optional<User> optionalUser = userRepository.findByUsernameOrEmail(loginDto.getUsernameOrEmail(), loginDto.getUsernameOrEmail());

        String userRole = "";
        if(optionalUser.isPresent()){
            User user = optionalUser.get();
            Role role = user.getRole();
            userRole = role.getName();
        }

        JWTAuthResponse jwtAuthResponse = new JWTAuthResponse();
        jwtAuthResponse.setAccessToken(token);
        jwtAuthResponse.setRole(userRole);

        return jwtAuthResponse;
    }
}
