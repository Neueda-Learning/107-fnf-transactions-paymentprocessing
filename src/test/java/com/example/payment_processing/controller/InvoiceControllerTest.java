package com.example.payment_processing.controller;

import com.example.payment_processing.dto.CreateInvoiceRequest;
import com.example.payment_processing.model.Invoice;
import com.example.payment_processing.service.InvoiceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class InvoiceControllerTest {

    @Mock
    private InvoiceService invoiceService;

    @InjectMocks
    private InvoiceController invoiceController;

    @Test
    void createInvoice_shouldDelegateToService() {
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setInvoiceNumber("INV-100");
        request.setInvoiceAmount(new BigDecimal("250.75"));
        request.setVendorId(1L);
        request.setCurrency("USD");

        Invoice expected = new Invoice();
        expected.setInvoiceNumber("INV-100");

        when(invoiceService.createInvoice(request)).thenReturn(expected);

        Invoice result = invoiceController.createInvoice(request);

        assertEquals(expected, result);
        verify(invoiceService).createInvoice(request);
    }

    @Test
    void createInvoice_whenDuplicateInvoiceNumber_shouldThrowConflict() {
        when(invoiceService.createInvoice(any()))
                .thenThrow(new ResponseStatusException(CONFLICT, "Invoice number already exists"));

        assertThrows(ResponseStatusException.class,
                () -> invoiceController.createInvoice(new CreateInvoiceRequest()));
    }

    @Test
    void createInvoice_whenVendorNotFound_shouldThrowNotFound() {
        when(invoiceService.createInvoice(any()))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "Vendor not found"));

        assertThrows(ResponseStatusException.class,
                () -> invoiceController.createInvoice(new CreateInvoiceRequest()));
    }

    @Test
    void getAllInvoices_shouldDelegateToService() {
        List<Invoice> expected = List.of(new Invoice());
        when(invoiceService.getAllInvoices()).thenReturn(expected);

        List<Invoice> result = invoiceController.getAllInvoices();

        assertEquals(expected, result);
        verify(invoiceService).getAllInvoices();
    }

    @Test
    void getInvoiceById_shouldDelegateToService() {
        Invoice expected = new Invoice();
        when(invoiceService.getInvoiceById(10L)).thenReturn(expected);

        Invoice result = invoiceController.getInvoiceById(10L);

        assertEquals(expected, result);
        verify(invoiceService).getInvoiceById(10L);
    }

    @Test
    void getInvoiceById_whenNotFound_shouldThrowResponseStatusException() {
        when(invoiceService.getInvoiceById(999L))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "Invoice not found"));

        assertThrows(ResponseStatusException.class,
                () -> invoiceController.getInvoiceById(999L));
    }
}
