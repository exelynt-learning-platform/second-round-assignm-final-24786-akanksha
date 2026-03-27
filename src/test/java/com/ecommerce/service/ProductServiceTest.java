package com.ecommerce.service;

import com.ecommerce.entity.Product;
import com.ecommerce.exception.ApiException;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @InjectMocks ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setPrice(999.99);
        product.setStockQuantity(10);
    }

    @Test
    void getAllProducts_returnsList() {
        when(productRepository.findAll()).thenReturn(List.of(product));
        assertThat(productService.getAllProducts()).hasSize(1);
    }

    @Test
    void getProduct_found() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        assertThat(productService.getProduct(1L).getName()).isEqualTo("Laptop");
    }

    @Test
    void getProduct_notFound_throwsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> productService.getProduct(99L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void createProduct_success() {
        when(productRepository.save(any())).thenReturn(product);
        Product created = productService.createProduct(product);
        assertThat(created.getName()).isEqualTo("Laptop");
    }

    @Test
    void deleteProduct_success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        productService.deleteProduct(1L);
        verify(productRepository).delete(product);
    }
}
