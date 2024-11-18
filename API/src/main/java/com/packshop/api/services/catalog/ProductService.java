package com.packshop.api.services.catalog;

import com.packshop.api.dto.catalog.ProductDTO;
import com.packshop.api.etities.catalog.Product;
import com.packshop.api.etities.catalog.ProductAttribute;
import com.packshop.api.exception.ResourceNotFoundException;
import com.packshop.api.repositories.catalog.ProductRepository;
import com.packshop.api.utilities.EntityDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return EntityDtoMapper.convertListToDto(products, ProductDTO.class);
    }

    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return EntityDtoMapper.mapToDto(product, ProductDTO.class);
    }

    public ProductDTO createProduct(ProductDTO productDTO) {
        // Chuyển đổi DTO thành Entity
        Product product = EntityDtoMapper.mapToEntity(productDTO, Product.class);

        // Đặt liên kết giữa Product và ProductAttribute (nếu có attributes trong DTO)
        if (product.getAttributes() != null) {
            ProductAttribute attributes = product.getAttributes();
            attributes.setProduct(product); // Liên kết hai thực thể
        }

        // Lưu Product (cùng với ProductAttribute nhờ Cascade.PERSIST)
        product = productRepository.save(product);

        // Chuyển đổi lại sang DTO để trả về
        return EntityDtoMapper.mapToDto(product, ProductDTO.class);
    }

    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        EntityDtoMapper.updateEntityFromDto(product, productDTO);
        product = productRepository.save(product);
        return EntityDtoMapper.mapToDto(product, ProductDTO.class);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }
}

