package com.ecommerce.service;

import com.ecommerce.dto.CartDto.*;
import com.ecommerce.entity.*;
import com.ecommerce.exception.ApiException;
import com.ecommerce.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock CartRepository cartRepository;
    @Mock ProductRepository productRepository;
    @Mock UserRepository userRepository;
    @InjectMocks CartService cartService;

    private User user;
    private Product product;
    private Cart cart;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(10.0);
        product.setStockQuantity(5);

        cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        cart.setItems(new ArrayList<>());
    }

    @Test
    void addItem_success() {
        AddItemRequest request = new AddItemRequest();
        request.setProductId(1L);
        request.setQuantity(2);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartRepository.save(any())).thenReturn(cart);

        CartResponse response = cartService.addItem(1L, request);

        assertThat(response).isNotNull();
        verify(cartRepository).save(cart);
    }

    @Test
    void addItem_insufficientStock_throwsException() {
        AddItemRequest request = new AddItemRequest();
        request.setProductId(1L);
        request.setQuantity(10);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> cartService.addItem(1L, request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void removeItem_success() {
        CartItem item = new CartItem();
        item.setId(1L);
        item.setProduct(product);
        item.setQuantity(1);
        item.setCart(cart);
        cart.getItems().add(item);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any())).thenReturn(cart);

        CartResponse response = cartService.removeItem(1L, 1L);

        assertThat(response.getItems()).isEmpty();
    }

    @Test
    void getCart_createsNewCartIfNotExists() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.save(any())).thenReturn(cart);

        CartResponse response = cartService.getCart(1L);

        assertThat(response).isNotNull();
        verify(cartRepository).save(any(Cart.class));
    }
}
