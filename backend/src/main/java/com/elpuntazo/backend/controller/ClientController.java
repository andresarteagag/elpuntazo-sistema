package com.elpuntazo.backend.controller;

import com.elpuntazo.backend.dto.ClientRequest;
import com.elpuntazo.backend.dto.ClientResponse;
import com.elpuntazo.backend.security.AppUserDetails;
import com.elpuntazo.backend.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * El directorio de clientes es una pantalla de administracion: en
 * SecurityConfig la ruta "/api/clients/**" esta restringida a ADMIN.
 * Los vendedores no la necesitan porque al crear una factura escriben
 * el nombre del cliente como texto libre.
 */
@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public Page<ClientResponse> search(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return clientService.search(search, page, size);
    }

    @PostMapping
    public ClientResponse create(@Valid @RequestBody ClientRequest request,
                                  @AuthenticationPrincipal AppUserDetails principal) {
        return clientService.create(request, principal.getUser());
    }

    @PutMapping("/{id}")
    public ClientResponse update(@PathVariable Long id, @Valid @RequestBody ClientRequest request) {
        return clientService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public ClientResponse setStatus(@PathVariable Long id, @RequestBody java.util.Map<String, Boolean> body) {
        boolean active = Boolean.TRUE.equals(body.get("active"));
        return clientService.setActive(id, active);
    }
}
