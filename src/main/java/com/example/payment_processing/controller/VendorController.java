package com.example.payment_processing.controller;

import com.example.payment_processing.dto.CreateVendorRequest;
import com.example.payment_processing.model.Vendor;
import com.example.payment_processing.service.VendorService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/vendors")
public class VendorController {


    private final VendorService vendorService;


    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Vendor createVendor(
            @RequestBody CreateVendorRequest request) {

        return vendorService.createVendor(request);
    }

    @GetMapping
    public List<Vendor> getAllVendors() {

        return vendorService.getAllVendors();
    }
    @GetMapping("/{id}")
    public Vendor getVendorById(
            @PathVariable Long id) {

        return vendorService.getVendorById(id);

    }


}