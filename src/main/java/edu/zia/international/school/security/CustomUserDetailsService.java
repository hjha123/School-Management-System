package edu.zia.international.school.security;

import edu.zia.international.school.repository.UserRepository;
import edu.zia.international.school.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail).orElseThrow(() ->
                new UsernameNotFoundException("User doesn't exist with username or email : " + usernameOrEmail));

        GrantedAuthority authorities = new SimpleGrantedAuthority(user.getRole().getName());

        return new org.springframework.security.core.userdetails.User(
                usernameOrEmail,
                user.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName())));
    }
}
