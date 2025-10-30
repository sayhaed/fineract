package org.apache.fineract.portfolio.overdraftproduct.serialization;

import java.util.List;
import lombok.Builder;
import lombok.Value;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;

@Value
@Builder
public class OverdraftProductValidationResult {

    boolean valid;
    List<ApiParameterError> errors;
}
