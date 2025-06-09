package com.ecommerce.ordermanagement.service;

import com.ecommerce.ordermanagement.model.Product;
import com.ecommerce.ordermanagement.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    @Cacheable(value = "products")
    public List<Product> getAllProducts() {
        log.info("Fetching all products from database");
        return productRepository.findAll();
    }

    @Cacheable(value = "products", key = "#id")
    public Optional<Product> getProductById(Long id) {
        log.info("Fetching product with id: {} from database", id);
        return productRepository.findById(id);
    }

    @Cacheable(value = "productsByCategory", key = "#category")
    public List<Product> getProductsByCategory(String category) {
        log.info("Fetching products for category: {} from database", category);
        return productRepository.findByCategory(category);
    }

    public List<Product> searchProductsByName(String name) {
        log.info("Searching products with name containing: {}", name);
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Product> getProductsInStock(Integer minStock) {
        log.info("Fetching products with stock greater than: {}", minStock);
        return productRepository.findByStockGreaterThan(minStock);
    }

    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.info("Fetching products between ${} and ${}", minPrice, maxPrice);
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    @CacheEvict(value = {"products", "productsByCategory"}, allEntries = true)
    public Product createProduct(Product product) {
        log.info("Creating new product: {}", product.getName());
        return productRepository.save(product);
    }

    @CachePut(value = "products", key = "#id")
    @CacheEvict(value = {"products", "productsByCategory"}, allEntries = true)
    public Product updateProduct(Long id, Product productDetails) {
        log.info("Updating product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStock(productDetails.getStock());
        product.setCategory(productDetails.getCategory());
        product.setImageUrl(productDetails.getImageUrl());

        return productRepository.save(product);
    }

    @CacheEvict(value = {"products", "productsByCategory"}, allEntries = true)
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);
        productRepository.deleteById(id);
    }

    public boolean updateStock(Long productId, Integer quantity) {
        log.info("Updating stock for product {}: {} units", productId, quantity);

        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setStock(product.getStock() + quantity);
            productRepository.save(product);
            return true;
        }
        return false;
    }

    public List<Product> getLowStockProducts(Integer threshold) {
        log.info("Fetching products with stock <= {}", threshold);
        return productRepository.findLowStockProducts(threshold);
    }

    public List<String> getAllCategories() {
        log.info("Fetching all distinct categories");
        return productRepository.findDistinctCategories();
    }
}