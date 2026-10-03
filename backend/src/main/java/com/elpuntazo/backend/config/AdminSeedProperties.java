package com.elpuntazo.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Administradores que el sistema debe tener. Se configuran con variables
 * de entorno (ver application.yml); nunca se escribe una contrasena en el
 * codigo, entre otras cosas porque el repositorio es publico.
 *
 * Agregar un administrador nuevo no requiere tocar codigo: basta con
 * anadir otra entrada aqui y sus variables en el servidor.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AdminSeedProperties {

    private List<Admin> admins = new ArrayList<>();

    @Getter
    @Setter
    public static class Admin {
        private String email;
        private String password;
        private String name;
    }
}
