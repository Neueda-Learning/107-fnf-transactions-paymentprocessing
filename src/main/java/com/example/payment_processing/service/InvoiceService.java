package com.example.payment_processing.service;

import com.example.payment_processing.dto.CreateInvoiceRequest;
import com.example.payment_processing.model.Invoice;
import com.example.payment_processing.model.Vendor;
import com.example.payment_processing.repository.InvoiceRepository;
import com.example.payment_processing.repository.VendorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final VendorRepository vendorRepository;

    public InvoiceService(InvoiceRepository invoiceRepository, VendorRepository vendorRepository) {
        this.invoiceRepository = invoiceRepository;
        this.vendorRepository = vendorRepository;
    }


    public Invoice createInvoice(CreateInvoiceRequest request) {
        if (invoiceRepository.existsByInvoiceNumber(request.getInvoiceNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invoice number already exists");
        }

        Vendor vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendor not found"));

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(request.getInvoiceNumber());
        invoice.setInvoiceAmount(request.getInvoiceAmount());
        invoice.setCurrency(request.getCurrency().toUpperCase());
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setVendor(vendor);

        return invoiceRepository.save(invoice);
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
    }
}
