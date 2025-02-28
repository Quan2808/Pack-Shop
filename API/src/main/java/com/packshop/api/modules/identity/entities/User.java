package com.packshop.api.modules.identity.entities;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.packshop.api.modules.identity.entities.address.Address;
import com.packshop.api.modules.shopping.entities.cart.Cart;
import com.packshop.api.modules.shopping.entities.order.Order;

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
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        private String username;

        @Column(nullable = false)
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String password;

        @Column(nullable = false, unique = true)
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        private String email;

        @Column(name = "full_name", nullable = false)
        @NotBlank(message = "Full name is required")
        @Size(max = 100, message = "Full name must not exceed 100 characters")
        private String fullName;

        @Column(name = "phone_number", unique = true)
        @Pattern(regexp = "^\\(\\+84\\)\\s[0-9]{3}\\s[0-9]{3}\\s[0-9]{3}$", message = "Phone number must be in the format (+84) 123 456 789")
        private String phoneNumber;

        @Column(name = "avatar_url")
        @Size(max = 255, message = "Avatar URL must not exceed 255 characters")
        private String avatarUrl;

        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        private Set<Address> shippingAddresses;

        @OneToOne(mappedBy = "user", cascade = { CascadeType.PERSIST, CascadeType.MERGE,
                        CascadeType.REMOVE }, fetch = FetchType.LAZY)
        @JsonIgnore
        private Cart cart;

        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        private Set<Order> orders;

        @ManyToMany(fetch = FetchType.EAGER)
        @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
        private Set<Role> roles;
}
