package com.example.payment_processing.service;

import com.example.payment_processing.dto.CreateRefundRequest;
import com.example.payment_processing.exception.PaymentNotFoundException;
import com.example.payment_processing.model.Invoice;
import com.example.payment_processing.model.Payment;
import com.example.payment_processing.model.PaymentStatus;
import com.example.payment_processing.model.Refund;
import com.example.payment_processing.model.RefundStatus;
import com.example.payment_processing.repository.PaymentRepository;
import com.example.payment_processing.repository.RefundRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;


    public RefundService(
            RefundRepository refundRepository,
            PaymentRepository paymentRepository) {

        this.refundRepository = refundRepository;
        this.paymentRepository = paymentRepository;
    }


    public Refund createRefund(CreateRefundRequest request) {


        // Check payment exists
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() ->
                        new PaymentNotFoundException(
                                "Payment with id "
                                        + request.getPaymentId()
                                        + " not found"
                        )
                );


        // Payment must be completed
        if (payment.getStatus() != PaymentStatus.COMPLETED) {

            throw new RuntimeException(
                    "Only completed payments can be refunded"
            );
        }


        // Prevent duplicate refund
        boolean refundExists =
                refundRepository.existsByPayment_Id(payment.getId());


        if (refundExists) {

            throw new RuntimeException(
                    "Refund already exists for this payment"
            );
        }


        Invoice invoice = payment.getInvoice();

        BigDecimal invoiceAmount =
                invoice.getInvoiceAmount();


        BigDecimal refundAmount =
                payment.getAmount()
                        .subtract(invoiceAmount);



        // No extra amount to refund
        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new RuntimeException(
                    "No refund required. Payment amount matches invoice amount"
            );
        }



        Refund refund = new Refund();

        refund.setRefundAmount(refundAmount);

        refund.setPayment(payment);

        refund.setStatus(RefundStatus.CREATED);

        refund.setCreatedAt(LocalDateTime.now());


        return refundRepository.save(refund);
    }



    public List<Refund> getAllRefunds() {

        return refundRepository.findAll();

    }



    public Refund getRefundById(Long id) {

        return refundRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Refund with id " + id + " not found"
                        )
                );
    }
}