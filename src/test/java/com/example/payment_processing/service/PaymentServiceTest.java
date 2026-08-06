package com.example.payment_processing.service;

import com.example.payment_processing.dto.CreatePaymentRequest;
import com.example.payment_processing.dto.PaymentQuoteResponse;
import com.example.payment_processing.exception.DuplicatePaymentException;
import com.example.payment_processing.exception.InvalidStatusTransitionException;
import com.example.payment_processing.exception.PaymentNotFoundException;
import com.example.payment_processing.model.ExchangeRate;
import com.example.payment_processing.model.Invoice;
import com.example.payment_processing.model.Payment;
import com.example.payment_processing.model.PaymentFxDetails;
import com.example.payment_processing.model.PaymentStatus;
import com.example.payment_processing.model.Vendor;
import com.example.payment_processing.repository.ExchangeRateRepository;
import com.example.payment_processing.repository.InvoiceRepository;
import com.example.payment_processing.repository.PaymentFxDetailsRepository;
import com.example.payment_processing.repository.PaymentRepository;
import com.example.payment_processing.repository.VendorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private VendorRepository vendorRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private PaymentFxDetailsRepository paymentFxDetailsRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void createPayment_sameCurrency_success() {
        Invoice invoice = invoice(10L, "150.00", 1L, "VENDOR-ACC-1");
        CreatePaymentRequest request = paymentRequest(10L, "150.00", "usd", "SENDER-123", "VENDOR-ACC-1");

        when(paymentRepository.existsByInvoice_IdAndStatus(10L, PaymentStatus.COMPLETED)).thenReturn(false);
        when(invoiceRepository.findById(10L)).thenReturn(Optional.of(invoice));
        when(vendorRepository.findById(1L)).thenReturn(Optional.of(invoice.getVendor()));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment saved = paymentService.createPayment(request);

        assertEquals(new BigDecimal("150.00"), saved.getAmount());
        assertEquals("USD", saved.getCurrency());
        assertEquals("SENDER-123", saved.getSenderAccount());
        assertEquals("VENDOR-ACC-1", saved.getReceiverAccount());
        assertEquals(PaymentStatus.CREATED, saved.getStatus());
        assertEquals(invoice, saved.getInvoice());
        assertNotNull(saved.getCreatedAt());
        verify(paymentFxDetailsRepository, never()).save(any(PaymentFxDetails.class));
    }

    @Test
    void createPayment_crossCurrency_success_savesFxDetails() {
        Invoice invoice = invoice(11L, "117.60", 2L, "VENDOR-ACC-2");
        CreatePaymentRequest request = paymentRequest(11L, "100.00", "eur", "SENDER-222", "VENDOR-ACC-2");

        when(paymentRepository.existsByInvoice_IdAndStatus(11L, PaymentStatus.COMPLETED)).thenReturn(false);
        when(invoiceRepository.findById(11L)).thenReturn(Optional.of(invoice));
        when(vendorRepository.findById(2L)).thenReturn(Optional.of(invoice.getVendor()));
        when(exchangeRateRepository.findByCurrencyCode("EUR")).thenReturn(Optional.of(rate("EUR", "1.200000")));
        when(exchangeRateRepository.findByCurrencyCode("USD")).thenReturn(Optional.of(rate("USD", "1.000000")));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment saved = paymentService.createPayment(request);

        assertEquals("EUR", saved.getCurrency());
        assertEquals(PaymentStatus.CREATED, saved.getStatus());

        ArgumentCaptor<PaymentFxDetails> fxCaptor = ArgumentCaptor.forClass(PaymentFxDetails.class);
        verify(paymentFxDetailsRepository).save(fxCaptor.capture());
        PaymentFxDetails fxDetails = fxCaptor.getValue();
        assertEquals("EUR", fxDetails.getSenderCurrency());
        assertEquals("USD", fxDetails.getReceiverCurrency());
        assertEquals(new BigDecimal("1.200000"), fxDetails.getExchangeRate());
        assertEquals(new BigDecimal("2.40"), fxDetails.getFxFeeAmount());
        assertEquals(new BigDecimal("117.60"), fxDetails.getConvertedAmount());
        assertNotNull(fxDetails.getConvertedAt());
    }

    @Test
    void createPayment_throwsWhenInvoiceAlreadyHasCompletedPayment() {
        CreatePaymentRequest request = paymentRequest(12L, "10.00", "USD", "S1", "R1");
        when(paymentRepository.existsByInvoice_IdAndStatus(12L, PaymentStatus.COMPLETED)).thenReturn(true);

        assertThrows(DuplicatePaymentException.class, () -> paymentService.createPayment(request));

        verify(invoiceRepository, never()).findById(any());
    }

    @Test
    void createPayment_throwsWhenInvoiceNotFound() {
        CreatePaymentRequest request = paymentRequest(13L, "10.00", "USD", "S1", "R1");
        when(paymentRepository.existsByInvoice_IdAndStatus(13L, PaymentStatus.COMPLETED)).thenReturn(false);
        when(invoiceRepository.findById(13L)).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class, () -> paymentService.createPayment(request));
    }

    @Test
    void createPayment_throwsWhenAmountHasMoreThan2Decimals() {
        Invoice invoice = invoice(14L, "10.00", 3L, "ACC");
        CreatePaymentRequest request = paymentRequest(14L, "10.001", "USD", "S1", "ACC");

        when(paymentRepository.existsByInvoice_IdAndStatus(14L, PaymentStatus.COMPLETED)).thenReturn(false);
        when(invoiceRepository.findById(14L)).thenReturn(Optional.of(invoice));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> paymentService.createPayment(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Amount must have maximum 2 decimal places", ex.getReason());
    }

    @Test
    void createPayment_throwsWhenSenderAndReceiverAccountsMatch() {
        Invoice invoice = invoice(15L, "10.00", 4L, "ACC");
        CreatePaymentRequest request = paymentRequest(15L, "10.00", "USD", "same-acc", "SAME-ACC");

        when(paymentRepository.existsByInvoice_IdAndStatus(15L, PaymentStatus.COMPLETED)).thenReturn(false);
        when(invoiceRepository.findById(15L)).thenReturn(Optional.of(invoice));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> paymentService.createPayment(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Sender account and receiver account must be different", ex.getReason());
    }

    @Test
    void createPayment_throwsWhenSameCurrencyAmountMismatch() {
        Invoice invoice = invoice(16L, "10.00", 5L, "ACC");
        CreatePaymentRequest request = paymentRequest(16L, "9.99", "USD", "S1", "ACC");

        when(paymentRepository.existsByInvoice_IdAndStatus(16L, PaymentStatus.COMPLETED)).thenReturn(false);
        when(invoiceRepository.findById(16L)).thenReturn(Optional.of(invoice));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> paymentService.createPayment(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertNotNull(ex.getReason());
        assertTrue(ex.getReason().contains("Payment amount does not match invoice amount"));
    }

    @Test
    void createPayment_throwsWhenCrossCurrencyConvertedAmountOutsideTolerance() {
        Invoice invoice = invoice(17L, "117.50", 6L, "ACC");
        CreatePaymentRequest request = paymentRequest(17L, "100.00", "EUR", "S1", "ACC");

        when(paymentRepository.existsByInvoice_IdAndStatus(17L, PaymentStatus.COMPLETED)).thenReturn(false);
        when(invoiceRepository.findById(17L)).thenReturn(Optional.of(invoice));
        when(exchangeRateRepository.findByCurrencyCode("EUR")).thenReturn(Optional.of(rate("EUR", "1.200000")));
        when(exchangeRateRepository.findByCurrencyCode("USD")).thenReturn(Optional.of(rate("USD", "1.000000")));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> paymentService.createPayment(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertNotNull(ex.getReason());
        assertTrue(ex.getReason().contains("outside allowed tolerance"));
    }

    @Test
    void createPayment_throwsWhenVendorAccountDoesNotMatchReceiverAccount() {
        Invoice invoice = invoice(18L, "10.00", 7L, "VENDOR-ACC");
        CreatePaymentRequest request = paymentRequest(18L, "10.00", "USD", "S1", "OTHER-ACC");

        when(paymentRepository.existsByInvoice_IdAndStatus(18L, PaymentStatus.COMPLETED)).thenReturn(false);
        when(invoiceRepository.findById(18L)).thenReturn(Optional.of(invoice));
        when(vendorRepository.findById(7L)).thenReturn(Optional.of(invoice.getVendor()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> paymentService.createPayment(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Receiver account does not match vendor bank account", ex.getReason());
    }

    @Test
    void getPaymentQuote_sameCurrency_hasNoFx() {
        Invoice invoice = invoice(20L, "250.00", 8L, "ACC");
        when(invoiceRepository.findById(20L)).thenReturn(Optional.of(invoice));

        PaymentQuoteResponse quote = paymentService.getPaymentQuote(20L, "usd");

        assertFalse(quote.isCrossCurrency());
        assertEquals(new BigDecimal("250.00"), quote.getRequiredPaymentAmount());
        assertEquals(BigDecimal.ZERO, quote.getFxFeePercent());
        assertEquals(BigDecimal.ZERO, quote.getFxFeeAmount());
        assertEquals(new BigDecimal("250.00"), quote.getConvertedAmount());
    }

    @Test
    void getPaymentQuote_crossCurrency_calculatesExpectedValues() {
        Invoice invoice = invoice(21L, "117.60", 9L, "ACC");
        when(invoiceRepository.findById(21L)).thenReturn(Optional.of(invoice));
        when(exchangeRateRepository.findByCurrencyCode("EUR")).thenReturn(Optional.of(rate("EUR", "1.200000")));
        when(exchangeRateRepository.findByCurrencyCode("USD")).thenReturn(Optional.of(rate("USD", "1.000000")));

        PaymentQuoteResponse quote = paymentService.getPaymentQuote(21L, "eur");

        assertTrue(quote.isCrossCurrency());
        assertEquals(new BigDecimal("100.00"), quote.getRequiredPaymentAmount());
        assertEquals(new BigDecimal("1.200000"), quote.getCrossRate());
        assertEquals(new BigDecimal("2.00"), quote.getFxFeePercent());
        assertEquals(new BigDecimal("2.40"), quote.getFxFeeAmount());
        assertEquals(new BigDecimal("117.60"), quote.getConvertedAmount());
    }

    @Test
    void getAllPayments_returnsRepositoryResult() {
        Payment p1 = new Payment();
        Payment p2 = new Payment();
        when(paymentRepository.findAll()).thenReturn(List.of(p1, p2));

        List<Payment> payments = paymentService.getAllPayments();

        assertEquals(2, payments.size());
        assertEquals(List.of(p1, p2), payments);
    }

    @Test
    void getPaymentById_throwsWhenNotFound() {
        when(paymentRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class, () -> paymentService.getPaymentById(42L));
    }

    @Test
    void updatePaymentStatus_allowsValidTransition() {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.CREATED);

        when(paymentRepository.findById(50L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment updated = paymentService.updatePaymentStatus(50L, PaymentStatus.VALIDATED);

        assertEquals(PaymentStatus.VALIDATED, updated.getStatus());
    }

    @Test
    void updatePaymentStatus_rejectsInvalidTransition() {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.CREATED);
        when(paymentRepository.findById(51L)).thenReturn(Optional.of(payment));

        assertThrows(InvalidStatusTransitionException.class,
                () -> paymentService.updatePaymentStatus(51L, PaymentStatus.COMPLETED));

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    private CreatePaymentRequest paymentRequest(Long invoiceId,
                                                String amount,
                                                String currency,
                                                String sender,
                                                String receiver) {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setInvoiceId(invoiceId);
        request.setAmount(new BigDecimal(amount));
        request.setCurrency(currency);
        request.setSenderAccount(sender);
        request.setReceiverAccount(receiver);
        return request;
    }

    private Invoice invoice(Long invoiceId,
                            String invoiceAmount,
                            Long vendorId,
                            String vendorAccount) {
        Vendor vendor = new Vendor();
        vendor.setId(vendorId);
        vendor.setBankAccount(vendorAccount);

        Invoice invoice = new Invoice();
        invoice.setId(invoiceId);
        invoice.setInvoiceAmount(new BigDecimal(invoiceAmount));
        invoice.setCurrency("USD");
        invoice.setVendor(vendor);
        return invoice;
    }

    private ExchangeRate rate(String code, String rateToUsd) {
        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setCurrencyCode(code);
        exchangeRate.setRateToUsd(new BigDecimal(rateToUsd));
        return exchangeRate;
    }
}

