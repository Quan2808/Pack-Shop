package com.packshop.api.repositories.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import com.packshop.api.entities.catalog.Product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
