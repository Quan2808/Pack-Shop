package com.packshop.api.modules.identity.entities;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.packshop.api.modules.identity.entities.address.Address;

import com.packshop.api.modules.shopping.cart.entities.Cart;
import com.packshop.api.modules.shopping.order.entities.Order;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id", updatable = false, nullable = false)
        private Long id;

        @Column(nullable = false, unique = true)
        @ValidUniqueName(fieldName = "Username")
        private String username;

        @Column(nullable = false)
        @ValidPassword
        private String password;

        @Column(nullable = false, unique = true)
        @ValidEmail
        private String email;

        @Column(name = "full_name", nullable = false)
        @ValidFieldName(fieldName = "Full name")
        private String fullName;

        @Column(name = "phone_number", unique = true)
        @ValidPhoneNumber
        private String phoneNumber;

        @Column(name = "avatar_url")
        @ValidPath(maxLength = 255)
        private String avatarUrl;

        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        private Set<Address> shippingAddresses;

        @OneToOne(mappedBy = "user", cascade = { CascadeType.PERSIST, CascadeType.MERGE,
                        CascadeType.REMOVE }, fetch = FetchType.LAZY)
        @JsonManagedReference
        private Cart cart;

        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        @JsonManagedReference
        private Set<Order> orders;

        @ManyToMany(fetch = FetchType.EAGER)
        @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
        private Set<Role> roles;
}
