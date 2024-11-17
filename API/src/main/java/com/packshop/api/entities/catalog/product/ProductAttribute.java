package com.packshop.api.entities.catalog.product;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Table(name = "product_attribute")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String material; // Example: "Cotton"

    private String dimensions; // Example: "30x40x10 cm"

    private String capacity; // Example: "20L"

    private String weight; // Example: "0.8 kg"

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonBackReference
    private Product product;
}