package com.elpuntazo.backend.repository;

import com.elpuntazo.backend.entity.Invoice;
import com.elpuntazo.backend.entity.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long>,
        JpaSpecificationExecutor<Invoice> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    Page<Invoice> findBySellerId(Long sellerId, org.springframework.data.domain.Pageable pageable);

    long countByStatus(InvoiceStatus status);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(i.totalValue), 0) FROM Invoice i WHERE i.status = 'ACTIVA'")
    BigDecimal sumTotalActiveInvoices();
}
