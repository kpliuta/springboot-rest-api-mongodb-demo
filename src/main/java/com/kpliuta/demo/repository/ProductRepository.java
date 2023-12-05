package com.kpliuta.demo.repository;

import com.kpliuta.demo.domain.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Update;

public interface ProductRepository extends MongoRepository<Product, String> {

    /**
     * Finds a product by given ID and sets 'removed' flag to 'true'.
     *
     * @param id product ID
     */
    @Update("{ '$set' : { 'removed' : true } }")
    void findAndSetRemovedById(String id);
}
