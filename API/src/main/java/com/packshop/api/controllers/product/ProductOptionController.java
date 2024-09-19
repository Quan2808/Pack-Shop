package com.packshop.api.controllers.product;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.packshop.api.entities.product.ProductOption;
import com.packshop.api.services.product.ProductOptionService;

@RestController
@RequestMapping("/api/product-options")
public class ProductOptionController {

    @Autowired
    private ProductOptionService productOptionService;

    @GetMapping
    public List<ProductOption> getAllProductOptions() {
        return productOptionService.getAllProductOptions();
    }

    @GetMapping("/{id}")
    public Optional<ProductOption> getProductOptionById(@PathVariable Long id) {
        return productOptionService.getProductOptionById(id);
    }

    @PostMapping
    public ProductOption createProductOption(@RequestBody ProductOption productOption) {
        return productOptionService.saveProductOption(productOption);
    }

    @PutMapping("/{id}")
    public ProductOption updateProductOption(@PathVariable Long id, @RequestBody ProductOption productOption) {
        productOption.setId(id);
        return productOptionService.saveProductOption(productOption);
    }

    @DeleteMapping("/{id}")
    public void deleteProductOption(@PathVariable Long id) {
        productOptionService.deleteProductOption(id);
    }
}
