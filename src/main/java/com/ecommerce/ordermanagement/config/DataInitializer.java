package com.ecommerce.ordermanagement.config;

import com.ecommerce.ordermanagement.model.Product;
import com.ecommerce.ordermanagement.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(ProductRepository productRepository) {
        return args -> {
            log.info("Initializing database with sample products...");

            Product laptop = new Product();
            laptop.setName("MacBook Pro 16\"");
            laptop.setDescription("High-performance laptop with M3 chip");
            laptop.setPrice(new BigDecimal("2499.99"));
            laptop.setStock(50);
            laptop.setCategory("Electronics");
            laptop.setImageUrl("https://example.com/macbook.jpg");

            Product phone = new Product();
            phone.setName("iPhone 15 Pro");
            phone.setDescription("Latest smartphone with advanced features");
            phone.setPrice(new BigDecimal("999.99"));
            phone.setStock(100);
            phone.setCategory("Electronics");
            phone.setImageUrl("https://example.com/iphone.jpg");

            Product book = new Product();
            book.setName("Spring Boot in Action");
            book.setDescription("Comprehensive guide to Spring Boot");
            book.setPrice(new BigDecimal("45.99"));
            book.setStock(200);
            book.setCategory("Books");
            book.setImageUrl("https://example.com/book.jpg");

            Product shirt = new Product();
            shirt.setName("Cotton T-Shirt");
            shirt.setDescription("Comfortable 100% cotton t-shirt");
            shirt.setPrice(new BigDecimal("29.99"));
            shirt.setStock(5); // Low stock
            shirt.setCategory("Clothing");
            shirt.setImageUrl("https://example.com/shirt.jpg");

            Product headphones = new Product();
            headphones.setName("Wireless Headphones");
            headphones.setDescription("Noise-cancelling Bluetooth headphones");
            headphones.setPrice(new BigDecimal("199.99"));
            headphones.setStock(75);
            headphones.setCategory("Electronics");
            headphones.setImageUrl("https://example.com/headphones.jpg");

            productRepository.saveAll(Arrays.asList(laptop, phone, book, shirt, headphones));

            log.info("Sample products loaded successfully!");
        };
    }
}
