package com.example.trackbill.AIBackend.repository;
import com.example.trackbill.AIBackend.model.FarmerNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface FarmerNotificationRepository extends JpaRepository<FarmerNotification,Long> {
 List<FarmerNotification> findAllByFarmerIdOrderByCreatedAtDesc(Long farmerId);
 Optional<FarmerNotification> findByIdAndFarmerId(Long id,Long farmerId);
 long countByFarmerIdAndReadFalse(Long farmerId);
 List<FarmerNotification> findAllByFarmerIdAndReadFalse(Long farmerId);
}
