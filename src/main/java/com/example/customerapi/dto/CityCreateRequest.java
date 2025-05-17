package com.example.customerapi.dto;

import lombok.Data;

@Data
public class CityCreateRequest {
    private String name;
    private CountryCreateRequest country;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CountryCreateRequest getCountry() {
        return country;
    }

    public void setCountry(CountryCreateRequest country) {
        this.country = country;
    }
} 