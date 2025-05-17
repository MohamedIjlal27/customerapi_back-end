package com.example.customerapi.service.impl;

import com.example.customerapi.dto.*;
import com.example.customerapi.entity.*;
import com.example.customerapi.repository.CustomerRepository;
import com.example.customerapi.repository.CountryRepository;
import com.example.customerapi.repository.CityRepository;
import com.example.customerapi.repository.FamilyMemberRepository;
import com.example.customerapi.service.CustomerService;
import jakarta.persistence.EntityNotFoundException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

import com.example.customerapi.dto.CustomerCreateRequest;
import com.example.customerapi.dto.MobileNumberCreateRequest;
import com.example.customerapi.dto.AddressCreateRequest;
import com.example.customerapi.dto.FamilyMemberCreateRequest;
import com.example.customerapi.dto.CityCreateRequest;
import com.example.customerapi.dto.CountryCreateRequest;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final FamilyMemberRepository familyMemberRepository;

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository,
                             CountryRepository countryRepository,
                             CityRepository cityRepository,
                             FamilyMemberRepository familyMemberRepository) {
        this.customerRepository = customerRepository;
        this.countryRepository = countryRepository;
        this.cityRepository = cityRepository;
        this.familyMemberRepository = familyMemberRepository;
    }

    @Override
    @Transactional
    public Customer createCustomer(CustomerCreateRequest request) {
        // Check if customer with same NIC number exists
        if (customerRepository.existsByNicNumber(request.getNicNumber())) {
            throw new RuntimeException("Customer with NIC number " + request.getNicNumber() + " already exists");
        }

        // Create main customer
        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setNicNumber(request.getNicNumber());
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());

        // Handle mobile numbers
        if (request.getMobileNumbers() != null) {
            List<MobileNumber> mobileNumbers = new ArrayList<>();
            for (MobileNumberCreateRequest mobileRequest : request.getMobileNumbers()) {
                MobileNumber mobileNumber = new MobileNumber();
                mobileNumber.setNumber(mobileRequest.getNumber());
                mobileNumber.setCustomer(customer);
                mobileNumbers.add(mobileNumber);
            }
            customer.setMobileNumbers(mobileNumbers);
        }

        // Handle addresses
        if (request.getAddresses() != null) {
            List<Address> addresses = new ArrayList<>();
            for (AddressCreateRequest addressRequest : request.getAddresses()) {
                Address address = new Address();
                address.setAddressLine1(addressRequest.getAddressLine1());
                address.setAddressLine2(addressRequest.getAddressLine2());
                address.setCustomer(customer);

                // Handle city and country
                if (addressRequest.getCity() != null) {
                    CityCreateRequest cityRequest = addressRequest.getCity();
                    
                    // Check if country exists
                    Country country;
                    if (cityRequest.getCountry() != null) {
                        CountryCreateRequest countryRequest = cityRequest.getCountry();
                        if (countryRequest.getCode() == null || countryRequest.getCode().trim().isEmpty()) {
                            throw new RuntimeException("Country code is required for city: " + cityRequest.getName());
                        }
                        if (countryRequest.getName() == null || countryRequest.getName().trim().isEmpty()) {
                            throw new RuntimeException("Country name is required for city: " + cityRequest.getName());
                        }
                        
                        Optional<Country> existingCountry = countryRepository.findByCode(countryRequest.getCode());
                        
                        if (existingCountry.isPresent()) {
                            country = existingCountry.get();
                        } else {
                            country = new Country();
                            country.setName(countryRequest.getName());
                            country.setCode(countryRequest.getCode());
                            country = countryRepository.save(country);
                        }
                    } else {
                        throw new RuntimeException("Country information is required for city: " + cityRequest.getName());
                    }

                    // Check if city exists
                    Optional<City> existingCity = cityRepository.findByNameAndCountryId(cityRequest.getName(), country.getId());
                    City city;
                    if (existingCity.isPresent()) {
                        city = existingCity.get();
                    } else {
                        city = new City();
                        city.setName(cityRequest.getName());
                        city.setCountry(country);
                        city = cityRepository.save(city);
                    }
                    
                    address.setCity(city);
                } else {
                    throw new RuntimeException("City information is required for address");
                }

                addresses.add(address);
            }
            customer.setAddresses(addresses);
        }

        // Save the main customer first to get an ID
        customer = customerRepository.save(customer);

        // Handle family members
        if (request.getFamilyMembers() != null) {
            for (FamilyMemberCreateRequest memberRequest : request.getFamilyMembers()) {
                // Check if family member with same NIC number exists
                if (customerRepository.existsByNicNumber(memberRequest.getNicNumber())) {
                    throw new RuntimeException("Family member with NIC number " + memberRequest.getNicNumber() + " already exists");
                }

                // Create family member as a new customer
                Customer familyMember = new Customer();
                familyMember.setName(memberRequest.getName());
                familyMember.setDateOfBirth(memberRequest.getDateOfBirth());
                familyMember.setNicNumber(memberRequest.getNicNumber());
                familyMember.setCreatedAt(LocalDateTime.now());
                familyMember.setUpdatedAt(LocalDateTime.now());

                // Save the family member
                familyMember = customerRepository.save(familyMember);

                // Add the family relationship
                customer.addFamilyMember(familyMember);
            }
        }

        // Save the final customer with all relationships
        return customerRepository.save(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        
        // Load addresses
        List<Customer> customersWithAddresses = customerRepository.findAllWithAddresses();
        customers.forEach(customer -> {
            customersWithAddresses.stream()
                .filter(c -> c.getId().equals(customer.getId()))
                .findFirst()
                .ifPresent(c -> customer.setAddresses(c.getAddresses()));
        });
        
        // Load mobile numbers
        List<Customer> customersWithMobileNumbers = customerRepository.findAllWithMobileNumbers();
        customers.forEach(customer -> {
            customersWithMobileNumbers.stream()
                .filter(c -> c.getId().equals(customer.getId()))
                .findFirst()
                .ifPresent(c -> customer.setMobileNumbers(c.getMobileNumbers()));
        });
        
        // Load family members
        List<Customer> customersWithFamilyMembers = customerRepository.findAllWithFamilyMembers();
        customers.forEach(customer -> {
            customersWithFamilyMembers.stream()
                .filter(c -> c.getId().equals(customer.getId()))
                .findFirst()
                .ifPresent(c -> customer.setFamilyMembers(c.getFamilyMembers()));
        });
        
        return customers;
    }

    @Override
    @Transactional(readOnly = true)
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));
    }

    @Override
    @Transactional
    public Customer updateCustomer(Long id, CustomerCreateRequest request) {
        Customer customer = getCustomerById(id);
        customer.setName(request.getName());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setNicNumber(request.getNicNumber());
        customer.setUpdatedAt(LocalDateTime.now());
        return customerRepository.save(customer);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new EntityNotFoundException("Customer not found with id: " + id);
        }
        customerRepository.deleteById(id);
    }

    @Override
    @Transactional
    public List<Customer> bulkCreateCustomers(MultipartFile file) {
        List<Customer> customers = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row
                
                Customer customer = new Customer();
                customer.setName(getCellValueAsString(row.getCell(0)));
                customer.setDateOfBirth(LocalDate.parse(getCellValueAsString(row.getCell(1))));
                customer.setNicNumber(getCellValueAsString(row.getCell(2)));
                
                if (!customerRepository.existsByNicNumber(customer.getNicNumber())) {
                    customers.add(customerRepository.save(customer));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing Excel file", e);
        }
        return customers;
    }

    @Override
    @Transactional
    public List<Customer> bulkUpdateCustomers(MultipartFile file) {
        List<Customer> updatedCustomers = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row
                
                String nicNumber = getCellValueAsString(row.getCell(2));
                Customer existingCustomer = customerRepository.findByNicNumber(nicNumber)
                        .orElseThrow(() -> new EntityNotFoundException("Customer not found with NIC: " + nicNumber));
                
                existingCustomer.setName(getCellValueAsString(row.getCell(0)));
                existingCustomer.setDateOfBirth(LocalDate.parse(getCellValueAsString(row.getCell(1))));
                
                updatedCustomers.add(customerRepository.save(existingCustomer));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing Excel file", e);
        }
        return updatedCustomers;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());
            default:
                return "";
        }
    }
} 