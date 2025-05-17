package com.example.customerapi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "family_members")
public class FamilyMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;
    
    @NotBlank(message = "NIC number is required")
    @Pattern(regexp = "^[0-9]{9}[VX]$", message = "NIC number must be 9 digits followed by V or X")
    @Column(unique = true)
    private String nicNumber;
    
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getNicNumber() {
        return nicNumber;
    }

    public void setNicNumber(String nicNumber) {
        this.nicNumber = nicNumber;
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
} 