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
package com.yourorg.fineract.plugins.overdraftproduct.service;

import com.yourorg.fineract.plugins.overdraftproduct.domain.OverdraftProduct;
import com.yourorg.fineract.plugins.overdraftproduct.domain.repositories.OverdraftProductRepository;
import com.yourorg.fineract.plugins.overdraftproduct.serialization.OverdraftProductData;
import com.yourorg.fineract.plugins.overdraftproduct.service.exception.OverdraftProductNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OverdraftProductReadPlatformServiceImpl implements OverdraftProductReadPlatformService {

    private final OverdraftProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<OverdraftProductData> retrieveAll() {
        return productRepository.findAll().stream().map(OverdraftProductData::from).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OverdraftProductData retrieveOne(Long id) {
        OverdraftProduct product = productRepository.findById(id).orElseThrow(() -> new OverdraftProductNotFoundException(id));
        return OverdraftProductData.from(product);
    }
}
