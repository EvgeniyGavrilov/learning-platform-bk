package com.medical_learning_platform.app.product;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ProductRepository extends ReactiveCrudRepository<Product, Long> {
    Flux<Product> findByNameIgnoreCase(String name);
}