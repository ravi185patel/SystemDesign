package com.example.distributedcache.config;


import com.example.distributedcache.model.Product;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import tools.jackson.databind.ObjectMapper;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {


    @Bean
    public RedisTemplate<String, Product> redisTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper) {

        RedisTemplate<String, Product> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer keySerializer =
                new StringRedisSerializer();

        JacksonJsonRedisSerializer<Product> valueSerializer =
                new JacksonJsonRedisSerializer<>(
                        objectMapper,
                        Product.class
                );

        template.setKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashKeySerializer(keySerializer);
        template.setHashValueSerializer(valueSerializer);

        template.afterPropertiesSet();

        return template;
    }

//    @Bean
//    public RedisTemplate<String, Product> redisTemplate(
//            RedisConnectionFactory connectionFactory,
//            ObjectMapper objectMapper) {
//
//        RedisTemplate<String, Product> template = new RedisTemplate<>();
//        template.setConnectionFactory(connectionFactory);
//
//        StringRedisSerializer keySerializer = new StringRedisSerializer();
//
//        GenericJackson2JsonRedisSerializer valueSerializer =
//                new GenericJackson2JsonRedisSerializer(objectMapper);
//
//        template.setKeySerializer(keySerializer);
//        template.setValueSerializer(valueSerializer);
//        template.setHashKeySerializer(keySerializer);
//        template.setHashValueSerializer(valueSerializer);
//
//        template.afterPropertiesSet();
//
//        return template;
//    }
//    @Bean
//    public RedisTemplate<String, Product> redisTemplate(RedisConnectionFactory connectionFactory){
//        RedisTemplate<String, Product> template = new RedisTemplate<>();
//        template.setConnectionFactory(connectionFactory);
//
//        StringRedisSerializer keySerializer = new StringRedisSerializer();
//
//        Jackson2JsonRedisSerializer<Product> valueSerializer =
//                new Jackson2JsonRedisSerializer<>(Product.class);
//
//        template.setKeySerializer(keySerializer);
//        template.setValueSerializer(valueSerializer);
//        template.setHashKeySerializer(keySerializer);
//        template.setHashValueSerializer(valueSerializer);
//
//        template.afterPropertiesSet();
//
//        return template;
//    }
}
