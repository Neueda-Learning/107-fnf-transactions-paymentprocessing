package com.example.payment_processing.repository;

import com.example.payment_processing.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

	boolean existsByInvoiceNumber(String invoiceNumber);


}