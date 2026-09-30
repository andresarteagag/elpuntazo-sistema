package com.elpuntazo.backend.dto;

import java.math.BigDecimal;

public record DashboardSummary(
        long totalInvoices,
        long invoicesToday,
        BigDecimal totalSold,
        long activeSellers,
        long registeredClients
) {}
