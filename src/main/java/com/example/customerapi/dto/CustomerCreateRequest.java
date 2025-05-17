package com.example.customerapi.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class CustomerCreateRequest {
    private String name;
    private LocalDate dateOfBirth;
    private String nicNumber;
    private List<MobileNumberCreateRequest> mobileNumbers;
    private List<AddressCreateRequest> addresses;
    private List<FamilyMemberCreateRequest> familyMembers;

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

    public List<MobileNumberCreateRequest> getMobileNumbers() {
        return mobileNumbers;
    }

    public void setMobileNumbers(List<MobileNumberCreateRequest> mobileNumbers) {
        this.mobileNumbers = mobileNumbers;
    }

    public List<AddressCreateRequest> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<AddressCreateRequest> addresses) {
        this.addresses = addresses;
    }

    public List<FamilyMemberCreateRequest> getFamilyMembers() {
        return familyMembers;
    }

    public void setFamilyMembers(List<FamilyMemberCreateRequest> familyMembers) {
        this.familyMembers = familyMembers;
    }
} 