package com.elpuntazo.backend.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record InvoiceRequest(

        @NotBlank(message = "El nombre del cliente es obligatorio")
        String clientName,

        @NotNull(message = "El valor base es obligatorio")
        @DecimalMin(value = "0.01", message = "El valor base debe ser mayor a cero")
        BigDecimal baseValue,

        @NotNull(message = "El porcentaje de descuento es obligatorio")
        @DecimalMin(value = "0.0", message = "El descuento no puede ser negativo")
        @DecimalMax(value = "100.0", message = "El descuento no puede ser mayor a 100%")
        BigDecimal discountPercentage,

        @NotNull(message = "El valor del flete es obligatorio")
        @DecimalMin(value = "0.0", message = "El flete no puede ser negativo")
        BigDecimal shippingValue,

        @DecimalMin(value = "0.0", message = "El valor asumido por El Puntazo no puede ser negativo")
        BigDecimal shippingAssumedByCompany,

        @DecimalMin(value = "0.0", message = "El valor de garantia/devolucion no puede ser negativo")
        BigDecimal warrantyDeduction
) {}
