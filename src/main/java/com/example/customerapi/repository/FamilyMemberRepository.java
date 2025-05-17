package com.example.customerapi.repository;

import com.example.customerapi.entity.FamilyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {
    boolean existsByNicNumber(String nicNumber);
} 