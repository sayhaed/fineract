package org.apache.fineract.portfolio.overdraftproduct.service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftProduct;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftProductStatus;
import org.apache.fineract.portfolio.overdraftproduct.exception.OverdraftProductNotFoundException;
import org.apache.fineract.portfolio.overdraftproduct.repository.OverdraftProductRepository;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftProductData;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftProductMapper;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftProductValidationResult;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class OverdraftProductReadPlatformServiceImpl implements OverdraftProductReadPlatformService {

    private final OverdraftProductRepository productRepository;
    private final OverdraftProductMapper mapper;
    private final OverdraftProductValidationService validationService;

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<OverdraftProductData> retrieveAll(Optional<OverdraftProductStatus> status, Optional<String> currency) {
        List<OverdraftProduct> products;
        if (status.isPresent() && currency.isPresent()) {
            products = productRepository.findByStatusAndCurrencyCode(status.get(), currency.get());
        } else if (status.isPresent()) {
            products = productRepository.findByStatus(status.get());
        } else if (currency.isPresent()) {
            products = productRepository.findByCurrencyCodeIgnoreCase(currency.get());
        } else {
            products = productRepository.findAll();
        }
        return products.stream().map(mapper::mapToData).collect(Collectors.toList());
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public OverdraftProductData retrieveOne(Long id) {
        OverdraftProduct product = getProduct(id);
        return mapper.mapToData(product);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public OverdraftProductValidationResult validate(Long id) {
        OverdraftProduct product = getProduct(id);
        return validationService.evaluate(product);
    }

    private OverdraftProduct getProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new OverdraftProductNotFoundException(id));
    }
}
