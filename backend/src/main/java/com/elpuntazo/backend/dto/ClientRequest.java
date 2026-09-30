package com.elpuntazo.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record ClientRequest(
        @NotBlank(message = "El nombre del cliente es obligatorio")
        String name,

        String identification,
        String phone,
        String email
) {}
