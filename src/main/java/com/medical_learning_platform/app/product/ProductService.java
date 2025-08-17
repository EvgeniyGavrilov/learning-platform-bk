package com.medical_learning_platform.app.product;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@AllArgsConstructor
@Slf4j
@Service
public class ProductService {
    private ProductRepository productRepository;

    @CircuitBreaker(name = "productServiceCircuitBreaker", fallbackMethod = "fallbackAllProducts")
    @Retry(name = "productServiceRetry")
    public Flux<Product> getAllProducts() {
//        log.info("getAllProducts() called");
//        return Flux.error(new RuntimeException("Simulated error"));
        return productRepository.findAll()
            .doOnNext(p -> log.info("Fetched products: {}", p))
            .doOnError(e -> log.error("Error fetching products: {}", e.getMessage()));
    }

    public Flux<Product> fallbackAllProducts(CallNotPermittedException ex) {
        log.warn(ex.getMessage());
        return Flux.empty();
    }

    public Flux<Product> getProductByName(String name) {
        return productRepository.findByNameIgnoreCase(name).doOnNext(p -> log.info("Got product by name: {}", name));
    }

    public Mono<ResponseEntity<Product>> getProductById(Long id) {
        return productRepository.findById(id)
                .doOnNext(p -> log.info("Got product by id: {}", p))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    public Flux<Product> test() {
        return Flux.just(new Product());
    }

    public Mono<ResponseEntity<Product>> addProducts(Product product) {
        return productRepository.save(product)
                .doOnNext(p -> log.info("Posted products: {}", p))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    public Mono<ResponseEntity<Product>> putProduct(Long id, Product updatedProduct) {
        return productRepository.findById(id).flatMap(existingProduct -> {
            existingProduct.setName(updatedProduct.getName());
            existingProduct.setPrice(updatedProduct.getPrice());
            return productRepository.save(existingProduct)
                    .doOnNext(p -> log.info("Put product: {}", p))
                    .map(ResponseEntity::ok)
                    .defaultIfEmpty(ResponseEntity.notFound().build());
        });
    }

    public Mono<Void> deleteProduct(Long id) {
        return productRepository.deleteById(id).doOnNext(p -> log.info("Deleted product by id: {}", id));
    }

    public Flux<Product> streamProductsPeriodically() {
        return productRepository.findAll()
                .delayElements(Duration.ofSeconds(5));
    }
}
