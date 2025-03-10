package com.packshop.api.common.configurations;

import java.util.Collections;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.packshop.api.modules.catalog.dto.ProductDTO;
import com.packshop.api.modules.catalog.entities.product.Product;
import com.packshop.api.modules.identity.dto.AuthResponse;
import com.packshop.api.modules.identity.dto.UserResponse;
import com.packshop.api.modules.identity.entities.Role;
import com.packshop.api.modules.identity.entities.User;
import com.packshop.api.modules.shopping.address.dto.AddressDTO;
import com.packshop.api.modules.shopping.address.entity.Address;
import com.packshop.api.modules.shopping.cart.dto.CartItemDTO;
import com.packshop.api.modules.shopping.cart.entities.CartItem;
import com.packshop.api.modules.shopping.dto.ProductItemDTO;

@Configuration
public class ModelMapperConfig {
        @Bean
        public ModelMapper modelMapper() {
                ModelMapper modelMapper = new ModelMapper();

                // Product -> ProductDTO mapping
                modelMapper.createTypeMap(Product.class, ProductDTO.class)
                                .addMapping(src -> src.getCategory().getId(), ProductDTO::setCategoryId)
                                .addMapping(src -> src.getAttributes().getMaterial(), ProductDTO::setMaterial)
                                .addMapping(src -> src.getAttributes().getDimensions(), ProductDTO::setDimensions)
                                .addMapping(src -> src.getAttributes().getCapacity(), ProductDTO::setCapacity)
                                .addMapping(src -> src.getAttributes().getWeight(), ProductDTO::setWeight);

                // User -> UserResponse mapping
                modelMapper.createTypeMap(User.class, UserResponse.class)
                                .addMapping(User::getId, UserResponse::setUserId)
                                .addMapping(
                                                src -> src.getRoles() != null
                                                                ? src.getRoles().stream().map(Role::getName)
                                                                                .collect(Collectors.toSet())
                                                                : Collections.emptySet(),
                                                UserResponse::setRoles);

                // User -> AuthResponse mapping
                modelMapper.createTypeMap(User.class, AuthResponse.class)
                                .addMapping(User::getId, AuthResponse::setUserId)
                                .addMapping(
                                                src -> src.getRoles() != null
                                                                ? src.getRoles().stream().map(Role::getName)
                                                                                .collect(Collectors.toSet())
                                                                : Collections.emptySet(),
                                                AuthResponse::setRoles);

                // Product -> ProductCartDTO mapping
                modelMapper.createTypeMap(Product.class, ProductItemDTO.class);

                // CartItem -> CartItemDTO mapping
                modelMapper.createTypeMap(CartItem.class, CartItemDTO.class)
                                .addMappings(mapper -> {
                                        mapper.skip(CartItemDTO::setProduct);
                                });

                // Address -> AddressDTO mapping
                modelMapper.createTypeMap(AddressDTO.class, Address.class)
                                .addMappings(mapper -> {
                                        mapper.skip(Address::setUser);
                                });

                modelMapper.getConfiguration()
                                .setMatchingStrategy(MatchingStrategies.STRICT)
                                .setSkipNullEnabled(true);

                return modelMapper;
        }
}
