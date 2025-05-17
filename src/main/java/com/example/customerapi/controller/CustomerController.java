package com.example.customerapi.controller;

import com.example.customerapi.dto.CustomerCreateRequest;
import com.example.customerapi.entity.Customer;
import com.example.customerapi.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
@Tag(name = "Customer Management", description = "APIs for managing customers")
public class CustomerController {

    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a new customer")
    public ResponseEntity<Customer> createCustomer(@RequestBody CustomerCreateRequest request) {
        return ResponseEntity.ok(customerService.createCustomer(request));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all customers")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a customer by ID")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update a customer")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody CustomerCreateRequest request) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a customer")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/bulk-create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Bulk create customers from Excel file")
    public ResponseEntity<List<Customer>> bulkCreateCustomers(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(customerService.bulkCreateCustomers(file));
    }

    @PostMapping(value = "/bulk-update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Bulk update customers from Excel file")
    public ResponseEntity<List<Customer>> bulkUpdateCustomers(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(customerService.bulkUpdateCustomers(file));
    }
} 