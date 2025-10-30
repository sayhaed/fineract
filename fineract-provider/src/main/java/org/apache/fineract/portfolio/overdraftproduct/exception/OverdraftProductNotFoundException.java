package org.apache.fineract.portfolio.overdraftproduct.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class OverdraftProductNotFoundException extends AbstractPlatformResourceNotFoundException {

    public OverdraftProductNotFoundException(Long id) {
        super("error.msg.overdraftproduct.not.found", "Overdraft product with identifier %s does not exist", id);
    }
}
