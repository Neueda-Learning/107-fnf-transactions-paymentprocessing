package com.example.payment_processing.service;

import com.example.payment_processing.dto.CreatePaymentRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    private static final BigDecimal MAX_PAYMENT_AMOUNT =
            new BigDecimal("1000000");
    private static final BigDecimal MAX_PAYMENT_AMOUNT = new BigDecimal("1000000");
    private static final BigDecimal FX_FEE_PERCENT = new BigDecimal("0.02"); // 2% forex fee

    private final PaymentRepository paymentRepository;
    private final VendorRepository vendorRepository;
    private final InvoiceRepository invoiceRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final PaymentFxDetailsRepository paymentFxDetailsRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          VendorRepository vendorRepository,
                          InvoiceRepository invoiceRepository,
                          ExchangeRateRepository exchangeRateRepository,
                          PaymentFxDetailsRepository paymentFxDetailsRepository) {
        this.paymentRepository = paymentRepository;
        this.vendorRepository = vendorRepository;
        this.invoiceRepository = invoiceRepository;
        this.exchangeRateRepository = exchangeRateRepository;
        this.paymentFxDetailsRepository = paymentFxDetailsRepository;
    }


    public Payment createPayment(CreatePaymentRequest request) {

        boolean alreadyPaid =
                paymentRepository.existsByInvoice_IdAndStatus(
                        request.getInvoiceId(),
                        PaymentStatus.COMPLETED
                );


        if (alreadyPaid) {

            throw new DuplicatePaymentException(
                    "Invoice already has a successful payment"
            );

        }


        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() ->
                        new PaymentNotFoundException("Invoice not found")
                );


        // Amount must be greater than 0
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Amount must be greater than 0"
            );
        }


        // Amount must not exceed maximum limit
        if (request.getAmount().compareTo(MAX_PAYMENT_AMOUNT) > 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Amount must not exceed 1,000,000"
            );
        }


        // Amount must have maximum 2 decimal places
        if (request.getAmount().scale() > 2) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Amount must have maximum 2 decimal places"
            );
        }


        // Sender and receiver accounts cannot be same
        if (request.getSenderAccount()
                .equalsIgnoreCase(request.getReceiverAccount())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sender account and receiver account must be different"
            );
        }


        BigDecimal invoiceAmount = invoice.getInvoiceAmount();

        if (request.getAmount().compareTo(invoiceAmount) < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payment amount cannot be less than invoice amount of "
                            + invoiceAmount
            );
        }


        // Receiver account must match vendor bank account
        Vendor vendor = vendorRepository.findById(invoice.getVendor().getId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Vendor not found"
                        )
                );


        if (vendor.getBankAccount() == null ||
                !vendor.getBankAccount()
                        .equalsIgnoreCase(request.getReceiverAccount())) {


            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Receiver account does not match vendor bank account"
            );
        }


        Payment payment = new Payment();

        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency().toUpperCase());
        payment.setSenderAccount(request.getSenderAccount());
        payment.setReceiverAccount(request.getReceiverAccount());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setStatus(PaymentStatus.CREATED);
        payment.setInvoice(invoice);

        Payment savedPayment = paymentRepository.save(payment);

        // If currencies differ, compute FX conversion and store fx details
        String senderCurrency = request.getCurrency().toUpperCase();
        String receiverCurrency = invoice.getCurrency().toUpperCase();

        if (!senderCurrency.equals(receiverCurrency)) {
            ExchangeRate senderRate = exchangeRateRepository.findByCurrencyCode(senderCurrency)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Exchange rate not found for currency: " + senderCurrency));

            ExchangeRate receiverRate = exchangeRateRepository.findByCurrencyCode(receiverCurrency)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Exchange rate not found for currency: " + receiverCurrency));

            // rate = senderRateToUsd / receiverRateToUsd
            BigDecimal crossRate = senderRate.getRateToUsd()
                    .divide(receiverRate.getRateToUsd(), 6, RoundingMode.HALF_UP);

            BigDecimal baseConverted = request.getAmount()
                    .multiply(crossRate)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal fxFee = baseConverted
                    .multiply(FX_FEE_PERCENT)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal convertedAmount = baseConverted.subtract(fxFee);

            PaymentFxDetails fxDetails = new PaymentFxDetails();
            fxDetails.setPayment(savedPayment);
            fxDetails.setSenderCurrency(senderCurrency);
            fxDetails.setReceiverCurrency(receiverCurrency);
            fxDetails.setExchangeRate(crossRate);
            fxDetails.setFxFeeAmount(fxFee);
            fxDetails.setConvertedAmount(convertedAmount);
            fxDetails.setConvertedAt(LocalDateTime.now());

            paymentFxDetailsRepository.save(fxDetails);
        }

        return savedPayment;
    }



    public List<Payment> getAllPayments() {

        return paymentRepository.findAll();

    }



    public Payment getPaymentById(Long id) {

        return paymentRepository.findById(id)
                .orElseThrow(() ->
                        new PaymentNotFoundException(
                                "Payment with id " + id + " not found"
                        )
                );

    }



    public Payment updatePaymentStatus(Long id, PaymentStatus newStatus) {


        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new PaymentNotFoundException(
                                "Payment with id " + id + " not found"
                        )
                );


        PaymentStatus currentStatus = payment.getStatus();


        boolean validTransition =
                isValidStatusTransition(
                        currentStatus,
                        newStatus
                );


        if (!validTransition) {

            throw new InvalidStatusTransitionException(
                    "Cannot move payment status from "
                            + currentStatus
                            + " to "
                            + newStatus
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