package com.example.payment_processing.service;

import com.example.payment_processing.dto.CreatePaymentRequest;
import com.example.payment_processing.model.Invoice;
import com.example.payment_processing.model.Payment;
import com.example.payment_processing.model.PaymentStatus;
import com.example.payment_processing.model.Vendor;
import com.example.payment_processing.repository.InvoiceRepository;
import com.example.payment_processing.repository.PaymentRepository;
import com.example.payment_processing.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

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

        // Prevent duplicate successful payment
        boolean alreadyPaid =
                paymentRepository.existsByInvoice_IdAndStatus(
                        request.getInvoiceId(),
                        PaymentStatus.COMPLETED
                );

        if (alreadyPaid) {
            throw new RuntimeException(
                    "Invoice already has a successful payment");
        }



        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
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

    public Payment getPaymentById(Long id) {

        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }
}