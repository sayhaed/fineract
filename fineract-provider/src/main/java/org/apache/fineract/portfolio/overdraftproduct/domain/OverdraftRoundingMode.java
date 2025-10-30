package org.apache.fineract.portfolio.overdraftproduct.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Locale;

public enum OverdraftRoundingMode {
    HALF_EVEN(RoundingMode.HALF_EVEN),
    HALF_UP(RoundingMode.HALF_UP),
    HALF_DOWN(RoundingMode.HALF_DOWN),
    DOWN(RoundingMode.DOWN),
    UP(RoundingMode.UP);

    private final RoundingMode roundingMode;

    OverdraftRoundingMode(RoundingMode roundingMode) {
        this.roundingMode = roundingMode;
    }

    public RoundingMode getRoundingMode() {
        return roundingMode;
    }

    @JsonCreator
    public static OverdraftRoundingMode fromValue(String value) {
        if (value == null) {
            return null;
        }
        return Arrays.stream(values()).filter(mode -> mode.name().equalsIgnoreCase(value)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown overdraft rounding mode: " + value));
    }

    @JsonValue
    public String toValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}
