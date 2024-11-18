package com.packshop.api.repositories.catalog;

import com.packshop.api.etities.catalog.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
