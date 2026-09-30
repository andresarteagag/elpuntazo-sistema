package com.elpuntazo.backend.config;

import com.elpuntazo.backend.entity.Role;
import com.elpuntazo.backend.entity.User;
import com.elpuntazo.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Crea el primer usuario ADMIN al arrancar la aplicacion, unicamente
 * si todavia no existe ningun administrador en la base de datos.
 * Las credenciales se toman de variables de entorno para no dejar
 * nada quemado en el codigo. Una vez creado, el propio administrador
 * puede crear a los vendedores desde la aplicacion.
 */
@Configuration
public class InitialAdminInitializer {

    @Value("${app.initial-admin.email:}")
    private String initialAdminEmail;

    @Value("${app.initial-admin.password:}")
    private String initialAdminPassword;

    @Value("${app.initial-admin.name:Administrador}")
    private String initialAdminName;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public InitialAdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @org.springframework.context.annotation.Bean
    public CommandLineRunner createInitialAdmin() {
        return args -> {
            boolean anyAdminExists = userRepository.findAll().stream()
                    .anyMatch(u -> u.getRole() == Role.ADMIN);

            if (anyAdminExists) {
                return;
            }

            if (initialAdminEmail == null || initialAdminEmail.isBlank()
                    || initialAdminPassword == null || initialAdminPassword.isBlank()) {
                // No hay credenciales configuradas: no se crea nada.
                // Ver README para configurar INITIAL_ADMIN_EMAIL / INITIAL_ADMIN_PASSWORD.
                return;
            }

            User admin = User.builder()
                    .name(initialAdminName)
                    .email(initialAdminEmail)
                    .passwordHash(passwordEncoder.encode(initialAdminPassword))
                    .role(Role.ADMIN)
                    .active(true)
                    .build();

            userRepository.save(admin);
        };
    }
}
