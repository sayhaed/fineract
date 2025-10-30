package org.apache.fineract.portfolio.overdraftproduct.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftProductVersion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OverdraftProductVersionRepository extends JpaRepository<OverdraftProductVersion, Long> {

    List<OverdraftProductVersion> findByProductIdOrderByEffectiveFromAsc(Long productId);

    Optional<OverdraftProductVersion> findFirstByProductIdAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqual(Long productId,
            LocalDate from, LocalDate to);
}
