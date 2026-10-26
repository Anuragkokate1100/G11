package com.example.trackbill.AIBackend.repository;

import com.example.trackbill.AIBackend.model.TractorOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TractorOwnerRepository extends JpaRepository<TractorOwner, Long> {
    Optional<TractorOwner> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByTractorNumber(String tractorNumber);
}
