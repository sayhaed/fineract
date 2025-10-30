package org.apache.fineract.portfolio.overdraftproduct.service;

import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftFeeCommand;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftProductAccountingCommand;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftProductCommand;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftProductData;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftProductLifecycleCommand;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftUsageRuleCommand;

public interface OverdraftProductWritePlatformService {

    OverdraftProductData createProduct(OverdraftProductCommand command);

    OverdraftProductData updateProduct(Long id, OverdraftProductCommand command);

    OverdraftProductData activateProduct(Long id, OverdraftProductLifecycleCommand command);

    OverdraftProductData retireProduct(Long id, OverdraftProductLifecycleCommand command);

    OverdraftProductData updateFees(Long id, List<OverdraftFeeCommand> fees);

    OverdraftProductData updateUsageRules(Long id, List<OverdraftUsageRuleCommand> rules);

    OverdraftProductData updateAccounting(Long id, OverdraftProductAccountingCommand accountingCommand);
}
