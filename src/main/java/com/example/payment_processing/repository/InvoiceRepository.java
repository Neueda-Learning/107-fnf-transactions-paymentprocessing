package com.example.payment_processing.repository;

import com.example.payment_processing.model.Invoice;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
@Component
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

	boolean existsByInvoiceNumber(String invoiceNumber);

	@Modifying
	@Transactional
	@Query(value = "INSERT INTO invoices (invoice_number, invoice_amount, vendor_id) VALUES (:invoiceNumber, :invoiceAmount, :vendorId)", nativeQuery = true)
	int insertInvoice(
			@Param("invoiceNumber") String invoiceNumber,
			@Param("invoiceAmount") BigDecimal invoiceAmount,
			@Param("vendorId") Long vendorId
	);

}