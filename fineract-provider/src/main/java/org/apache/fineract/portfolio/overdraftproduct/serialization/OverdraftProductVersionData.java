package org.apache.fineract.portfolio.overdraftproduct.serialization;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OverdraftProductVersionData {

    Long id;
    String versionLabel;
    LocalDate effectiveFrom;
    LocalDate effectiveTo;
    String notes;
}
