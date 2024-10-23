package com.packshop.api.repositories.catalog.product;

import org.springframework.data.jpa.repository.JpaRepository;

import com.packshop.api.entities.catalog.product.ProductAttribute;

public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long> {
}
