package org.apache.fineract.portfolio.overdraftproduct.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Locale;

public enum OverdraftInterestMethod {
    DAILY_BALANCE,
    MINIMUM_BALANCE,
    AVERAGE_DAILY_BALANCE;

    @JsonCreator
    public static OverdraftInterestMethod fromValue(String value) {
        if (value == null) {
            return null;
        }
        return Arrays.stream(values()).filter(method -> method.name().equalsIgnoreCase(value)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown overdraft interest method: " + value));
    }

    @JsonValue
    public String toValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}
