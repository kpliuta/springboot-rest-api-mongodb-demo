package com.kpliuta.demo.integration;

import com.kpliuta.demo.domain.Order;
import com.kpliuta.demo.domain.Product;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
abstract class BaseIntegrationTest {

    @Value("http://localhost:${local.server.port}")
    String uriBase;

    static final RestClient restClient = RestClient.create();

    @Autowired
    MongoTemplate mongoTemplate;

    @AfterEach
    void cleanup() {
        mongoTemplate.dropCollection(Order.class);
        mongoTemplate.dropCollection(Product.class);
    }
}
