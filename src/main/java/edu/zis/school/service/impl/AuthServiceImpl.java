package edu.zis.school.service.impl;

import edu.zis.school.dto.JWTAuthResponse;
import edu.zis.school.dto.LoginDto;
import edu.zis.school.dto.RegisterDto;
import edu.zis.school.entity.Role;
import edu.zis.school.entity.User;
import edu.zis.school.exception.AppRuntimeException;
import edu.zis.school.repository.RoleRepository;
import edu.zis.school.repository.UserRepository;
import edu.zis.school.security.JWTTokenProvider;
import edu.zis.school.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {

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

        User user = new User();
        user.setName(registerDto.getName());
        user.setUsername(registerDto.getUsername());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName("ADMIN");
        roles.add(userRole);

        user.setRoles(roles);

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
            Set<Role> roles = user.getRoles();

            Optional<Role> optionalRole = roles.stream().findFirst();

            if(optionalRole.isPresent()){
                Role role = optionalRole.get();
                userRole = role.getName();
            }
        }

        JWTAuthResponse jwtAuthResponse = new JWTAuthResponse();
        jwtAuthResponse.setAccessToken(token);
        jwtAuthResponse.setRole(userRole);

        return jwtAuthResponse;
    }
}
