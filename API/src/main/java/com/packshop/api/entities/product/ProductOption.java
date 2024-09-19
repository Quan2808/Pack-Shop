package com.packshop.api.entities.product;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_option")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // Example: "Material", "Dimensions", "Capacity", "Weight"

    private String value; // Example: "Cotton", "30x40x10 cm", "20L", "0.8 kg"

    @JsonBackReference
    @ManyToOne
    private ProductVariant variant;
}
