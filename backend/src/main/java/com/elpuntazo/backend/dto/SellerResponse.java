package com.elpuntazo.backend.dto;

import com.elpuntazo.backend.entity.User;

public record SellerResponse(
        Long id,
        String name,
        String email,
        boolean active
) {
    public static SellerResponse from(User user) {
        return new SellerResponse(user.getId(), user.getName(), user.getEmail(), user.isActive());
    }
}
