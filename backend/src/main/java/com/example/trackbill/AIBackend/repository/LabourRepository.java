package com.example.trackbill.AIBackend.repository;

import com.example.trackbill.AIBackend.model.Labour;
import com.example.trackbill.AIBackend.model.LabourCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface LabourRepository extends JpaRepository<Labour, Long> {

    List<Labour> findByTractorOwnerIdOrderByNameAsc(Long ownerId);

    Optional<Labour> findByIdAndTractorOwnerId(Long id, Long ownerId);

    long countByTractorOwnerId(Long ownerId);

    long countByTractorOwnerIdAndCategory(Long ownerId, LabourCategory category);

    @Query("SELECT COALESCE(SUM(l.advanceMoney), 0) FROM Labour l WHERE l.tractorOwner.id = :ownerId")
    BigDecimal sumAdvanceMoneyByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT l FROM Labour l WHERE l.tractorOwner.id = :ownerId AND (" +
           "LOWER(l.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.village) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "l.phone LIKE CONCAT('%', :query, '%')) " +
           "ORDER BY l.name ASC")
    List<Labour> searchLabour(@Param("ownerId") Long ownerId, @Param("query") String query);

    @Query("SELECT l.category, COUNT(l) FROM Labour l WHERE l.tractorOwner.id = :ownerId GROUP BY l.category")
    List<Object[]> getCategoryCountsByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT l.type, COUNT(l) FROM Labour l WHERE l.tractorOwner.id = :ownerId GROUP BY l.type")
    List<Object[]> getTypeCountsByOwnerId(@Param("ownerId") Long ownerId);
}
