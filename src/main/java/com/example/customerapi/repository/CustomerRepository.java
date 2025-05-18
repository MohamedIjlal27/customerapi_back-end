package com.example.customerapi.repository;

import com.example.customerapi.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByNicNumber(String nicNumber);
    
    boolean existsByNicNumber(String nicNumber);
    
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Customer c JOIN c.mobileNumbers m WHERE m.number = ?1")
    boolean existsByMobileNumber(String number);
    
    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.mobileNumbers LEFT JOIN FETCH c.addresses WHERE c.id = ?1")
    Optional<Customer> findByIdWithDetails(Long id);
    
    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.addresses")
    List<Customer> findAllWithAddresses();
    
    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.mobileNumbers")
    List<Customer> findAllWithMobileNumbers();
    
    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.familyMembers")
    List<Customer> findAllWithFamilyMembers();

    @Query(value = "SELECT c FROM Customer c ORDER BY c.id",
           countQuery = "SELECT COUNT(c) FROM Customer c")
    Page<Customer> findAllWithPagination(Pageable pageable);

    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.addresses WHERE c.id IN :ids")
    List<Customer> findAllWithAddressesByIds(@Param("ids") List<Long> ids);

    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.mobileNumbers WHERE c.id IN :ids")
    List<Customer> findAllWithMobileNumbersByIds(@Param("ids") List<Long> ids);

    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.familyMembers WHERE c.id IN :ids")
    List<Customer> findAllWithFamilyMembersByIds(@Param("ids") List<Long> ids);
} 