package com.kpliuta.demo.load;

import com.kpliuta.demo.domain.Order;
import com.kpliuta.demo.domain.OrderItem;
import com.kpliuta.demo.domain.Product;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static com.kpliuta.demo.DummyData.*;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class OrderUpdateSimulation extends Simulation {

    final static ObjectMapper objectMapper = new ObjectMapper();

    public OrderUpdateSimulation() {
        setUp(
                getInitializeDataScenario()
                        .injectOpen(atOnceUsers(1)),
                getAddProductToOrderScenario()
                        .injectOpen(nothingFor(Duration.ofSeconds(5)), rampUsersPerSec(10).to(100).during(Duration.ofMinutes(1))),
                getRemoveProductFromOrderScenario()
                        .injectOpen(nothingFor(Duration.ofSeconds(5)), rampUsersPerSec(10).to(100).during(Duration.ofMinutes(1)))
        ).protocols(getHttpProtocolBuilder());
    }

    private static ScenarioBuilder getInitializeDataScenario() {
        return scenario("Initialize data")
                .exec(http("create-product-1")
                        .post("/api/product")
                        .header("Content-Type", "application/json")
                        .body(StringBody(toJson(buildVwBeetleProduct())))
                        .check(status().is(200)))
                .exec(http("create-product-2")
                        .post("/api/product")
                        .header("Content-Type", "application/json")
                        .body(StringBody(toJson(buildMercedesBenzVitoProduct())))
                        .check(status().is(200)))
                .exec(http("create-product-3")
                        .post("/api/product")
                        .header("Content-Type", "application/json")
                        .body(StringBody(toJson(buildPorsche911Product())))
                        .check(status().is(200)))
                .exec(http("create-order")
                        .post("/api/order")
                        .header("Content-Type", "application/json")
                        .body(StringBody(toJson(buildOrder("1"))))
                        .check(status().is(200)))
                .exitHereIfFailed();
    }

    private static ScenarioBuilder getAddProductToOrderScenario() {
        Random random = new Random();
        return scenario("Add product to order")
                .exec(http("add-product-get-products")
                        .get("/api/product?pageNumber=0&pageSize=100")
                        .check(status().is(200))
                        .check(bodyString().saveAs("products")))
                .exec(http("add-product-get-orders")
                        .get("/api/order?pageNumber=0&pageSize=100")
                        .check(status().is(200))
                        .check(bodyString().saveAs("orders")))
                .exec(session -> {
                    List<Product> products = fromJson(session.getString("products"), new TypeReference<>() {
                    });
                    List<Order> orders = fromJson(session.getString("orders"), new TypeReference<>() {
                    });
                    Product product = products.get(random.nextInt(products.size()));
                    Order order = getUniqueElement(orders);
                    order.getItems().add(OrderItem.builder().product(product).quantity(1).build());
                    return session.set("updated-order", toJson(order));
                })
                .exec(http("add-product-update-order")
                        .put("/api/order")
                        .header("Content-Type", "application/json")
                        .body(StringBody(session -> session.getString("updated-order")))
                        .check(status().in(200, 412)))
                .exitHereIfFailed();
    }

    private static ScenarioBuilder getRemoveProductFromOrderScenario() {
        return scenario("Remove product from order")
                .exec(http("remove-product-get-orders")
                        .get("/api/order?pageNumber=0&pageSize=100")
                        .check(status().is(200))
                        .check(bodyString().saveAs("orders")))
                .exec(session -> {
                    List<Order> orders = fromJson(session.getString("orders"), new TypeReference<>() {
                    });
                    Order order = getUniqueElement(orders);
                    Iterator<OrderItem> iterator = order.getItems().iterator();
                    if (iterator.hasNext()) {
                        iterator.next();
                        iterator.remove();
                    }
                    return session.set("updated-order", toJson(order));
                })
                .exec(http("remove-product-update-order")
                        .put("/api/order")
                        .header("Content-Type", "application/json")
                        .body(StringBody(session -> session.getString("updated-order")))
                        .check(status().in(200, 412)))
                .exitHereIfFailed();
    }

    private static HttpProtocolBuilder getHttpProtocolBuilder() {
        return http.baseUrl("http://localhost:8080")
                .acceptHeader("application/json");
    }

    private static Order getUniqueElement(List<Order> orders) {
        if (orders.size() != 1) {
            throw new RuntimeException("It should be exactly one Order in the database");
        }
        return orders.get(0);
    }

    private static <T> String toJson(T object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T fromJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> List<T> fromJson(String json, TypeReference<List<T>> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
