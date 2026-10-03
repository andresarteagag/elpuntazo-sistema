package com.elpuntazo.backend.config;

import com.elpuntazo.backend.entity.Role;
import com.elpuntazo.backend.entity.User;
import com.elpuntazo.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Crea al arrancar los administradores configurados que todavia no
 * existan en la base de datos.
 *
 * Regla importante: un usuario que YA existe nunca se modifica. Ni su
 * contrasena, ni su rol, ni su estado. Por eso agregar un administrador
 * nuevo es una operacion segura: no afecta a los que ya estan trabajando.
 *
 * Como consecuencia, cambiar la contrasena en la variable de entorno NO
 * cambia la del usuario ya creado: manda lo que esta en la base de datos.
 */
@Configuration
public class InitialAdminInitializer {

    private static final Logger log = LoggerFactory.getLogger(InitialAdminInitializer.class);
    private static final String NOMBRE_POR_DEFECTO = "Administrador";

    private final AdminSeedProperties properties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public InitialAdminInitializer(AdminSeedProperties properties,
                                    UserRepository userRepository,
                                    PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public CommandLineRunner crearAdministradoresConfigurados() {
        return args -> properties.getAdmins().forEach(this::crearSiNoExiste);
    }

    private void crearSiNoExiste(AdminSeedProperties.Admin admin) {
        // Entrada sin configurar (la variable de entorno no esta puesta).
        if (esVacio(admin.getEmail()) || esVacio(admin.getPassword())) {
            return;
        }

        String email = admin.getEmail().trim();

        if (userRepository.existsByEmail(email)) {
            return;
        }

        User nuevo = User.builder()
                .name(esVacio(admin.getName()) ? NOMBRE_POR_DEFECTO : admin.getName().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(admin.getPassword()))
                .role(Role.ADMIN)
                .active(true)
                .build();

        userRepository.save(nuevo);
        log.info("Administrador creado: {}", email);
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
