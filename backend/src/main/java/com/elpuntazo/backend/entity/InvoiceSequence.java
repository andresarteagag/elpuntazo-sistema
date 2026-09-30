package com.elpuntazo.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "invoice_sequence")
@Getter
@Setter
public class InvoiceSequence {

    @Id
    private Integer id;

    @Column(name = "last_number", nullable = false)
    private Long lastNumber;
}
