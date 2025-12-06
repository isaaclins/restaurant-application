package com.restaurant.cart.config;

import com.restaurant.cart.model.Cart;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis Configuration - Using JDK Serialization for reliability
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Cart> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Cart> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key serializer - strings
        template.setKeySerializer(new StringRedisSerializer());

        // Value serializer - JDK serialization (Cart implements Serializable)
        template.setValueSerializer(new JdkSerializationRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }
}
