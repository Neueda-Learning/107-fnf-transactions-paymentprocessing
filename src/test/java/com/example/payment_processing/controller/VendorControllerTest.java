package com.example.payment_processing.controller;

import com.example.payment_processing.dto.CreateVendorRequest;
import com.example.payment_processing.model.Vendor;
import com.example.payment_processing.service.VendorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VendorControllerTest {

    @Mock
    private VendorService vendorService;

    @InjectMocks
    private VendorController vendorController;

    @Test
    void createVendor_shouldDelegateToService() {
        CreateVendorRequest request = new CreateVendorRequest();
        request.setName("Vendor A");
        request.setEmail("vendor@example.com");
        request.setBankAccount("BA-123");
        request.setCountry("US");

        Vendor expected = new Vendor();
        expected.setName("Vendor A");

        when(vendorService.createVendor(request)).thenReturn(expected);

        Vendor result = vendorController.createVendor(request);

        assertEquals(expected, result);
        verify(vendorService).createVendor(request);
    }

    @Test
    void getAllVendors_shouldDelegateToService() {
        List<Vendor> expected = List.of(new Vendor());
        when(vendorService.getAllVendors()).thenReturn(expected);

        List<Vendor> result = vendorController.getAllVendors();

        assertEquals(expected, result);
        verify(vendorService).getAllVendors();
    }

    @Test
    void getVendorById_shouldDelegateToService() {
        Vendor expected = new Vendor();
        when(vendorService.getVendorById(1L)).thenReturn(expected);

        Vendor result = vendorController.getVendorById(1L);

        assertEquals(expected, result);
        verify(vendorService).getVendorById(1L);
    }

    @Test
    void deleteVendor_shouldDelegateToService() {
        vendorController.deleteVendor(1L);

        verify(vendorService).deleteVendor(1L);
    }
}
