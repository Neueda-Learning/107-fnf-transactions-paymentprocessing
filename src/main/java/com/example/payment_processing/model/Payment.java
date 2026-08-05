package com.example.payment_processing.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;

    private String currency;

    private String senderAccount;

    private String receiverAccount;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;



    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;


    // Required by JPA
    public Payment() {
    }


    // Getters

    public Long getId() {
        return id;
    }


    public BigDecimal getAmount() {
        return amount;
    }


    public String getCurrency() {
        return currency;
    }


    public String getSenderAccount() {
        return senderAccount;
    }


    public String getReceiverAccount() {
        return receiverAccount;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public PaymentStatus getStatus() {
        return status;
    }





    public Invoice getInvoice() {
        return invoice;
    }


    // Setters

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }


    public void setCurrency(String currency) {
        this.currency = currency;
    }


    public void setSenderAccount(String senderAccount) {
        this.senderAccount = senderAccount;
    }


    public void setReceiverAccount(String receiverAccount) {
        this.receiverAccount = receiverAccount;
    }


    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    public void setStatus(PaymentStatus status) {
        this.status = status;
    }





    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }
}