package com.astar.arena.service;

import com.astar.arena.dto.request.LoginRequest;
import com.astar.arena.dto.request.RegisterRequest;
import com.astar.arena.dto.response.AuthResponse;
import com.astar.arena.entity.User;
import com.astar.arena.repository.UserRepository;
import com.astar.arena.security.JwtTokenProvider;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request){
        if ( userRepository.existsByEmail(request.email()) ){
            throw new RuntimeException("Email already in use.");
        }

        User user = User.builder()
                .email(request.email())
                .displayedName(request.displayedName())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role("USER")
                .build();
        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getEmail());
        return new AuthResponse(token, "Bearer");
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        String token = jwtTokenProvider.generateToken(request.email());
        return new AuthResponse(token, "Bearer");
    }

}
