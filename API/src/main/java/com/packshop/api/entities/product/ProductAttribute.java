package com.packshop.api.entities.product;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @OneToOne(mappedBy = "productAttribute")
    @JsonBackReference
    private Product product;
}
