package org.apache.fineract.portfolio.overdraftproduct.service;

import java.util.List;
import java.util.Optional;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftProductStatus;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftProductData;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftProductValidationResult;

public interface OverdraftProductReadPlatformService {

    List<OverdraftProductData> retrieveAll(Optional<OverdraftProductStatus> status, Optional<String> currency);

    OverdraftProductData retrieveOne(Long id);

    OverdraftProductValidationResult validate(Long id);
}
