package org.apache.fineract.portfolio.overdraftproduct.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Locale;

public enum OverdraftProductStatus {
    DRAFT,
    ACTIVE,
    RETIRED;

    @JsonCreator
    public static OverdraftProductStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        return Arrays.stream(values()).filter(status -> status.name().equalsIgnoreCase(value)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown overdraft product status: " + value));
    }

    @JsonValue
    public String toValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public boolean isDraft() {
        return this == DRAFT;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isRetired() {
        return this == RETIRED;
    }
}
