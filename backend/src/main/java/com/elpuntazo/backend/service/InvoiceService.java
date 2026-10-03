package com.elpuntazo.backend.service;

import com.elpuntazo.backend.dto.InvoiceRequest;
import com.elpuntazo.backend.dto.InvoiceResponse;
import com.elpuntazo.backend.entity.*;
import com.elpuntazo.backend.exception.BusinessException;
import com.elpuntazo.backend.repository.InvoiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Crea y guarda una factura. El total SIEMPRE se recalcula aqui en el
     * backend a partir de los valores recibidos -- nunca se confia en un
     * total que venga ya calculado desde el navegador.
     */
    @Transactional
    public InvoiceResponse create(InvoiceRequest request, User seller) {
        // El numero lo escribe el vendedor. La columna es unica en la base
        // de datos, asi que se avisa con un mensaje claro antes de intentar
        // guardar y chocar con un error tecnico.
        String invoiceNumber = request.invoiceNumber().trim();
        if (invoiceRepository.findByInvoiceNumber(invoiceNumber).isPresent()) {
            throw new BusinessException("Ya existe una factura con el numero " + invoiceNumber + ".");
        }

        BigDecimal baseValue = request.baseValue();
        BigDecimal discountPercentage = request.discountPercentage();
        BigDecimal shippingValue = request.shippingValue();
        BigDecimal shippingAssumedByCompany = request.shippingAssumedByCompany() != null
                ? request.shippingAssumedByCompany()
                : BigDecimal.ZERO;
        BigDecimal warrantyDeduction = request.warrantyDeduction() != null
                ? request.warrantyDeduction()
                : BigDecimal.ZERO;

        if (shippingAssumedByCompany.compareTo(shippingValue) > 0) {
            throw new BusinessException("El valor asumido por El Puntazo no puede ser mayor al flete total.");
        }

        // discountValue = baseValue * (discountPercentage / 100)
        BigDecimal discountValue = baseValue
                .multiply(discountPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal valueAfterDiscount = baseValue.subtract(discountValue);

        // TOTAL = valor despues del descuento + flete - lo que asume El Puntazo del flete - garantia/devolucion
        BigDecimal totalValue = valueAfterDiscount
                .add(shippingValue)
                .subtract(shippingAssumedByCompany)
                .subtract(warrantyDeduction);

        if (totalValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("El total no puede quedar negativo. Revisa los valores de flete, garantia o devolucion.");
        }

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .clientName(request.clientName().trim())
                .seller(seller)
                .baseValue(baseValue)
                .discountPercentage(discountPercentage)
                .discountValue(discountValue)
                .valueAfterDiscount(valueAfterDiscount)
                .shippingValue(shippingValue)
                .shippingAssumedByCompany(shippingAssumedByCompany)
                .warrantyDeduction(warrantyDeduction)
                .totalValue(totalValue)
                .status(InvoiceStatus.ACTIVA)
                .createdBy(seller)
                .build();

        return InvoiceResponse.from(invoiceRepository.save(invoice));
    }

    @Transactional
    public InvoiceResponse cancel(Long id, User admin) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("La factura solicitada no existe."));

        if (invoice.getStatus() == InvoiceStatus.ANULADA) {
            throw new BusinessException("Esta factura ya estaba anulada.");
        }

        invoice.setStatus(InvoiceStatus.ANULADA);
        invoice.setUpdatedBy(admin);

        return InvoiceResponse.from(invoiceRepository.save(invoice));
    }

    public InvoiceResponse getById(Long id, User requester) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("La factura solicitada no existe."));

        assertCanView(invoice, requester);

        return InvoiceResponse.from(invoice);
    }

    public Invoice getEntityById(Long id, User requester) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("La factura solicitada no existe."));

        assertCanView(invoice, requester);

        return invoice;
    }

    public Page<InvoiceResponse> search(User requester, Long sellerId, String invoiceNumber,
                                         InvoiceStatus status, LocalDate from, LocalDate to,
                                         Pageable pageable) {

        Specification<Invoice> spec = Specification.where(null);

        // Un vendedor solo puede ver sus propias facturas, sin importar
        // que filtros intente enviar -- esto se fuerza aqui, en el backend.
        if (requester.getRole() == Role.VENDEDOR) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("seller").get("id"), requester.getId()));
        } else if (sellerId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("seller").get("id"), sellerId));
        }

        if (invoiceNumber != null && !invoiceNumber.isBlank()) {
            String term = "%" + invoiceNumber.trim().toUpperCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.upper(root.get("invoiceNumber")), term));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (from != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from.atStartOfDay()));
        }
        if (to != null) {
            spec = spec.and((root, query, cb) -> cb.lessThan(root.get("createdAt"), to.plusDays(1).atStartOfDay()));
        }

        return invoiceRepository.findAll(spec, pageable).map(InvoiceResponse::from);
    }

    private void assertCanView(Invoice invoice, User requester) {
        boolean isOwner = invoice.getSeller().getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new BusinessException("No tienes permiso para ver esta factura.");
        }
    }

}
