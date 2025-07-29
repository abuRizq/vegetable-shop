package com.veggieshop.service;

import com.veggieshop.dto.AuthDto;
import com.veggieshop.dto.UserDto;
import com.veggieshop.entity.User;
import com.veggieshop.repository.UserRepository;
import com.veggieshop.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Override
    public AuthDto.AuthResponse login(AuthDto.AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Invalid credentials"));


            Object principal = authentication.getPrincipal();
            if (!(principal instanceof UserDetails)) {
                throw new RuntimeException("Principal is not UserDetails! It is: " + principal.getClass());
            }

            String token = jwtUtil.generateToken((UserDetails) authentication.getPrincipal());

            AuthDto.AuthResponse response = new AuthDto.AuthResponse();
            response.setToken(token);
            response.setUser(new UserDto.UserResponse() {{
                setId(user.getId());
                setName(user.getName());
                setEmail(user.getEmail());
                setRole(user.getRole());
            }});
            return response;
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
            throw ex;
        }
    }
}
