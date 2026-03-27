package com.ecommerce.controller;

import com.ecommerce.dto.CartDto.*;
import com.ecommerce.service.CartService;
import com.ecommerce.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final AuthUtil authUtil;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cartService.getCart(authUtil.getUserId(userDetails)));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@AuthenticationPrincipal UserDetails userDetails,
                                                @Valid @RequestBody AddItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(authUtil.getUserId(userDetails), request));
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateItem(@AuthenticationPrincipal UserDetails userDetails,
                                                   @PathVariable Long cartItemId,
                                                   @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.updateItem(authUtil.getUserId(userDetails), cartItemId, quantity));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> removeItem(@AuthenticationPrincipal UserDetails userDetails,
                                                   @PathVariable Long cartItemId) {
        return ResponseEntity.ok(cartService.removeItem(authUtil.getUserId(userDetails), cartItemId));
    }
}
