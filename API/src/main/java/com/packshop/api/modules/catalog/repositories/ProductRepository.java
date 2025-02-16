package com.packshop.api.modules.catalog.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.packshop.api.modules.catalog.entities.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
