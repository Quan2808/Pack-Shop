package com.packshop.api.modules.catalog.entities.product;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.packshop.api.modules.catalog.entities.category.Category;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String thumbnail;

    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @Lob
    private String description;

    @ElementCollection
    private List<String> media;

    @Column(nullable = false)
    @Min(value = 0, message = "Price must be non-negative")
    private Long price;

    @Column(nullable = false, unique = true)
    private String sku;

    private int quantity;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private ProductAttribute attributes;

    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id", nullable = false)
    @JsonManagedReference
    private Category category;

    public void setAttributes(ProductAttribute attributes) {
        if (attributes == null) {
            if (this.attributes != null) {
                this.attributes.setProduct(null);
            }
        } else {
            attributes.setProduct(this);
        }
        this.attributes = attributes;
    }
}
