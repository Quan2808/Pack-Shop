package com.packshop.api.common.configurations;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.packshop.api.modules.catalog.dto.ProductDTO;
import com.packshop.api.modules.catalog.entities.Product;

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

        return modelMapper;
    }
}
