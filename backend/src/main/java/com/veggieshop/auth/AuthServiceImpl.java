package com.veggieshop.auth;

import com.veggieshop.user.User;
import com.veggieshop.user.UserRepository;
import com.veggieshop.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import com.veggieshop.exception.BadRequestException;
import com.veggieshop.user.UserMapper;

@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;   // Inject UserMapper

    @Override
    public AuthDto.AuthResponse login(AuthDto.AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new BadRequestException("Invalid credentials"));

            Object principal = authentication.getPrincipal();
            if (!(principal instanceof UserDetails)) {
                throw new BadRequestException("Invalid authentication details, please try again.");
            }

            String token = jwtUtil.generateToken((UserDetails) authentication.getPrincipal());

            AuthDto.AuthResponse response = new AuthDto.AuthResponse();
            response.setToken(token);

            // Use MapStruct for mapping
            response.setUser(userMapper.toUserResponse(user));

            return response;
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new BadRequestException("Invalid credentials");
        }
    }
}
