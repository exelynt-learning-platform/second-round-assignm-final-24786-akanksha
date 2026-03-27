package com.ecommerce.util;

import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUtil {

    private final UserRepository userRepository;

    public Long getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername()).map(User::getId).orElseThrow();
    }
}
