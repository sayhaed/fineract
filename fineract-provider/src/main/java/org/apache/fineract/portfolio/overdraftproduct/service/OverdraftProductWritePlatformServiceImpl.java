package org.apache.fineract.portfolio.overdraftproduct.service;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftProduct;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftProductStatus;
import org.apache.fineract.portfolio.overdraftproduct.exception.OverdraftProductLifecycleException;
import org.apache.fineract.portfolio.overdraftproduct.exception.OverdraftProductNotFoundException;
import org.apache.fineract.portfolio.overdraftproduct.repository.OverdraftProductRepository;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftFeeCommand;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftProductAccountingCommand;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftProductCommand;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftProductData;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftProductLifecycleCommand;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftProductMapper;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftUsageRuleCommand;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class OverdraftProductWritePlatformServiceImpl implements OverdraftProductWritePlatformService {

    private final OverdraftProductRepository productRepository;
    private final OverdraftProductMapper mapper;
    private final OverdraftProductValidationService validationService;

    @Override
    public OverdraftProductData createProduct(OverdraftProductCommand command) {
        validationService.validateForCreate(command);
        OverdraftProduct product = mapper.mapToNewEntity(command);
        OverdraftProduct saved = productRepository.save(product);
        return mapper.mapToData(saved);
    }

    @Override
    public OverdraftProductData updateProduct(Long id, OverdraftProductCommand command) {
        OverdraftProduct product = getProduct(id);
        ensureDraft(product);
        validationService.validateForUpdate(product, command);
        mapper.updateEntity(product, command);
        return mapper.mapToData(product);
    }

    @Override
    public OverdraftProductData activateProduct(Long id, OverdraftProductLifecycleCommand command) {
        OverdraftProduct product = getProduct(id);
        validationService.validateActivation(product, command);
        product.activate(resolveActionDate(command));
        return mapper.mapToData(product);
    }

    @Override
    public OverdraftProductData retireProduct(Long id, OverdraftProductLifecycleCommand command) {
        OverdraftProduct product = getProduct(id);
        validationService.validateRetirement(product, command);
        product.retire(resolveActionDate(command));
        return mapper.mapToData(product);
    }

    @Override
    public OverdraftProductData updateFees(Long id, List<OverdraftFeeCommand> fees) {
        OverdraftProduct product = getProduct(id);
        ensureNotRetired(product);
        validationService.validateFeesUpdate(fees);
        mapper.replaceFees(product, fees);
        return mapper.mapToData(product);
    }

    @Override
    public OverdraftProductData updateUsageRules(Long id, List<OverdraftUsageRuleCommand> rules) {
        OverdraftProduct product = getProduct(id);
        ensureNotRetired(product);
        validationService.validateUsageRulesUpdate(rules);
        mapper.replaceUsageRules(product, rules);
        return mapper.mapToData(product);
    }

    @Override
    public OverdraftProductData updateAccounting(Long id, OverdraftProductAccountingCommand accountingCommand) {
        OverdraftProduct product = getProduct(id);
        ensureNotRetired(product);
        validationService.validateAccountingUpdate(accountingCommand);
        mapper.updateAccounting(product, accountingCommand);
        return mapper.mapToData(product);
    }

    private OverdraftProduct getProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new OverdraftProductNotFoundException(id));
    }

    private void ensureDraft(OverdraftProduct product) {
        if (!product.getStatus().isDraft()) {
            throw new OverdraftProductLifecycleException("update.not.allowed",
                    "Only draft overdraft products can be modified", product.getStatus());
        }
    }

    private void ensureNotRetired(OverdraftProduct product) {
        if (product.getStatus().isRetired()) {
            throw new OverdraftProductLifecycleException("update.not.allowed",
                    "Retired overdraft products cannot be modified", OverdraftProductStatus.RETIRED);
        }
    }

    private LocalDate resolveActionDate(OverdraftProductLifecycleCommand command) {
        if (command != null && command.getActionDate() != null) {
            return command.getActionDate();
        }
        return DateUtils.getBusinessLocalDate();
    }
}
