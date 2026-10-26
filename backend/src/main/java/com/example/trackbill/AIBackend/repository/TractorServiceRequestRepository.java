package com.example.trackbill.AIBackend.repository;
import com.example.trackbill.AIBackend.model.TractorServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface TractorServiceRequestRepository extends JpaRepository<TractorServiceRequest,Long> {
 List<TractorServiceRequest> findAllByFarmerIdOrderByCreatedAtDesc(Long farmerId);
 Optional<TractorServiceRequest> findByIdAndFarmerId(Long id,Long farmerId);
 List<TractorServiceRequest> findAllByStatusOrderByCreatedAtAsc(String status);
}
