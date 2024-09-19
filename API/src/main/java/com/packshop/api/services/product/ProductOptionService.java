package com.packshop.api.services.product;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.packshop.api.entities.product.ProductOption;
import com.packshop.api.repositories.product.ProductOptionRepository;

@Service
public class ProductOptionService {

    @Autowired
    private ProductOptionRepository productOptionRepository;

    public List<ProductOption> getAllProductOptions() {
        return productOptionRepository.findAll();
    }

    public Optional<ProductOption> getProductOptionById(Long id) {
        return productOptionRepository.findById(id);
    }

    public ProductOption saveProductOption(ProductOption productOption) {
        return productOptionRepository.save(productOption);
    }

    public void deleteProductOption(Long id) {
        productOptionRepository.deleteById(id);
    }
}
