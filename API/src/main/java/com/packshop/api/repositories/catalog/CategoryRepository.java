package com.packshop.api.repositories.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import com.packshop.api.entities.catalog.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);
}
