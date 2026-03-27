package com.ecommerce.service;

import com.ecommerce.dto.OrderDto.*;
import com.ecommerce.entity.*;
import com.ecommerce.exception.ApiException;
import com.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final PaymentService paymentService;

    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException("Cart is empty", HttpStatus.BAD_REQUEST));

        if (cart.getItems().isEmpty()) {
            throw new ApiException("Cart is empty", HttpStatus.BAD_REQUEST);
        }

        Order order = new Order();
        order.setUser(cart.getUser());
        order.setShippingAddress(request.getShippingAddress());

        List<OrderItem> orderItems = cart.getItems().stream().map(cartItem -> {
            Product product = cartItem.getProduct();
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new ApiException("Insufficient stock for: " + product.getName(), HttpStatus.BAD_REQUEST);
            }
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(cartItem.getQuantity());
            item.setPrice(product.getPrice());
            return item;
        }).collect(Collectors.toList());

        order.setItems(orderItems);
        order.setTotalPrice(orderItems.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum());
        Order saved = orderRepository.save(order);

        cart.getItems().clear();
        cartRepository.save(cart);

        String clientSecret = paymentService.createPaymentIntent(saved.getId());
        OrderResponse response = toResponse(saved);
        response.setClientSecret(clientSecret);
        return response;
    }

    public OrderResponse getOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("Order not found", HttpStatus.NOT_FOUND));
        if (!order.getUser().getId().equals(userId)) {
            throw new ApiException("Access denied", HttpStatus.FORBIDDEN);
        }
        return toResponse(order);
    }

    public List<OrderResponse> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream().map(i -> {
            OrderItemResponse r = new OrderItemResponse();
            r.setProductName(i.getProduct().getName());
            r.setQuantity(i.getQuantity());
            r.setPrice(i.getPrice());
            return r;
        }).collect(Collectors.toList());

        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getId());
        response.setItems(items);
        response.setTotalPrice(order.getTotalPrice());
        response.setShippingAddress(order.getShippingAddress());
        response.setPaymentStatus(order.getPaymentStatus().name());
        response.setCreatedAt(order.getCreatedAt());
        return response;
    }
}
