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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    private static final BigDecimal MAX_PAYMENT_AMOUNT =
            new BigDecimal("1000000");
    private static final BigDecimal MAX_PAYMENT_AMOUNT = new BigDecimal("1000000");
    private static final BigDecimal FX_FEE_PERCENT = new BigDecimal("0.02"); // 2% forex fee
    private static final BigDecimal CROSS_CURRENCY_TOLERANCE = new BigDecimal("0.01");

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

        String senderCurrency = request.getCurrency().toUpperCase();
        String receiverCurrency = invoice.getCurrency().toUpperCase();

        FxComputation fxComputation = null;
        if (senderCurrency.equals(receiverCurrency)) {
            if (request.getAmount().compareTo(invoice.getInvoiceAmount()) != 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Payment amount does not match invoice amount of " + invoice.getInvoiceAmount());
            }
        } else {
            fxComputation = computeFxDetails(request.getAmount(), senderCurrency, receiverCurrency);

            if (!isWithinTolerance(invoice.getInvoiceAmount(), fxComputation.convertedAmount(), CROSS_CURRENCY_TOLERANCE)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Converted payment amount " + fxComputation.convertedAmount()
                                + " is outside allowed tolerance of " + CROSS_CURRENCY_TOLERANCE
                                + " for invoice amount of " + invoice.getInvoiceAmount());
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
        payment.setCurrency(senderCurrency);
        payment.setSenderAccount(request.getSenderAccount());
        payment.setReceiverAccount(request.getReceiverAccount());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setStatus(PaymentStatus.CREATED);
        payment.setInvoice(invoice);

        Payment savedPayment = paymentRepository.save(payment);

        if (fxComputation != null) {
            PaymentFxDetails fxDetails = new PaymentFxDetails();
            fxDetails.setPayment(savedPayment);
            fxDetails.setSenderCurrency(senderCurrency);
            fxDetails.setReceiverCurrency(receiverCurrency);
            fxDetails.setExchangeRate(fxComputation.crossRate());
            fxDetails.setFxFeeAmount(fxComputation.fxFee());
            fxDetails.setConvertedAmount(fxComputation.convertedAmount());
            fxDetails.setConvertedAt(LocalDateTime.now());

            paymentFxDetailsRepository.save(fxDetails);
        }

        return savedPayment;
    }

    private FxComputation computeFxDetails(BigDecimal paymentAmount, String senderCurrency, String receiverCurrency) {
        ExchangeRate senderRate = exchangeRateRepository.findByCurrencyCode(senderCurrency)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Exchange rate not found for currency: " + senderCurrency));

        ExchangeRate receiverRate = exchangeRateRepository.findByCurrencyCode(receiverCurrency)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Exchange rate not found for currency: " + receiverCurrency));

        BigDecimal crossRate = senderRate.getRateToUsd()
                .divide(receiverRate.getRateToUsd(), 6, RoundingMode.HALF_UP);

        BigDecimal baseConverted = paymentAmount
                .multiply(crossRate)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal fxFee = baseConverted
                .multiply(FX_FEE_PERCENT)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal convertedAmount = baseConverted.subtract(fxFee);

        return new FxComputation(crossRate, fxFee, convertedAmount);
    }

    private boolean isWithinTolerance(BigDecimal expectedAmount, BigDecimal actualAmount, BigDecimal tolerance) {
        return expectedAmount.subtract(actualAmount).abs().compareTo(tolerance) <= 0;
    }

    private record FxComputation(BigDecimal crossRate, BigDecimal fxFee, BigDecimal convertedAmount) {
    }


    public PaymentQuoteResponse getPaymentQuote(Long invoiceId, String currency) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new PaymentNotFoundException("Invoice not found"));

        String paymentCurrency = currency.toUpperCase();
        String invoiceCurrency = invoice.getCurrency().toUpperCase();

        PaymentQuoteResponse quote = new PaymentQuoteResponse();
        quote.setInvoiceId(invoiceId);
        quote.setInvoiceCurrency(invoiceCurrency);
        quote.setInvoiceAmount(invoice.getInvoiceAmount());
        quote.setPaymentCurrency(paymentCurrency);

        if (paymentCurrency.equals(invoiceCurrency)) {
            // Same currency — sender pays exactly the invoice amount, no FX
            quote.setCrossCurrency(false);
            quote.setRequiredPaymentAmount(invoice.getInvoiceAmount());
            quote.setFxFeePercent(BigDecimal.ZERO);
            quote.setFxFeeAmount(BigDecimal.ZERO);
            quote.setConvertedAmount(invoice.getInvoiceAmount());
        } else {
            // Cross currency — reverse-compute required sender amount from invoice amount
            ExchangeRate senderRate = exchangeRateRepository.findByCurrencyCode(paymentCurrency)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Exchange rate not found for currency: " + paymentCurrency));

            ExchangeRate receiverRate = exchangeRateRepository.findByCurrencyCode(invoiceCurrency)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Exchange rate not found for currency: " + invoiceCurrency));

            // crossRate: 1 unit of paymentCurrency = crossRate units of invoiceCurrency
            BigDecimal crossRate = senderRate.getRateToUsd()
                    .divide(receiverRate.getRateToUsd(), 6, RoundingMode.HALF_UP);

            // invoiceAmount = paymentAmount * crossRate * (1 - fxFeePercent)
            // => paymentAmount = invoiceAmount / (crossRate * (1 - fxFeePercent))
            BigDecimal netFactor = BigDecimal.ONE.subtract(FX_FEE_PERCENT);
            BigDecimal requiredPaymentAmount = invoice.getInvoiceAmount()
                    .divide(crossRate.multiply(netFactor), 2, RoundingMode.HALF_UP);

            // Recompute fee and converted amount from the required amount for display
            BigDecimal baseConverted = requiredPaymentAmount
                    .multiply(crossRate)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal fxFee = baseConverted
                    .multiply(FX_FEE_PERCENT)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal convertedAmount = baseConverted.subtract(fxFee);

            quote.setCrossCurrency(true);
            quote.setRequiredPaymentAmount(requiredPaymentAmount);
            quote.setCrossRate(crossRate);
            quote.setFxFeePercent(FX_FEE_PERCENT.multiply(new BigDecimal("100")));
            quote.setFxFeeAmount(fxFee);
            quote.setConvertedAmount(convertedAmount);
        }

        return quote;
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