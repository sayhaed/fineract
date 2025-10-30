package org.apache.fineract.portfolio.overdraftproduct.accounting;

import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftProduct;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftProductAccounting;
import org.apache.fineract.portfolio.overdraftproduct.repository.OverdraftProductRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Transactional
public class OverdraftAccountingMapper {

    private final OverdraftProductRepository productRepository;

    @Transactional(Transactional.TxType.SUPPORTS)
    public Optional<GLAccount> receivableAccount(Long productId) {
        return getAccounting(productId).map(OverdraftProductAccounting::getReceivableAccount);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public Optional<GLAccount> interestIncomeAccount(Long productId) {
        return getAccounting(productId).map(OverdraftProductAccounting::getInterestIncomeAccount);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public Optional<GLAccount> feeIncomeAccount(Long productId) {
        return getAccounting(productId).map(OverdraftProductAccounting::getFeeIncomeAccount);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public Optional<GLAccount> suspenseAccount(Long productId) {
        return getAccounting(productId).map(OverdraftProductAccounting::getSuspenseAccount);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public Optional<GLAccount> writeOffAccount(Long productId) {
        return getAccounting(productId).map(OverdraftProductAccounting::getWriteOffAccount);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public Optional<GLAccount> penaltyIncomeAccount(Long productId) {
        return getAccounting(productId).map(OverdraftProductAccounting::getPenaltyIncomeAccount);
    }

    private Optional<OverdraftProductAccounting> getAccounting(Long productId) {
        if (productId == null) {
            return Optional.empty();
        }
        return productRepository.findById(productId).map(OverdraftProduct::getAccounting);
    }
}
