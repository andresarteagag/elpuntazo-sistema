package com.elpuntazo.backend.dto;

import com.elpuntazo.backend.entity.Invoice;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvoiceResponse(
        Long id,
        String invoiceNumber,
        String clientName,
        Long sellerId,
        String sellerName,
        BigDecimal baseValue,
        BigDecimal discountPercentage,
        BigDecimal discountValue,
        BigDecimal valueAfterDiscount,
        BigDecimal shippingValue,
        BigDecimal shippingAssumedByCompany,
        BigDecimal warrantyDeduction,
        BigDecimal totalValue,
        String status,
        LocalDateTime createdAt
) {
    public static InvoiceResponse from(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getClientName(),
                invoice.getSeller().getId(),
                invoice.getSeller().getName(),
                invoice.getBaseValue(),
                invoice.getDiscountPercentage(),
                invoice.getDiscountValue(),
                invoice.getValueAfterDiscount(),
                invoice.getShippingValue(),
                invoice.getShippingAssumedByCompany(),
                invoice.getWarrantyDeduction(),
                invoice.getTotalValue(),
                invoice.getStatus().name(),
                invoice.getCreatedAt()
        );
    }
}
