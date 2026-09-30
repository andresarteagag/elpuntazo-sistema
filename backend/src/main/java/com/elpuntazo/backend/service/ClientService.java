package com.elpuntazo.backend.service;

import com.elpuntazo.backend.dto.ClientRequest;
import com.elpuntazo.backend.dto.ClientResponse;
import com.elpuntazo.backend.entity.Client;
import com.elpuntazo.backend.entity.User;
import com.elpuntazo.backend.exception.BusinessException;
import com.elpuntazo.backend.repository.ClientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Page<ClientResponse> search(String search, int page, int size) {
        String term = (search == null) ? "" : search.trim();
        Pageable pageable = PageRequest.of(page, size);
        return clientRepository.search(term, pageable).map(ClientResponse::from);
    }

    public ClientResponse create(ClientRequest request, User createdBy) {
        validateNoDuplicateIdentification(request.identification(), null);

        Client client = Client.builder()
                .name(request.name())
                .identification(blankToNull(request.identification()))
                .phone(request.phone())
                .email(request.email())
                .createdBy(createdBy)
                .build();

        return ClientResponse.from(clientRepository.save(client));
    }

    public ClientResponse update(Long id, ClientRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new BusinessException("El cliente solicitado no existe."));

        validateNoDuplicateIdentification(request.identification(), id);

        client.setName(request.name());
        client.setIdentification(blankToNull(request.identification()));
        client.setPhone(request.phone());
        client.setEmail(request.email());

        return ClientResponse.from(clientRepository.save(client));
    }

    public ClientResponse setActive(Long id, boolean active) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new BusinessException("El cliente solicitado no existe."));
        client.setActive(active);
        return ClientResponse.from(clientRepository.save(client));
    }

    private void validateNoDuplicateIdentification(String identification, Long ignoreId) {
        if (identification == null || identification.isBlank()) {
            return;
        }
        clientRepository.findAll().stream()
                .filter(c -> identification.equals(c.getIdentification()))
                .filter(c -> ignoreId == null || !c.getId().equals(ignoreId))
                .findAny()
                .ifPresent(c -> {
                    throw new BusinessException("Ya existe un cliente registrado con esa identificacion.");
                });
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
