package com.example.customerapi.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class FamilyMemberCreateRequest {
    private String name;
    private LocalDate dateOfBirth;
    private String nicNumber;

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
} 