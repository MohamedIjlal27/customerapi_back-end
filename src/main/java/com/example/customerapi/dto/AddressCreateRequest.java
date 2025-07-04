package com.example.customerapi.dto;

import lombok.Data;

@Data
public class AddressCreateRequest {
    private String addressLine1;
    private String addressLine2;
    private CityCreateRequest city;

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public CityCreateRequest getCity() {
        return city;
    }

    public void setCity(CityCreateRequest city) {
        this.city = city;
    }
} 