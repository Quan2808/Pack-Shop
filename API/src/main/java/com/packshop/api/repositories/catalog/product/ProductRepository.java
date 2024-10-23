package com.packshop.api.repositories.catalog.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.packshop.api.entities.catalog.product.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
