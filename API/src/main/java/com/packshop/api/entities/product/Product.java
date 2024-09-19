package com.packshop.api.entities.product;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.packshop.api.entities.category.Category;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private double price;

    @Column(length = 255)
    private String thumbnail;

    @JsonManagedReference
    @OneToMany(mappedBy = "product")
    private List<ProductImage> images;

    @ManyToOne
    private Category category;
    
    @JsonManagedReference
    @OneToMany(mappedBy = "product")
    private List<ProductVariant> variants;
}
