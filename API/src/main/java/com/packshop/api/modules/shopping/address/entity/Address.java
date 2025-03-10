package com.packshop.api.modules.shopping.address.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.packshop.api.modules.identity.entities.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alias_name")
    @Size(max = 100, message = "Alias name must not exceed 100 characters")
    private String aliasName;

    @Column(name = "street_address", nullable = false)
    @NotBlank(message = "Street address is required")
    @Size(max = 255, message = "Street address must not exceed 255 characters")
    private String streetAddress;

    @Column(name = "ward", nullable = false)
    @NotBlank(message = "Ward is required")
    @Size(max = 100, message = "Ward must not exceed 100 characters")
    private String ward;

    @Column(name = "district", nullable = false)
    @NotBlank(message = "District is required")
    @Size(max = 100, message = "District must not exceed 100 characters")
    private String district;

    @Column(name = "province", nullable = false)
    @NotBlank(message = "Province is required")
    @Size(max = 100, message = "Province must not exceed 100 characters")
    private String province;

    @Column(name = "full_address")
    private String fullAddress;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    public String generateFullAddress() {
        return String.format("%s, %s, %s, %s",
                streetAddress, ward, district, province);
    }

    public String generateDefaultAliasName(User user, List<Address> userAddresses) {
        int nextIndex = userAddresses.size() + 1;
        return "My Address " + nextIndex;
    }

}