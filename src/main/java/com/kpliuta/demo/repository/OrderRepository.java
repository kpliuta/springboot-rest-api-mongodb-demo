package com.kpliuta.demo.repository;

import com.kpliuta.demo.domain.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, String> {
}
