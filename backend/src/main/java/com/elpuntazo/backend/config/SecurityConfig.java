package com.elpuntazo.backend.config;

import com.elpuntazo.backend.security.AppUserDetailsService;
import com.elpuntazo.backend.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final AppUserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * Dominios propios del frontend, permitidos SIEMPRE ademas de lo que
     * diga ALLOWED_ORIGINS.
     *
     * Vercel le anade un sufijo aleatorio al dominio de produccion (por eso
     * es "elpuntazo-sistema-mu" y no "elpuntazo-sistema") y genera uno
     * distinto en cada despliegue de preview. Sin esto, basta con que la
     * variable quede desactualizada para que nadie pueda iniciar sesion,
     * que es exactamente lo que ocurrio: el navegador recibia
     * 403 "Invalid CORS request" y mostraba "No se pudo conectar con el
     * servidor".
     *
     * El patron se limita a los dominios de esta aplicacion; no se abre
     * "*.vercel.app" entero.
     */
    private static final List<String> FRONTEND_PROPIO = List.of(
            "https://elpuntazo-sistema.vercel.app",
            "https://elpuntazo-sistema-*.vercel.app"
    );

    public SecurityConfig(AppUserDetailsService userDetailsService, JwtAuthFilter jwtAuthFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Se usa setAllowedOriginPatterns (y no setAllowedOrigins) para poder
     * aceptar comodines, por ejemplo "https://*.vercel.app": Vercel genera
     * una URL distinta en cada despliegue de preview y sin el comodin esas
     * URLs quedarian bloqueadas por CORS.
     *
     * Cada origen se recorta con trim() porque ALLOWED_ORIGINS suele
     * escribirse separado por comas y espacios ("a, b"); sin el trim el
     * segundo origen nunca coincidiria.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> origins = Stream.concat(
                        Arrays.stream(allowedOrigins.split(","))
                                .map(String::trim)
                                .filter(origin -> !origin.isEmpty()),
                        FRONTEND_PROPIO.stream())
                .distinct()
                .toList();

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        // El navegador necesita ver Content-Disposition para nombrar el PDF descargado.
        configuration.setExposedHeaders(List.of("Content-Disposition"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/sellers/**").hasRole("ADMIN")
                        .requestMatchers("/api/dashboard/**").hasRole("ADMIN")
                        .requestMatchers("/api/invoices/*/cancel").hasRole("ADMIN")
                        .requestMatchers("/api/clients/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
