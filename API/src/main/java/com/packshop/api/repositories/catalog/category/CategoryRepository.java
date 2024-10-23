package com.packshop.api.repositories.catalog.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.packshop.api.entities.catalog.category.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
