package com.packshop.api.modules.catalog.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

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

    private String thumbnail;

    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @Lob
    private String description;

    @ElementCollection
    private List<String> media;

    private BigDecimal price;

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
