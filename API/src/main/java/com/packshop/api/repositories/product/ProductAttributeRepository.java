package com.packshop.api.repositories.product;

import org.springframework.data.jpa.repository.JpaRepository;

import com.packshop.api.entities.product.ProductAttribute;

public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long> {
}
