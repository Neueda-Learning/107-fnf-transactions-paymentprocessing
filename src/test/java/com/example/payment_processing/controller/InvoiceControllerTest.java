package com.example.payment_processing.controller;

import com.example.payment_processing.dto.CreateInvoiceRequest;
import com.example.payment_processing.model.Invoice;
import com.example.payment_processing.service.InvoiceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
}
