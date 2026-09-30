package com.elpuntazo.backend.service;

import com.elpuntazo.backend.dto.CreateSellerRequest;
import com.elpuntazo.backend.dto.SellerResponse;
import com.elpuntazo.backend.dto.UpdateSellerRequest;
import com.elpuntazo.backend.entity.Role;
import com.elpuntazo.backend.entity.User;
import com.elpuntazo.backend.exception.BusinessException;
import com.elpuntazo.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SellerService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SellerService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<SellerResponse> listAll() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.VENDEDOR)
                .map(SellerResponse::from)
                .toList();
    }

    public SellerResponse create(CreateSellerRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Ya existe un usuario registrado con ese correo.");
        }

        User seller = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.VENDEDOR)
                .active(true)
                .build();

        return SellerResponse.from(userRepository.save(seller));
    }

    public SellerResponse update(Long id, UpdateSellerRequest request) {
        User seller = getSellerOrThrow(id);

        seller.setName(request.name());

        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            seller.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        }

        return SellerResponse.from(userRepository.save(seller));
    }

    public SellerResponse setActive(Long id, boolean active) {
        User seller = getSellerOrThrow(id);
        seller.setActive(active);
        return SellerResponse.from(userRepository.save(seller));
    }

    private User getSellerOrThrow(Long id) {
        User seller = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("El vendedor solicitado no existe."));

        if (seller.getRole() != Role.VENDEDOR) {
            throw new BusinessException("El usuario solicitado no es un vendedor.");
        }

        return seller;
    }
}
