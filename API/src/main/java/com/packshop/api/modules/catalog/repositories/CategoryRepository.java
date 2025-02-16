package com.packshop.api.modules.catalog.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.packshop.api.modules.catalog.entities.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);
}
