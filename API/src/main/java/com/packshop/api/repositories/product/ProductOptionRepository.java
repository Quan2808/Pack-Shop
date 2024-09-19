package com.packshop.api.repositories.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.packshop.api.entities.product.ProductOption;

@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {
}
