package com.example.payment_processing.service;

import com.example.payment_processing.dto.CreatePaymentRequest;
import com.example.payment_processing.model.Invoice;
import com.example.payment_processing.model.Payment;
import com.example.payment_processing.model.PaymentStatus;
import com.example.payment_processing.model.Vendor;
import com.example.payment_processing.repository.InvoiceRepository;
import com.example.payment_processing.repository.PaymentRepository;
import com.example.payment_processing.repository.VendorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    private static final BigDecimal MAX_PAYMENT_AMOUNT = new BigDecimal("1000000");

    private final PaymentRepository paymentRepository;
    private final VendorRepository vendorRepository;
    private final InvoiceRepository invoiceRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          VendorRepository vendorRepository,
                          InvoiceRepository invoiceRepository) {
        this.paymentRepository = paymentRepository;
        this.vendorRepository = vendorRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public Payment createPayment(CreatePaymentRequest request) {

        boolean alreadyPaid =
                paymentRepository.existsByInvoice_IdAndStatus(
                        request.getInvoiceId(),
                        PaymentStatus.COMPLETED
                );

        if (alreadyPaid) {
            throw new RuntimeException("Invoice already has a successful payment");
        }

        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        // Amount must be greater than 0
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than 0");
        }

        // Amount must not exceed 1,000,000
        if (request.getAmount().compareTo(MAX_PAYMENT_AMOUNT) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must not exceed 1,000,000");
        }

        // Amount must have maximum 2 decimal places
        if (request.getAmount().scale() > 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must have maximum 2 decimal places");
        }

        // Source and destination accounts must be different
        if (request.getSenderAccount().equalsIgnoreCase(request.getReceiverAccount())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Sender account and receiver account must be different");
        }

        // Payment amount must match invoice amount
        if (request.getAmount().compareTo(invoice.getInvoiceAmount()) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Payment amount does not match invoice amount of " + invoice.getInvoiceAmount());
        }

        // Receiver account must match vendor bank account from invoice vendor
        Vendor vendor = vendorRepository.findById(invoice.getVendor().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendor not found"));

        if (vendor.getBankAccount() == null ||
                !vendor.getBankAccount().equalsIgnoreCase(request.getReceiverAccount())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Receiver account does not match vendor bank account");
        }

        Payment payment = new Payment();
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setSenderAccount(request.getSenderAccount());
        payment.setReceiverAccount(request.getReceiverAccount());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setStatus(PaymentStatus.CREATED);
        payment.setInvoice(invoice);

        return paymentRepository.save(payment);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    public Payment updatePaymentStatus(Long id, PaymentStatus newStatus) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        PaymentStatus currentStatus = payment.getStatus();

        boolean validTransition = isValidStatusTransition(
                currentStatus,
                newStatus
        );

        if (!validTransition) {
            throw new RuntimeException(
                    "Invalid payment status transition from "
                            + currentStatus + " to " + newStatus
            );
        }

        payment.setStatus(newStatus);

        return paymentRepository.save(payment);
    }

    private boolean isValidStatusTransition(
            PaymentStatus currentStatus,
            PaymentStatus newStatus) {

        return switch (currentStatus) {
            case CREATED ->
                    newStatus == PaymentStatus.VALIDATED;

            case VALIDATED ->
                    newStatus == PaymentStatus.SENT;

            case SENT ->
                    newStatus == PaymentStatus.COMPLETED
                            || newStatus == PaymentStatus.FAILED;

            case FAILED ->
                    newStatus == PaymentStatus.CREATED;

            case COMPLETED ->
                    false;
        };
    }

}