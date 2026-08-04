package com.example.payment_processing.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class CreateVendorRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String bankAccount;

    @NotBlank
    private String country;


    // Default constructor
    public CreateVendorRequest() {
    }


    // Getters

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public String getCountry() {
        return country;
    }


    // Setters

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}