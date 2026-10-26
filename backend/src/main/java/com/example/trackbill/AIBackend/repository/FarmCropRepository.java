package com.example.trackbill.AIBackend.repository;
import com.example.trackbill.AIBackend.model.FarmCrop;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface FarmCropRepository extends JpaRepository<FarmCrop,Long> {
 List<FarmCrop> findAllByFarmerIdOrderByCreatedAtDesc(Long farmerId);
 Optional<FarmCrop> findByIdAndFarmerId(Long id, Long farmerId);
}
