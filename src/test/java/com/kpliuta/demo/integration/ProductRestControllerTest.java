package com.kpliuta.demo.integration;

import com.kpliuta.demo.domain.Product;
import org.junit.jupiter.api.Test;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;

import java.math.BigDecimal;
import java.util.List;

import static com.kpliuta.demo.DummyData.*;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;


class ProductRestControllerTest extends BaseIntegrationTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.4");

    static final String PRODUCT_RESOURCE_PATH = "/api/product";

    @Test
    void getReturnsEmptyList() {
        ResponseEntity<List<Product>> response = restClient.get()
                .uri(uriBase + PRODUCT_RESOURCE_PATH + "?pageNumber={number}&pageSize={size}", 0, 100)
                .accept(APPLICATION_JSON)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {
                });
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertTrue(requireNonNull(response.getBody()).isEmpty());
    }

    @Test
    void getReturnsSingleElementList() {
        // create product
        ResponseEntity<Product> createResponse = restClient.post()
                .uri(uriBase + PRODUCT_RESOURCE_PATH)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(buildMercedesBenzVitoProduct())
                .retrieve()
                .toEntity(Product.class);
        assertTrue(createResponse.getStatusCode().is2xxSuccessful());

        // get returns single element list
        ResponseEntity<List<Product>> getResponse = restClient.get()
                .uri(uriBase + PRODUCT_RESOURCE_PATH + "?pageNumber={number}&pageSize={size}", 0, 100)
                .accept(APPLICATION_JSON)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {
                });
        assertTrue(getResponse.getStatusCode().is2xxSuccessful());
        assertEquals(1, requireNonNull(getResponse.getBody()).size());
    }

    @Test
    void getDoesNotReturnDeletedProduct() {
        // create product
        ResponseEntity<Product> createResponse = restClient.post()
                .uri(uriBase + PRODUCT_RESOURCE_PATH)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(buildVwBeetleProduct())
                .retrieve()
                .toEntity(Product.class);
        assertTrue(createResponse.getStatusCode().is2xxSuccessful());

        Product product = requireNonNull(createResponse.getBody());

        // delete product
        ResponseEntity<Void> deleteResponse = restClient.delete()
                .uri(uriBase + PRODUCT_RESOURCE_PATH + "/{id}", product.getId())
                .accept(APPLICATION_JSON)
                .retrieve()
                .toBodilessEntity();
        assertTrue(deleteResponse.getStatusCode().is2xxSuccessful());

        // get returns empty list
        ResponseEntity<List<Product>> getResponse = restClient.get()
                .uri(uriBase + PRODUCT_RESOURCE_PATH + "?pageNumber={number}&pageSize={size}", 0, 100)
                .accept(APPLICATION_JSON)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {
                });
        assertTrue(getResponse.getStatusCode().is2xxSuccessful());
        assertTrue(requireNonNull(getResponse.getBody()).isEmpty());
    }

    @Test
    void getReturns400IfPagingParametersNotPassed() {
        assertThrows(ExpectedHttpStatusException.class, () -> {
            restClient.get()
                    .uri(uriBase + PRODUCT_RESOURCE_PATH)
                    .accept(APPLICATION_JSON)
                    .retrieve()
                    .onStatus(status -> status.value() == 400, (request, response) -> {
                        throw new ExpectedHttpStatusException();
                    })
                    .toEntity(Product.class);
        });
    }

    @Test
    void getReturnsProductById() {
        // create product
        ResponseEntity<Product> createResponse = restClient.post()
                .uri(uriBase + PRODUCT_RESOURCE_PATH)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(buildMercedesBenzVitoProduct())
                .retrieve()
                .toEntity(Product.class);
        assertTrue(createResponse.getStatusCode().is2xxSuccessful());

        Product product = requireNonNull(createResponse.getBody());

        // get returns product by id
        ResponseEntity<Product> getResponse = restClient.get()
                .uri(uriBase + PRODUCT_RESOURCE_PATH + "/{id}", product.getId())
                .accept(APPLICATION_JSON)
                .retrieve()
                .toEntity(Product.class);
        assertTrue(getResponse.getStatusCode().is2xxSuccessful());
        assertNotNull(getResponse.getBody());
    }

    @Test
    void getReturns404IfProductWasNotFound() {
        assertThrows(ExpectedHttpStatusException.class, () -> {
            restClient.get()
                    .uri(uriBase + PRODUCT_RESOURCE_PATH + "/{id}", "???")
                    .accept(APPLICATION_JSON)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (request, response) -> {
                        throw new ExpectedHttpStatusException();
                    })
                    .toEntity(Product.class);
        });
    }

    @Test
    void postCreatesVwBeetleProduct() {
        Product product = buildVwBeetleProduct();
        ResponseEntity<Product> response = restClient.post()
                .uri(uriBase + PRODUCT_RESOURCE_PATH)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(product)
                .retrieve()
                .toEntity(Product.class);
        assertTrue(response.getStatusCode().is2xxSuccessful());

        assertNotNull(requireNonNull(response.getBody()).getId());
        assertEquals(0, requireNonNull(response.getBody()).getVersion());
    }

    @Test
    void putUpdatesPorsche911Product() {
        // create product
        ResponseEntity<Product> createResponse = restClient.post()
                .uri(uriBase + PRODUCT_RESOURCE_PATH)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(buildPorsche911Product())
                .retrieve()
                .toEntity(Product.class);
        assertTrue(createResponse.getStatusCode().is2xxSuccessful());

        Product product = requireNonNull(createResponse.getBody());
        assertEquals(0, product.getVersion());

        // update product
        BigDecimal newPrice = BigDecimal.valueOf(999);
        product.setPrice(newPrice);
        ResponseEntity<Product> updateResponse = restClient.put()
                .uri(uriBase + PRODUCT_RESOURCE_PATH)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(product)
                .retrieve()
                .toEntity(Product.class);
        assertTrue(updateResponse.getStatusCode().is2xxSuccessful());

        Product updatedProduct = requireNonNull(updateResponse.getBody());
        assertEquals(newPrice, updatedProduct.getPrice());
        assertEquals(1, updatedProduct.getVersion());
    }

    @Test
    void putReturns404IfProductWasNotFound() {
        Product product = buildPorsche911Product();
        product.setId("999");
        product.setVersion(999);
        assertThrows(ExpectedHttpStatusException.class, () -> {
            restClient.put()
                    .uri(uriBase + PRODUCT_RESOURCE_PATH)
                    .contentType(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .body(product)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (request, response) -> {
                        throw new ExpectedHttpStatusException();
                    })
                    .toEntity(Product.class);
        });
    }

    @Test
    void putReturns412IfProductsVersionHasChanged() {
        // create product
        ResponseEntity<Product> createResponse = restClient.post()
                .uri(uriBase + PRODUCT_RESOURCE_PATH)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(buildPorsche911Product())
                .retrieve()
                .toEntity(Product.class);
        assertTrue(createResponse.getStatusCode().is2xxSuccessful());

        Product product = requireNonNull(createResponse.getBody());
        assertEquals(0, product.getVersion());

        // update product
        BigDecimal newPrice = BigDecimal.valueOf(999);
        product.setPrice(newPrice);
        ResponseEntity<Product> updateResponse = restClient.put()
                .uri(uriBase + PRODUCT_RESOURCE_PATH)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(product)
                .retrieve()
                .toEntity(Product.class);
        assertTrue(updateResponse.getStatusCode().is2xxSuccessful());

        // try to update stale version
        assertThrows(ExpectedHttpStatusException.class, () -> {
            restClient.put()
                    .uri(uriBase + PRODUCT_RESOURCE_PATH)
                    .contentType(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .body(product)
                    .retrieve()
                    .onStatus(status -> status.value() == 412, (request, response) -> {
                        throw new ExpectedHttpStatusException();
                    })
                    .toEntity(Product.class);
        });
    }
}
