package com.example.customerapi.repository;

import com.example.customerapi.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByNicNumber(String nicNumber);
    
    boolean existsByNicNumber(String nicNumber);
    
    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.mobileNumbers LEFT JOIN FETCH c.addresses WHERE c.id = ?1")
    Optional<Customer> findByIdWithDetails(Long id);
    
    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.addresses")
    List<Customer> findAllWithAddresses();
    
    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.mobileNumbers")
    List<Customer> findAllWithMobileNumbers();
    
    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.familyMembers")
    List<Customer> findAllWithFamilyMembers();
} 