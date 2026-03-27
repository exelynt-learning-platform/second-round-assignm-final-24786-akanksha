package com.ecommerce.service;

import com.ecommerce.dto.OrderDto.*;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock CartRepository cartRepository;
    @Mock ProductRepository productRepository;
    @Mock PaymentService paymentService;
    @InjectMocks OrderService orderService;

    private User user;
    private Product product;
    private Cart cart;
    private Order order;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        product = new Product();
        product.setId(1L);
        product.setName("Product");
        product.setPrice(20.0);
        product.setStockQuantity(10);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        cart = new Cart();
        cart.setUser(user);
        cart.setItems(new ArrayList<>(List.of(cartItem)));

        order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setItems(new ArrayList<>());
        order.setTotalPrice(40.0);
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
    }

    @Test
    void createOrder_success() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setShippingAddress("123 Main St");

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any())).thenReturn(order);
        when(paymentService.createPaymentIntent(any())).thenReturn("pi_client_secret");
        when(cartRepository.save(any())).thenReturn(cart);

        OrderResponse response = orderService.createOrder(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getClientSecret()).isEqualTo("pi_client_secret");
        verify(productRepository, atLeastOnce()).save(any(Product.class));
    }

    @Test
    void createOrder_emptyCart_throwsException() {
        cart.setItems(new ArrayList<>());
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        CreateOrderRequest request = new CreateOrderRequest();
        request.setShippingAddress("123 Main St");

        assertThatThrownBy(() -> orderService.createOrder(1L, request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Cart is empty");
    }

    @Test
    void getOrder_wrongUser_throwsException() {
        User otherUser = new User();
        otherUser.setId(2L);
        order.setUser(otherUser);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getOrder(1L, 1L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    void getUserOrders_returnsOrders() {
        order.setItems(new ArrayList<>());
        when(orderRepository.findByUserId(1L)).thenReturn(List.of(order));

        List<OrderResponse> orders = orderService.getUserOrders(1L);

        assertThat(orders).hasSize(1);
    }
}
