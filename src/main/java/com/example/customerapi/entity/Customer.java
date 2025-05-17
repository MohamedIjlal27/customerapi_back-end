package com.example.customerapi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotNull
    @Past
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @NotBlank
    @Column(name = "nic_number", nullable = false, unique = true)
    private String nicNumber;

    @JsonManagedReference
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MobileNumber> mobileNumbers = new ArrayList<>();

    @JsonManagedReference
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "customer_family_members",
        joinColumns = @JoinColumn(name = "customer_id"),
        inverseJoinColumns = @JoinColumn(name = "family_member_id")
    )
    @JsonManagedReference
    private List<Customer> familyMembers = new ArrayList<>();

    @ManyToMany(mappedBy = "familyMembers")
    @JsonBackReference
    private List<Customer> familyOf = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods for managing family relationships
    public void addFamilyMember(Customer familyMember) {
        familyMembers.add(familyMember);
        familyMember.getFamilyOf().add(this);
    }

    public void removeFamilyMember(Customer familyMember) {
        familyMembers.remove(familyMember);
        familyMember.getFamilyOf().remove(this);
    }

    // Existing getters and setters
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

    public List<MobileNumber> getMobileNumbers() {
        return mobileNumbers;
    }

    public void setMobileNumbers(List<MobileNumber> mobileNumbers) {
        this.mobileNumbers = mobileNumbers;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public List<Customer> getFamilyMembers() {
        return familyMembers;
    }

    public void setFamilyMembers(List<Customer> familyMembers) {
        this.familyMembers = familyMembers;
    }

    public List<Customer> getFamilyOf() {
        return familyOf;
    }

    public void setFamilyOf(List<Customer> familyOf) {
        this.familyOf = familyOf;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
} 