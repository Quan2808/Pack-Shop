package com.packshop.api.modules.catalog.services;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.packshop.api.common.exceptions.ResourceNotFoundException;
import com.packshop.api.modules.catalog.dto.ProductDTO;
import com.packshop.api.modules.catalog.entities.category.Category;
import com.packshop.api.modules.catalog.entities.product.Product;
import com.packshop.api.modules.catalog.entities.product.ProductAttribute;
import com.packshop.api.modules.catalog.repositories.CategoryRepository;
import com.packshop.api.modules.catalog.repositories.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    // Product operations
    public ProductDTO createProduct(ProductDTO productDTO) {
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Product product = modelMapper.map(productDTO, Product.class);
        product.setCategory(category);

        ProductAttribute attributes = new ProductAttribute();
        attributes.setMaterial(productDTO.getMaterial());
        attributes.setDimensions(productDTO.getDimensions());
        attributes.setCapacity(productDTO.getCapacity());
        attributes.setWeight(productDTO.getWeight());
        attributes.setProduct(product);

        product.setAttributes(attributes);

        Product savedProduct = productRepository.save(product);
        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    public ProductDTO getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return modelMapper.map(product, ProductDTO.class);
    }

    public List<ProductDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .collect(Collectors.toList());
    }

    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Category category = null;
        if (!product.getCategory().getId().equals(productDTO.getCategoryId())) {
            category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        }

        // Update basic product fields
        product.setName(productDTO.getName());
        product.setThumbnail(productDTO.getThumbnail());
        product.setStatus(productDTO.getStatus());
        product.setDescription(productDTO.getDescription());
        product.setMedia(productDTO.getMedia());
        product.setPrice(productDTO.getPrice());
        product.setSku(productDTO.getSku());
        product.setQuantity(productDTO.getQuantity());

        if (category != null) {
            product.setCategory(category);
        }

        // Update or create ProductAttribute
        ProductAttribute attributes = product.getAttributes();
        if (attributes == null) {
            attributes = new ProductAttribute();
            attributes.setProduct(product);
        }

        attributes.setMaterial(productDTO.getMaterial());
        attributes.setDimensions(productDTO.getDimensions());
        attributes.setCapacity(productDTO.getCapacity());
        attributes.setWeight(productDTO.getWeight());

        product.setAttributes(attributes);

        Product updatedProduct = productRepository.save(product);
        return modelMapper.map(updatedProduct, ProductDTO.class);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getAttributes() != null) {
            product.getAttributes().setProduct(null);
            product.setAttributes(null);
        }

        productRepository.delete(product);
    }
}
