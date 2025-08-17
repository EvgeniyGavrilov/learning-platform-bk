package com.medical_learning_platform.app.product;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class ProductController {
    private ProductService productService;

    @GetMapping("/products")
    public Flux<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/products/search")
    public Flux<Product> getProductByName(@RequestParam String name) {
        return productService.getProductByName(name);
    }

    @GetMapping("/products/{id}")
    public Mono<ResponseEntity<Product>> getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @GetMapping("/test")
    public Flux<Product> test() {
        return Flux.just(new Product());
    }

    @PostMapping("/products")
    public Mono<ResponseEntity<Product>> addProducts(@RequestParam Product product) {
        return productService.addProducts(product);
    }

    @PutMapping("/products/{id}")
    public Mono<ResponseEntity<Product>> putProduct(@PathVariable Long id, @RequestParam Product updatedProduct) {
        return productService.putProduct(id, updatedProduct);
    }

    @DeleteMapping("/products/{id}")
    public Mono<Void> deleteProduct(@PathVariable Long id) {
        return productService.deleteProduct(id);
    }

    @GetMapping(value = "/products/stream", produces = "application/stream+json")
    public Flux<Product> streamProductsPeriodically() {
        return productService.streamProductsPeriodically();
    }
}
