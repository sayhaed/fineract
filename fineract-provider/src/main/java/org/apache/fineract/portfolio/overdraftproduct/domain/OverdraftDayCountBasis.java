package org.apache.fineract.portfolio.overdraftproduct.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Locale;

public enum OverdraftDayCountBasis {
    ACTUAL_ACTUAL,
    ACTUAL_360,
    ACTUAL_365,
    THIRTY_360;

    @JsonCreator
    public static OverdraftDayCountBasis fromValue(String value) {
        if (value == null) {
            return null;
        }
        return Arrays.stream(values()).filter(basis -> basis.name().equalsIgnoreCase(value)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown overdraft day count basis: " + value));
    }

    @JsonValue
    public String toValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}
