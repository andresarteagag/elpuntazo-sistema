package com.elpuntazo.backend.controller;

import com.elpuntazo.backend.dto.CreateSellerRequest;
import com.elpuntazo.backend.dto.SellerResponse;
import com.elpuntazo.backend.dto.UpdateSellerRequest;
import com.elpuntazo.backend.service.SellerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Todos los endpoints de este controlador ya estan restringidos a ADMIN
 * en SecurityConfig ("/api/sellers/**" -> hasRole("ADMIN")), pero ademas
 * se protegen aqui la logica de negocio en el servicio, nunca solo en el frontend.
 */
@RestController
@RequestMapping("/api/sellers")
public class SellerController {

    private final SellerService sellerService;

    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @GetMapping
    public List<SellerResponse> listAll() {
        return sellerService.listAll();
    }

    @PostMapping
    public SellerResponse create(@Valid @RequestBody CreateSellerRequest request) {
        return sellerService.create(request);
    }

    @PutMapping("/{id}")
    public SellerResponse update(@PathVariable Long id, @Valid @RequestBody UpdateSellerRequest request) {
        return sellerService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public SellerResponse setStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        boolean active = Boolean.TRUE.equals(body.get("active"));
        return sellerService.setActive(id, active);
    }
}
