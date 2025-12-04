package com.restaurant.cart.client;

import com.restaurant.cart.dto.ProductDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback for Product Client when service is unavailable
 */
@Component
@Slf4j
public class ProductClientFallback implements ProductClient {

    @Override
    public ProductDTO getProductById(Long id) {
        log.warn("Product service unavailable, returning fallback for product: {}", id);
        return null;
    }
}
