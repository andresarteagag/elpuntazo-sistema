package com.elpuntazo.backend.service;

import com.elpuntazo.backend.dto.DashboardSummary;
import com.elpuntazo.backend.entity.Role;
import com.elpuntazo.backend.repository.ClientRepository;
import com.elpuntazo.backend.repository.InvoiceRepository;
import com.elpuntazo.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class DashboardService {

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;

    public DashboardService(InvoiceRepository invoiceRepository, UserRepository userRepository,
                             ClientRepository clientRepository) {
        this.invoiceRepository = invoiceRepository;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
    }

    public DashboardSummary getSummary() {
        LocalDate today = LocalDate.now();

        long totalInvoices = invoiceRepository.count();
        long invoicesToday = invoiceRepository.countByCreatedAtBetween(
                today.atStartOfDay(), today.plusDays(1).atStartOfDay().minusNanos(1));
        var totalSold = invoiceRepository.sumTotalActiveInvoices();
        long activeSellers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.VENDEDOR && u.isActive())
                .count();
        long registeredClients = clientRepository.count();

        return new DashboardSummary(totalInvoices, invoicesToday, totalSold, activeSellers, registeredClients);
    }
}
