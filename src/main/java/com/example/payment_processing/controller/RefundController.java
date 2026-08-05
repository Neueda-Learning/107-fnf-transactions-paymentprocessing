package com.example.payment_processing.controller;

import com.example.payment_processing.dto.CreateRefundRequest;
import com.example.payment_processing.model.Refund;
import com.example.payment_processing.service.RefundService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/refunds")
public class RefundController {


    private final RefundService refundService;


    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Refund createRefund(
            @Valid @RequestBody CreateRefundRequest request) {

        return refundService.createRefund(request);
    }


    @GetMapping
    public List<Refund> getAllRefunds() {

        return refundService.getAllRefunds();

    }


    @GetMapping("/{id}")
    public Refund getRefundById(
            @PathVariable Long id) {

        return refundService.getRefundById(id);

    }
}