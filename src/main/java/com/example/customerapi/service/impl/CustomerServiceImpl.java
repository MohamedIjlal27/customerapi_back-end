package com.example.customerapi.service.impl;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
        return getAllCustomersPaginated(0, 1000); // Default to first 1000 records
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> getAllCustomersPaginated(int page, int size) {
        // Create Pageable object for pagination
        Pageable pageable = PageRequest.of(page, size);
        
        // Get paginated customers
        Page<Customer> customerPage = customerRepository.findAllWithPagination(pageable);
        List<Customer> customers = customerPage.getContent();
        
        if (customers.isEmpty()) {
            return customers;
        }

        // Get IDs for batch loading
        List<Long> customerIds = customers.stream()
            .map(Customer::getId)
            .collect(Collectors.toList());

        // Load addresses in batch
        List<Customer> customersWithAddresses = customerRepository.findAllWithAddressesByIds(customerIds);
        customers.forEach(customer -> {
            customersWithAddresses.stream()
                .filter(c -> c.getId().equals(customer.getId()))
                .findFirst()
                .ifPresent(c -> customer.setAddresses(c.getAddresses()));
        });
        
        // Load mobile numbers in batch
        List<Customer> customersWithMobileNumbers = customerRepository.findAllWithMobileNumbersByIds(customerIds);
        customers.forEach(customer -> {
            customersWithMobileNumbers.stream()
                .filter(c -> c.getId().equals(customer.getId()))
                .findFirst()
                .ifPresent(c -> customer.setMobileNumbers(c.getMobileNumbers()));
        });
        
        // Load family members in batch
        List<Customer> customersWithFamilyMembers = customerRepository.findAllWithFamilyMembersByIds(customerIds);
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
    public long getTotalCustomerCount() {
        return customerRepository.count();
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
        
        // Update basic information
        customer.setName(request.getName());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setNicNumber(request.getNicNumber());
        customer.setUpdatedAt(LocalDateTime.now());

        // Handle mobile numbers
        if (request.getMobileNumbers() != null) {
            // Get existing mobile numbers
            List<String> existingNumbers = customer.getMobileNumbers().stream()
                .map(MobileNumber::getNumber)
                .collect(Collectors.toList());
            
            // Clear existing mobile numbers
            customer.getMobileNumbers().clear();
            
            // Add new mobile numbers
            for (MobileNumberCreateRequest mobileRequest : request.getMobileNumbers()) {
                // Check if the number already exists for another customer
                if (!existingNumbers.contains(mobileRequest.getNumber()) && 
                    customerRepository.existsByMobileNumber(mobileRequest.getNumber())) {
                    throw new RuntimeException("Mobile number " + mobileRequest.getNumber() + " is already registered to another customer");
                }
                
                MobileNumber mobileNumber = new MobileNumber();
                mobileNumber.setNumber(mobileRequest.getNumber());
                mobileNumber.setCustomer(customer);
                customer.getMobileNumbers().add(mobileNumber);
            }
        }

        // Handle addresses
        if (request.getAddresses() != null) {
            // Clear existing addresses
            customer.getAddresses().clear();
            
            // Add new addresses
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

                customer.getAddresses().add(address);
            }
        }

        // Handle family members
        if (request.getFamilyMembers() != null) {
            // Remove existing family member relationships
            List<Customer> existingFamilyMembers = new ArrayList<>(customer.getFamilyMembers());
            for (Customer familyMember : existingFamilyMembers) {
                customer.removeFamilyMember(familyMember);
            }
            
            // Add new family members
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

        // Save and return the updated customer
        return customerRepository.save(customer);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));
        
        // Remove family member relationships
        List<Customer> familyMembers = new ArrayList<>(customer.getFamilyMembers());
        for (Customer familyMember : familyMembers) {
            customer.removeFamilyMember(familyMember);
        }
        
        // Remove relationships where this customer is a family member of others
        List<Customer> familyOf = new ArrayList<>(customer.getFamilyOf());
        for (Customer parent : familyOf) {
            parent.removeFamilyMember(customer);
        }
        
        // Save the customer to update the relationships
        customerRepository.save(customer);
        
        // Now we can safely delete the customer
        customerRepository.deleteById(id);
    }

    @Override
    @Transactional
    public List<Customer> bulkCreateCustomers(MultipartFile file) {
        List<Customer> customers = new ArrayList<>();
        int batchSize = 1000; // Process 1000 records at a time
        int totalProcessed = 0;
        int totalCreated = 0;
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int totalRows = sheet.getPhysicalNumberOfRows();
            
            // Process in batches
            for (int rowIndex = 1; rowIndex < totalRows; rowIndex += batchSize) {
                int endIndex = Math.min(rowIndex + batchSize, totalRows);
                List<Customer> batchCustomers = new ArrayList<>();
                
                for (int i = rowIndex; i < endIndex; i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;
                    
                    try {
                        Customer customer = processCustomerRow(row);
                        if (customer != null && !customerRepository.existsByNicNumber(customer.getNicNumber())) {
                            batchCustomers.add(customer);
                        }
                        totalProcessed++;
                    } catch (Exception e) {
                        // Log error but continue processing
                        System.err.println("Error processing row " + i + ": " + e.getMessage());
                    }
                }
                
                // Save batch
                if (!batchCustomers.isEmpty()) {
                    List<Customer> savedCustomers = customerRepository.saveAll(batchCustomers);
                    customers.addAll(savedCustomers);
                    totalCreated += savedCustomers.size();
                }
                
                // Clear batch list to free memory
                batchCustomers.clear();
                
                // Log progress
                System.out.println("Processed " + totalProcessed + " of " + totalRows + " records. Created " + totalCreated + " customers.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing Excel file", e);
        }
        
        return customers;
    }
    
    private Customer processCustomerRow(Row row) {
        Customer customer = new Customer();
        
        // Set basic information
        customer.setName(getCellValueAsString(row.getCell(0)));
        
        // Handle date of birth
        String dateStr = getCellValueAsString(row.getCell(1));
        try {
            LocalDate dateOfBirth = LocalDate.parse(dateStr);
            if (dateOfBirth.isAfter(LocalDate.now())) {
                throw new RuntimeException("Date of birth must be in the past: " + dateStr);
            }
            customer.setDateOfBirth(dateOfBirth);
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format for date of birth: " + dateStr + ". Expected format: YYYY-MM-DD", e);
        }
        
        customer.setNicNumber(getCellValueAsString(row.getCell(2)));
        
        // Handle mobile number
        String mobileNumber = getCellValueAsString(row.getCell(3));
        if (mobileNumber != null && !mobileNumber.trim().isEmpty()) {
            MobileNumber mobile = new MobileNumber();
            mobile.setNumber(mobileNumber);
            mobile.setCustomer(customer);
            customer.getMobileNumbers().add(mobile);
        }
        
        // Handle address
        String addressLine1 = getCellValueAsString(row.getCell(4));
        String addressLine2 = getCellValueAsString(row.getCell(5));
        String cityName = getCellValueAsString(row.getCell(6));
        String countryName = getCellValueAsString(row.getCell(7));
        String countryCode = getCellValueAsString(row.getCell(8));
        
        if (addressLine1 != null && !addressLine1.trim().isEmpty() && 
            cityName != null && !cityName.trim().isEmpty() && 
            countryName != null && !countryName.trim().isEmpty() && 
            countryCode != null && !countryCode.trim().isEmpty()) {
            
            // Create or get country
            Country country;
            Optional<Country> existingCountry = countryRepository.findByCode(countryCode);
            if (existingCountry.isPresent()) {
                country = existingCountry.get();
            } else {
                country = new Country();
                country.setName(countryName);
                country.setCode(countryCode);
                country = countryRepository.save(country);
            }
            
            // Create or get city
            City city;
            Optional<City> existingCity = cityRepository.findByNameAndCountryId(cityName, country.getId());
            if (existingCity.isPresent()) {
                city = existingCity.get();
            } else {
                city = new City();
                city.setName(cityName);
                city.setCountry(country);
                city = cityRepository.save(city);
            }
            
            // Create address
            Address address = new Address();
            address.setAddressLine1(addressLine1);
            address.setAddressLine2(addressLine2);
            address.setCity(city);
            address.setCustomer(customer);
            customer.getAddresses().add(address);
        }
        
        // Handle family member
        String familyMemberName = getCellValueAsString(row.getCell(9));
        String familyMemberDob = getCellValueAsString(row.getCell(10));
        String familyMemberNic = getCellValueAsString(row.getCell(11));
        
        if (familyMemberName != null && !familyMemberName.trim().isEmpty() && 
            familyMemberDob != null && !familyMemberDob.trim().isEmpty() && 
            familyMemberNic != null && !familyMemberNic.trim().isEmpty()) {
            
            // Create family member
            Customer familyMember = new Customer();
            familyMember.setName(familyMemberName);
            try {
                LocalDate familyMemberDateOfBirth = LocalDate.parse(familyMemberDob);
                if (familyMemberDateOfBirth.isAfter(LocalDate.now())) {
                    throw new RuntimeException("Family member date of birth must be in the past: " + familyMemberDob);
                }
                familyMember.setDateOfBirth(familyMemberDateOfBirth);
            } catch (Exception e) {
                throw new RuntimeException("Invalid date format for family member date of birth: " + familyMemberDob + ". Expected format: YYYY-MM-DD", e);
            }
            familyMember.setNicNumber(familyMemberNic);
            familyMember.setCreatedAt(LocalDateTime.now());
            familyMember.setUpdatedAt(LocalDateTime.now());
            
            // Save family member first
            familyMember = customerRepository.save(familyMember);
            
            // Add family relationship
            customer.addFamilyMember(familyMember);
        }
        
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        
        return customer;
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