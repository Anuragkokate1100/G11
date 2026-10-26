package com.example.trackbill.AIBackend.repository;

import com.example.trackbill.AIBackend.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByTractorOwnerIdOrderByTripDateDesc(Long ownerId);

    Optional<Trip> findByIdAndTractorOwnerId(Long id, Long ownerId);

    List<Trip> findByTractorOwnerIdAndIsBilledFalseOrderByTripDateDesc(Long ownerId);

    List<Trip> findByTractorOwnerIdAndTripDateBetweenOrderByTripDateDesc(Long ownerId, LocalDate start, LocalDate end);

    List<Trip> findByTractorOwnerIdAndIdIn(Long ownerId, List<Long> ids);

    long countByTractorOwnerId(Long ownerId);

    @Query("SELECT COUNT(t) FROM Trip t WHERE t.tractorOwner.id = :ownerId AND t.tripDate BETWEEN :start AND :end")
    long countByTractorOwnerIdAndTripDateBetween(@Param("ownerId") Long ownerId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(t.weight), 0) FROM Trip t WHERE t.tractorOwner.id = :ownerId")
    BigDecimal sumWeightByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT COALESCE(SUM(t.weight), 0) FROM Trip t WHERE t.tractorOwner.id = :ownerId AND t.tripDate BETWEEN :start AND :end")
    BigDecimal sumWeightByOwnerIdAndTripDateBetween(@Param("ownerId") Long ownerId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(t.dieselRequired), 0) FROM Trip t WHERE t.tractorOwner.id = :ownerId")
    BigDecimal sumDieselByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT COALESCE(SUM(t.dieselRequired), 0) FROM Trip t WHERE t.tractorOwner.id = :ownerId AND t.tripDate BETWEEN :start AND :end")
    BigDecimal sumDieselByOwnerIdAndTripDateBetween(@Param("ownerId") Long ownerId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT t FROM Trip t WHERE t.tractorOwner.id = :ownerId AND (" +
           "LOWER(t.sugarcaneOwnerName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.millName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.tractorNumber) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY t.tripDate DESC")
    List<Trip> searchTrips(@Param("ownerId") Long ownerId, @Param("query") String query);

    @Query("SELECT t.millName, SUM(t.weight) FROM Trip t WHERE t.tractorOwner.id = :ownerId GROUP BY t.millName ORDER BY SUM(t.weight) DESC")
    List<Object[]> getMillWeightBreakdown(@Param("ownerId") Long ownerId);

    @Query("SELECT FUNCTION('YEAR', t.tripDate), FUNCTION('MONTH', t.tripDate), SUM(t.weight), COUNT(t) " +
           "FROM Trip t WHERE t.tractorOwner.id = :ownerId " +
           "GROUP BY FUNCTION('YEAR', t.tripDate), FUNCTION('MONTH', t.tripDate) " +
           "ORDER BY FUNCTION('YEAR', t.tripDate) ASC, FUNCTION('MONTH', t.tripDate) ASC")
    List<Object[]> getMonthlyTripTrends(@Param("ownerId") Long ownerId);
}
