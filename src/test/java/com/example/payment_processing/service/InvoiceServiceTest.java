package com.example.payment_processing.service;

import com.example.payment_processing.dto.CreateInvoiceRequest;
import com.example.payment_processing.model.Invoice;
import com.example.payment_processing.model.Vendor;
import com.example.payment_processing.repository.InvoiceRepository;
import com.example.payment_processing.repository.VendorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private VendorRepository vendorRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    @Test
    void createInvoice_success_uppercasesCurrencyAndSaves() {
        CreateInvoiceRequest request = createInvoiceRequest("INV-1001", "125.50", 1L, "usd");
        Vendor vendor = vendor();

        when(invoiceRepository.existsByInvoiceNumber("INV-1001")).thenReturn(false);
        when(vendorRepository.findById(1L)).thenReturn(Optional.of(vendor));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Invoice saved = invoiceService.createInvoice(request);

        assertEquals("INV-1001", saved.getInvoiceNumber());
        assertEquals(new BigDecimal("125.50"), saved.getInvoiceAmount());
        assertEquals("USD", saved.getCurrency());
        assertEquals(vendor, saved.getVendor());
    }

    @Test
    void createInvoice_throwsConflictWhenInvoiceNumberExists() {
        CreateInvoiceRequest request = createInvoiceRequest("INV-2001", "10.00", 2L, "EUR");
        when(invoiceRepository.existsByInvoiceNumber("INV-2001")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> invoiceService.createInvoice(request));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals("Invoice number already exists", ex.getReason());
        verify(vendorRepository, never()).findById(any());
        verify(invoiceRepository, never()).save(any(Invoice.class));
    }

    @Test
    void createInvoice_throwsNotFoundWhenVendorMissing() {
        CreateInvoiceRequest request = createInvoiceRequest("INV-3001", "88.00", 3L, "GBP");
        when(invoiceRepository.existsByInvoiceNumber("INV-3001")).thenReturn(false);
        when(vendorRepository.findById(3L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> invoiceService.createInvoice(request));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Vendor not found", ex.getReason());
        verify(invoiceRepository, never()).save(any(Invoice.class));
    }

    @Test
    void getAllInvoices_returnsRepositoryList() {
        Invoice first = new Invoice();
        Invoice second = new Invoice();
        when(invoiceRepository.findAll()).thenReturn(List.of(first, second));

        List<Invoice> invoices = invoiceService.getAllInvoices();

        assertEquals(List.of(first, second), invoices);
    }

    @Test
    void getInvoiceById_returnsInvoiceWhenFound() {
        Invoice invoice = new Invoice();
        invoice.setId(10L);
        when(invoiceRepository.findById(10L)).thenReturn(Optional.of(invoice));

        Invoice found = invoiceService.getInvoiceById(10L);

        assertEquals(invoice, found);
    }

    @Test
    void getInvoiceById_throwsNotFoundWhenMissing() {
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> invoiceService.getInvoiceById(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Invoice not found", ex.getReason());
    }

    private CreateInvoiceRequest createInvoiceRequest(String number,
                                                      String amount,
                                                      Long vendorId,
                                                      String currency) {
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setInvoiceNumber(number);
        request.setInvoiceAmount(new BigDecimal(amount));
        request.setVendorId(vendorId);
        request.setCurrency(currency);
        return request;
    }

    private Vendor vendor() {
        Vendor vendor = new Vendor();
        vendor.setId(1L);
        vendor.setBankAccount("BANK-1");
        return vendor;
    }
}
