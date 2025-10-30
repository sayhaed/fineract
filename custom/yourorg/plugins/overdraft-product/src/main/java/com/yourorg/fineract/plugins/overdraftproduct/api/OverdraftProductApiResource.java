/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.yourorg.fineract.plugins.overdraftproduct.api;

import com.yourorg.fineract.plugins.overdraftproduct.serialization.OverdraftProductCommand;
import com.yourorg.fineract.plugins.overdraftproduct.serialization.OverdraftProductData;
import com.yourorg.fineract.plugins.overdraftproduct.service.OverdraftProductReadPlatformService;
import com.yourorg.fineract.plugins.overdraftproduct.service.OverdraftProductWritePlatformService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/overdraft-products")
@RequiredArgsConstructor
@Validated
public class OverdraftProductApiResource {

    private final OverdraftProductReadPlatformService readPlatformService;
    private final OverdraftProductWritePlatformService writePlatformService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('OVERDRAFT_PRODUCT_VIEW', 'ALL_FUNCTIONS', 'ALL_FUNCTIONS_READ')")
    public ResponseEntity<List<OverdraftProductData>> retrieveAll() {
        return ResponseEntity.ok(readPlatformService.retrieveAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('OVERDRAFT_PRODUCT_VIEW', 'ALL_FUNCTIONS', 'ALL_FUNCTIONS_READ')")
    public ResponseEntity<OverdraftProductData> retrieveOne(@PathVariable("id") Long id) {
        return ResponseEntity.ok(readPlatformService.retrieveOne(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('OVERDRAFT_PRODUCT_ADMIN', 'ALL_FUNCTIONS')")
    public ResponseEntity<Void> createProduct(@Valid @RequestBody OverdraftProductCommand command) {
        Long id = writePlatformService.createProduct(command);
        return ResponseEntity.created(URI.create("/api/v1/overdraft-products/" + id)).build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('OVERDRAFT_PRODUCT_ADMIN', 'ALL_FUNCTIONS')")
    public ResponseEntity<Void> updateProduct(@PathVariable("id") Long id, @Valid @RequestBody OverdraftProductCommand command) {
        writePlatformService.updateProduct(id, command);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}:activate")
    @PreAuthorize("hasAnyAuthority('OVERDRAFT_PRODUCT_ADMIN', 'ALL_FUNCTIONS')")
    public ResponseEntity<Void> activate(@PathVariable("id") Long id) {
        writePlatformService.activate(id);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{id}:retire")
    @PreAuthorize("hasAnyAuthority('OVERDRAFT_PRODUCT_ADMIN', 'ALL_FUNCTIONS')")
    public ResponseEntity<Void> retire(@PathVariable("id") Long id) {
        writePlatformService.retire(id);
        return ResponseEntity.accepted().build();
    }
}
