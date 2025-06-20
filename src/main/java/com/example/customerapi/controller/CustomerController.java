package com.example.customerapi.controller;

import com.example.customerapi.dto.CustomerCreateRequest;
import com.example.customerapi.entity.Customer;
import com.example.customerapi.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a new customer")
    public ResponseEntity<Customer> createCustomer(@RequestBody CustomerCreateRequest request) {
        return ResponseEntity.ok(customerService.createCustomer(request));
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all customers (paginated)")
    public ResponseEntity<Map<String, Object>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1000") int size) {
        List<Customer> customers = customerService.getAllCustomersPaginated(page, size);
        long totalCount = customerService.getTotalCustomerCount();
        int totalPages = (int) Math.ceil((double) totalCount / size);
        
        return ResponseEntity.ok(Map.of(
            "customers", customers,
            "totalCount", totalCount,
            "currentPage", page,
            "pageSize", size,
            "totalPages", totalPages,
            "hasNext", page < totalPages - 1,
            "hasPrevious", page > 0
        ));
    }

    @GetMapping(value = "/view/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a customer by ID")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update a customer")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody CustomerCreateRequest request) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }

    @DeleteMapping(value = "/delete/{id}")
    @Operation(summary = "Delete a customer")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/bulk/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Bulk create customers from Excel file")
    public ResponseEntity<List<Customer>> bulkCreateCustomers(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(customerService.bulkCreateCustomers(file));
    }

    @PostMapping(value = "/bulk/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Bulk update customers from Excel file")
    public ResponseEntity<List<Customer>> bulkUpdateCustomers(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(customerService.bulkUpdateCustomers(file));
    }

    @GetMapping(value = "/bulk/template", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(summary = "Download customer data template for bulk update")
    public ResponseEntity<ByteArrayResource> downloadCustomerTemplate() {
        byte[] excelFile = customerService.generateCustomerTemplate();
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=customer_template.xlsx")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .contentLength(excelFile.length)
            .body(new ByteArrayResource(excelFile));
    }
} 