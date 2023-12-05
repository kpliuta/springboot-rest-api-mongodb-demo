package com.kpliuta.demo.integration;

import com.kpliuta.demo.domain.Order;
import com.kpliuta.demo.domain.OrderItem;
import com.kpliuta.demo.domain.Product;
import com.kpliuta.demo.DummyData;
import org.junit.jupiter.api.Test;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;


class OrderRestControllerTest extends BaseIntegrationTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.4");

    static final String ORDER_RESOURCE_PATH = "/api/order";

    @Test
    void getReturnsEmptyList() {
        ResponseEntity<List<Order>> response = restClient.get()
                .uri(uriBase + ORDER_RESOURCE_PATH + "?pageNumber={number}&pageSize={size}", 0, 100)
                .accept(APPLICATION_JSON)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {
                });
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertTrue(requireNonNull(response.getBody()).isEmpty());
    }

    @Test
    void deleteOrder() {
        // create order
        ResponseEntity<Order> createResponse = restClient.post()
                .uri(uriBase + ORDER_RESOURCE_PATH)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(DummyData.buildOrder("1", createVwBeetleProduct()))
                .retrieve()
                .toEntity(Order.class);
        assertTrue(createResponse.getStatusCode().is2xxSuccessful());

        Order order = requireNonNull(createResponse.getBody());

        // delete order
        ResponseEntity<Void> deleteResponse = restClient.delete()
                .uri(uriBase + ORDER_RESOURCE_PATH + "/{id}", order.getId())
                .accept(APPLICATION_JSON)
                .retrieve()
                .toBodilessEntity();
        assertTrue(deleteResponse.getStatusCode().is2xxSuccessful());

        // get returns empty list
        ResponseEntity<List<Order>> getResponse = restClient.get()
                .uri(uriBase + ORDER_RESOURCE_PATH + "?pageNumber={number}&pageSize={size}", 0, 100)
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
                    .uri(uriBase + ORDER_RESOURCE_PATH)
                    .accept(APPLICATION_JSON)
                    .retrieve()
                    .onStatus(status -> status.value() == 400, (request, response) -> {
                        throw new ExpectedHttpStatusException();
                    })
                    .toEntity(Order.class);
        });
    }

    @Test
    void getReturnsOrderById() {
        // create order
        ResponseEntity<Order> createResponse = restClient.post()
                .uri(uriBase + ORDER_RESOURCE_PATH)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(DummyData.buildOrder("1", createVwBeetleProduct()))
                .retrieve()
                .toEntity(Order.class);
        assertTrue(createResponse.getStatusCode().is2xxSuccessful());

        Order order = requireNonNull(createResponse.getBody());

        // get returns order by id
        ResponseEntity<Order> getResponse = restClient.get()
                .uri(uriBase + ORDER_RESOURCE_PATH + "/{id}", order.getId())
                .accept(APPLICATION_JSON)
                .retrieve()
                .toEntity(Order.class);
        assertTrue(getResponse.getStatusCode().is2xxSuccessful());
        assertNotNull(getResponse.getBody());
    }

    @Test
    void getReturns404IfOrderWasNotFound() {
        assertThrows(ExpectedHttpStatusException.class, () -> {
            restClient.get()
                    .uri(uriBase + ORDER_RESOURCE_PATH + "/{id}", "???")
                    .accept(APPLICATION_JSON)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (request, response) -> {
                        throw new ExpectedHttpStatusException();
                    })
                    .toEntity(Order.class);
        });
    }

    @Test
    void postCreatesOrder() {
        ResponseEntity<Order> response = restClient.post()
                .uri(uriBase + ORDER_RESOURCE_PATH)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(DummyData.buildOrder("1", createVwBeetleProduct()))
                .retrieve()
                .toEntity(Order.class);
        assertTrue(response.getStatusCode().is2xxSuccessful());

        assertNotNull(requireNonNull(response.getBody()).getId());
        assertEquals(0, requireNonNull(response.getBody()).getVersion());
    }

    @Test
    void putUpdatesOrder() {
        // create order
        ResponseEntity<Order> createResponse = restClient.post()
                .uri(uriBase + ORDER_RESOURCE_PATH)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(DummyData.buildOrder("1", createVwBeetleProduct()))
                .retrieve()
                .toEntity(Order.class);
        assertTrue(createResponse.getStatusCode().is2xxSuccessful());

        Order order = requireNonNull(createResponse.getBody());
        assertEquals(0, order.getVersion());

        // update order
        order.getItems().add(
                OrderItem.builder().product(createMercedesBenzVitoProduct()).quantity(1).build());
        ResponseEntity<Order> updateResponse = restClient.put()
                .uri(uriBase + ORDER_RESOURCE_PATH)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(order)
                .retrieve()
                .toEntity(Order.class);
        assertTrue(updateResponse.getStatusCode().is2xxSuccessful());

        Order updatedOrder = requireNonNull(updateResponse.getBody());
        assertEquals(2, updatedOrder.getItems().size());
        assertEquals(1, updatedOrder.getVersion());
    }

    @Test
    void putReturns404IfOrderWasNotFound() {
        Order order = DummyData.buildOrder("1", createVwBeetleProduct());
        order.setId("999");
        order.setVersion(999);
        assertThrows(ExpectedHttpStatusException.class, () -> {
            restClient.put()
                    .uri(uriBase + ORDER_RESOURCE_PATH)
                    .contentType(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .body(order)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (request, response) -> {
                        throw new ExpectedHttpStatusException();
                    })
                    .toEntity(Order.class);
        });
    }

    @Test
    void putReturns412IfOrdersVersionHasChanged() {
        // create order
        ResponseEntity<Order> createResponse = restClient.post()
                .uri(uriBase + ORDER_RESOURCE_PATH)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(DummyData.buildOrder("1", createVwBeetleProduct()))
                .retrieve()
                .toEntity(Order.class);
        assertTrue(createResponse.getStatusCode().is2xxSuccessful());

        Order order = requireNonNull(createResponse.getBody());
        assertEquals(0, order.getVersion());

        // update order
        order.getItems().add(
                OrderItem.builder().product(createMercedesBenzVitoProduct()).quantity(1).build());
        ResponseEntity<Order> updateResponse = restClient.put()
                .uri(uriBase + ORDER_RESOURCE_PATH)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(order)
                .retrieve()
                .toEntity(Order.class);
        assertTrue(updateResponse.getStatusCode().is2xxSuccessful());

        // try to update stale version
        assertThrows(ExpectedHttpStatusException.class, () -> {
            restClient.put()
                    .uri(uriBase + ORDER_RESOURCE_PATH)
                    .contentType(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .body(order)
                    .retrieve()
                    .onStatus(status -> status.value() == 412, (request, response) -> {
                        throw new ExpectedHttpStatusException();
                    })
                    .toEntity(Order.class);
        });
    }

    private Product createVwBeetleProduct() {
        ResponseEntity<Product> createResponse = restClient.post()
                .uri(uriBase + ProductRestControllerTest.PRODUCT_RESOURCE_PATH)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(DummyData.buildVwBeetleProduct())
                .retrieve()
                .toEntity(Product.class);
        assertTrue(createResponse.getStatusCode().is2xxSuccessful());
        return createResponse.getBody();
    }

    private Product createMercedesBenzVitoProduct() {
        ResponseEntity<Product> createResponse = restClient.post()
                .uri(uriBase + ProductRestControllerTest.PRODUCT_RESOURCE_PATH)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(DummyData.buildMercedesBenzVitoProduct())
                .retrieve()
                .toEntity(Product.class);
        assertTrue(createResponse.getStatusCode().is2xxSuccessful());
        return createResponse.getBody();
    }
}
