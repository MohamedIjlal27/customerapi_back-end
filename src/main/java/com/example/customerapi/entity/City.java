package com.example.customerapi.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "cities")
@JsonIgnoreProperties({"id", "addresses"})
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "City name is required")
    private String name;

    @JsonBackReference
    @NotNull(message = "Country is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    @JsonIgnoreProperties("cities")
    private Country country;

    @JsonManagedReference
    @OneToMany(mappedBy = "city", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("city")
    private List<Address> addresses = new ArrayList<>();

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

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }
} 