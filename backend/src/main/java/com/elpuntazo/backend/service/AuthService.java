package com.elpuntazo.backend.service;

import com.elpuntazo.backend.dto.LoginRequest;
import com.elpuntazo.backend.dto.LoginResponse;
import com.elpuntazo.backend.entity.User;
import com.elpuntazo.backend.exception.AuthenticationFailedException;
import com.elpuntazo.backend.repository.UserRepository;
import com.elpuntazo.backend.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthenticationFailedException("El correo o la contrasena son incorrectos."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthenticationFailedException("El correo o la contrasena son incorrectos.");
        }

        if (!user.isActive()) {
            throw new AuthenticationFailedException("Este usuario esta desactivado. Contacta al administrador.");
        }

        String token = jwtService.generateToken(user.getEmail(), Map.of(
                "role", user.getRole().name(),
                "name", user.getName(),
                "userId", user.getId()
        ));

        return new LoginResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }
}
