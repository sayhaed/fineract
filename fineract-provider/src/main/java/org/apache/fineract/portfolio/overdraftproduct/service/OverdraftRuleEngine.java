package org.apache.fineract.portfolio.overdraftproduct.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftProduct;
import org.apache.fineract.portfolio.overdraftproduct.exception.OverdraftProductNotFoundException;
import org.apache.fineract.portfolio.overdraftproduct.repository.OverdraftProductRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class OverdraftRuleEngine {

    private final OverdraftProductRepository productRepository;
    private final OverdraftProductValidationService validationService;

    @Transactional(Transactional.TxType.SUPPORTS)
    public void validateUsage(Long productId, OverdraftUsageValidationContext context) {
        if (productId == null) {
            return;
        }
        OverdraftProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new OverdraftProductNotFoundException(productId));
        validationService.validateUsage(product, context);
    }
}
