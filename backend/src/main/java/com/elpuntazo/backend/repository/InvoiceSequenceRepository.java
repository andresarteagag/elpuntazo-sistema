package com.elpuntazo.backend.repository;

import com.elpuntazo.backend.entity.InvoiceSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface InvoiceSequenceRepository extends JpaRepository<InvoiceSequence, Integer> {

    // Bloqueo pesimista: si dos vendedores crean una factura al mismo tiempo,
    // el segundo espera a que el primero termine, evitando numeros repetidos.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM InvoiceSequence s WHERE s.id = 1")
    InvoiceSequence lockForUpdate();
}
