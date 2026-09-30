package com.elpuntazo.backend.dto;

import com.elpuntazo.backend.entity.Client;

public record ClientResponse(
        Long id,
        String name,
        String identification,
        String phone,
        String email,
        boolean active
) {
    public static ClientResponse from(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getName(),
                client.getIdentification(),
                client.getPhone(),
                client.getEmail(),
                client.isActive()
        );
    }
}
