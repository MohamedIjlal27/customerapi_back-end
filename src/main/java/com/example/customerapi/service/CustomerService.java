package com.example.customerapi.service;

import com.example.customerapi.entity.Customer;
import com.example.customerapi.dto.CustomerCreateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CustomerService {
    Customer createCustomer(CustomerCreateRequest request);
    List<Customer> getAllCustomers();
    List<Customer> getAllCustomersPaginated(int page, int size);
    long getTotalCustomerCount();
    Customer getCustomerById(Long id);
    Customer updateCustomer(Long id, CustomerCreateRequest request);
    void deleteCustomer(Long id);
    List<Customer> bulkCreateCustomers(MultipartFile file);
    List<Customer> bulkUpdateCustomers(MultipartFile file);
} 