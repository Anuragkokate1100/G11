package com.example.trackbill.AIBackend.repository;
import com.example.trackbill.AIBackend.model.FarmerBillLink;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface FarmerBillLinkRepository extends JpaRepository<FarmerBillLink,Long> {
 List<FarmerBillLink> findAllByFarmerIdOrderByCreatedAtDesc(Long farmerId);
 Optional<FarmerBillLink> findByBillingId(Long billingId);
}
