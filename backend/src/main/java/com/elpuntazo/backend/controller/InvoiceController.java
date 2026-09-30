package com.elpuntazo.backend.controller;

import com.elpuntazo.backend.dto.InvoiceRequest;
import com.elpuntazo.backend.dto.InvoiceResponse;
import com.elpuntazo.backend.entity.Invoice;
import com.elpuntazo.backend.entity.InvoiceStatus;
import com.elpuntazo.backend.security.AppUserDetails;
import com.elpuntazo.backend.service.InvoicePdfService;
import com.elpuntazo.backend.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoicePdfService invoicePdfService;

    public InvoiceController(InvoiceService invoiceService, InvoicePdfService invoicePdfService) {
        this.invoiceService = invoiceService;
        this.invoicePdfService = invoicePdfService;
    }

    @PostMapping
    public InvoiceResponse create(@Valid @RequestBody InvoiceRequest request,
                                   @AuthenticationPrincipal AppUserDetails principal) {
        return invoiceService.create(request, principal.getUser());
    }

    @GetMapping("/{id}")
    public InvoiceResponse getById(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails principal) {
        return invoiceService.getById(id, principal.getUser());
    }

    @GetMapping
    public Page<InvoiceResponse> search(
            @AuthenticationPrincipal AppUserDetails principal,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) String invoiceNumber,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size,
                org.springframework.data.domain.Sort.by("createdAt").descending());

        return invoiceService.search(principal.getUser(), sellerId, invoiceNumber, status, from, to, pageable);
    }

    // La restriccion a ADMIN de esta ruta ya esta forzada en SecurityConfig
    @PatchMapping("/{id}/cancel")
    public InvoiceResponse cancel(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails principal) {
        return invoiceService.cancel(id, principal.getUser());
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id,
                                               @AuthenticationPrincipal AppUserDetails principal) {
        // getEntityById ya valida que el vendedor solo pueda descargar SUS
        // propias facturas (o cualquiera si es ADMIN).
        Invoice invoice = invoiceService.getEntityById(id, principal.getUser());
        byte[] pdf = invoicePdfService.generate(invoice);

        String filename = "liquidacion-" + invoice.getInvoiceNumber() + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(pdf);
    }
}
