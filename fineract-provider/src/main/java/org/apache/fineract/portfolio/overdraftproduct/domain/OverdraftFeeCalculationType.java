package org.apache.fineract.portfolio.overdraftproduct.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Locale;

public enum OverdraftFeeCalculationType {
    FLAT,
    PERCENTAGE,
    TIERED;

    @JsonCreator
    public static OverdraftFeeCalculationType fromValue(String value) {
        if (value == null) {
            return null;
        }
        return Arrays.stream(values()).filter(type -> type.name().equalsIgnoreCase(value)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown overdraft fee calculation type: " + value));
    }

    @JsonValue
    public String toValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}
