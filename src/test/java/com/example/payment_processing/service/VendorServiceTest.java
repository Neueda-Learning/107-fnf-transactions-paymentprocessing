package com.example.payment_processing.service;

import com.example.payment_processing.dto.CreateVendorRequest;
import com.example.payment_processing.model.Vendor;
import com.example.payment_processing.repository.VendorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VendorServiceTest {

    @Mock
    private VendorRepository vendorRepository;

    @InjectMocks
    private VendorService vendorService;

    @Test
    void createVendor_savesMappedVendorFields() {
        CreateVendorRequest request = createVendorRequest();
        when(vendorRepository.save(any(Vendor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Vendor saved = vendorService.createVendor(request);

        assertEquals("ACME Ltd", saved.getName());
        assertEquals("acme@vendor.com", saved.getEmail());
        assertEquals("ACCT-001", saved.getBankAccount());
        assertEquals("US", saved.getCountry());
    }

    @Test
    void deleteVendor_callsRepositoryDeleteById() {
        vendorService.deleteVendor(11L);

        verify(vendorRepository).deleteById(11L);
    }

    @Test
    void getAllVendors_returnsRepositoryList() {
        Vendor first = vendor("Vendor A", "a@vendor.com", "A-1", "US");
        Vendor second = vendor("Vendor B", "b@vendor.com", "B-1", "DE");
        when(vendorRepository.findAll()).thenReturn(List.of(first, second));

        List<Vendor> vendors = vendorService.getAllVendors();

        assertEquals(List.of(first, second), vendors);
    }

    @Test
    void getVendorById_returnsVendorWhenFound() {
        Vendor vendor = vendor("Vendor C", "c@vendor.com", "C-1", "IN");
        when(vendorRepository.findById(9L)).thenReturn(Optional.of(vendor));

        Vendor found = vendorService.getVendorById(9L);

        assertEquals(vendor, found);
    }

    @Test
    void getVendorById_throwsWhenMissing() {
        when(vendorRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> vendorService.getVendorById(99L));

        assertEquals("Vendor not found", ex.getMessage());
    }

    private CreateVendorRequest createVendorRequest() {
        CreateVendorRequest request = new CreateVendorRequest();
        request.setName("ACME Ltd");
        request.setEmail("acme@vendor.com");
        request.setBankAccount("ACCT-001");
        request.setCountry("US");
        return request;
    }

    private Vendor vendor(String name, String email, String bankAccount, String country) {
        Vendor vendor = new Vendor();
        vendor.setName(name);
        vendor.setEmail(email);
        vendor.setBankAccount(bankAccount);
        vendor.setCountry(country);
        return vendor;
    }
}
