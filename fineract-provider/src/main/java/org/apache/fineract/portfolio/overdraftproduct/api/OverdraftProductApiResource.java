package org.apache.fineract.portfolio.overdraftproduct.api;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftProductStatus;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftFeeCommand;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftProductAccountingCommand;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftProductCommand;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftProductData;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftProductLifecycleCommand;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftProductValidationResult;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftUsageRuleCommand;
import org.apache.fineract.portfolio.overdraftproduct.service.OverdraftProductReadPlatformService;
import org.apache.fineract.portfolio.overdraftproduct.service.OverdraftProductWritePlatformService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/overdraft-products")
@Validated
public class OverdraftProductApiResource {

    private final OverdraftProductWritePlatformService writeService;
    private final OverdraftProductReadPlatformService readService;

    @PostMapping
    @PreAuthorize("hasAuthority('OVERDRAFT_PRODUCT_ADMIN')")
    public ResponseEntity<OverdraftProductData> create(@Valid @RequestBody OverdraftProductCommand command) {
        OverdraftProductData data = writeService.createProduct(command);
        return new ResponseEntity<>(data, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('OVERDRAFT_PRODUCT_ADMIN')")
    public ResponseEntity<OverdraftProductData> update(@PathVariable Long id,
            @Valid @RequestBody OverdraftProductCommand command) {
        return ResponseEntity.ok(writeService.updateProduct(id, command));
    }

    @PostMapping("/{id}:activate")
    @PreAuthorize("hasAuthority('OVERDRAFT_PRODUCT_ADMIN')")
    public ResponseEntity<OverdraftProductData> activate(@PathVariable Long id,
            @RequestBody(required = false) OverdraftProductLifecycleCommand command) {
        return ResponseEntity.ok(writeService.activateProduct(id, command));
    }

    @PostMapping("/{id}:retire")
    @PreAuthorize("hasAuthority('OVERDRAFT_PRODUCT_ADMIN')")
    public ResponseEntity<OverdraftProductData> retire(@PathVariable Long id,
            @RequestBody(required = false) OverdraftProductLifecycleCommand command) {
        return ResponseEntity.ok(writeService.retireProduct(id, command));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('OVERDRAFT_PRODUCT_ADMIN','OVERDRAFT_PRODUCT_VIEW')")
    public ResponseEntity<List<OverdraftProductData>> retrieveAll(@RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "currency", required = false) String currency) {
        Optional<OverdraftProductStatus> statusFilter = Optional.ofNullable(status).map(OverdraftProductStatus::fromValue);
        Optional<String> currencyFilter = Optional.ofNullable(currency);
        return ResponseEntity.ok(readService.retrieveAll(statusFilter, currencyFilter));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('OVERDRAFT_PRODUCT_ADMIN','OVERDRAFT_PRODUCT_VIEW')")
    public ResponseEntity<OverdraftProductData> retrieveOne(@PathVariable Long id) {
        return ResponseEntity.ok(readService.retrieveOne(id));
    }

    @GetMapping("/{id}/validate")
    @PreAuthorize("hasAnyAuthority('OVERDRAFT_PRODUCT_ADMIN','OVERDRAFT_PRODUCT_VIEW')")
    public ResponseEntity<OverdraftProductValidationResult> validate(@PathVariable Long id) {
        return ResponseEntity.ok(readService.validate(id));
    }

    @PostMapping("/{id}/fees")
    @PreAuthorize("hasAuthority('OVERDRAFT_PRODUCT_ADMIN')")
    public ResponseEntity<OverdraftProductData> updateFees(@PathVariable Long id,
            @Valid @RequestBody List<OverdraftFeeCommand> fees) {
        return ResponseEntity.ok(writeService.updateFees(id, fees));
    }

    @PostMapping("/{id}/usage-rules")
    @PreAuthorize("hasAuthority('OVERDRAFT_PRODUCT_ADMIN')")
    public ResponseEntity<OverdraftProductData> updateUsageRules(@PathVariable Long id,
            @Valid @RequestBody List<OverdraftUsageRuleCommand> rules) {
        return ResponseEntity.ok(writeService.updateUsageRules(id, rules));
    }

    @PostMapping("/{id}/accounting")
    @PreAuthorize("hasAnyAuthority('OVERDRAFT_PRODUCT_ADMIN','FINANCE_ADMIN')")
    public ResponseEntity<OverdraftProductData> updateAccounting(@PathVariable Long id,
            @Valid @RequestBody OverdraftProductAccountingCommand command) {
        return ResponseEntity.ok(writeService.updateAccounting(id, command));
    }
}
