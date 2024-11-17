package com.packshop.api.services.catalog.product;

import com.packshop.api.dto.catalog.product.ProductDTO;
import com.packshop.api.entities.catalog.product.Product;
import com.packshop.api.entities.catalog.product.ProductAttribute;
import com.packshop.api.exception.ResourceNotFoundException;
import com.packshop.api.repositories.catalog.product.ProductRepository;
import com.packshop.api.utilities.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return MapperUtil.mapEntitiesToDtos(products, ProductDTO.class);
    }

    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return MapperUtil.mapToDto(product, ProductDTO.class);
    }

    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = MapperUtil.mapToEntity(productDTO, Product.class);

        if (product.getAttributes() != null) {
            ProductAttribute attributes = product.getAttributes();
            attributes.setProduct(product);
        }

        product = productRepository.save(product);

        return MapperUtil.mapToDto(product, ProductDTO.class);
    }

    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        MapperUtil.updateEntityFromDto(product, productDTO);
        product = productRepository.save(product);
        return MapperUtil.mapToDto(product, ProductDTO.class);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }
}

