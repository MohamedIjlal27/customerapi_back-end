package com.example.customerapi.repository;

import com.example.customerapi.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    List<City> findByCountryId(Long countryId);
    Optional<City> findByNameAndCountryId(String name, Long countryId);
} 