package com.elpuntazo.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSellerRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String name,

        @Size(min = 6, message = "La contrasena debe tener al menos 6 caracteres")
        String newPassword // opcional: null o vacio = no cambiar la contrasena
) {}
