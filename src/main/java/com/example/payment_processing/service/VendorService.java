package com.example.payment_processing.service;

import com.example.payment_processing.dto.CreateVendorRequest;
import com.example.payment_processing.model.Vendor;
import com.example.payment_processing.repository.VendorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VendorService {

    private final VendorRepository vendorRepository;


    public VendorService(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }


    public Vendor createVendor(CreateVendorRequest request) {

        Vendor vendor = new Vendor();

        vendor.setName(request.getName());
        vendor.setEmail(request.getEmail());
        vendor.setBankAccount(request.getBankAccount());
        vendor.setCountry(request.getCountry());

        return vendorRepository.save(vendor);
    }
    public void deleteVendor(Long id) {

        vendorRepository.deleteById(id);

    }


    public List<Vendor> getAllVendors() {
        return vendorRepository.findAll();
    }
}