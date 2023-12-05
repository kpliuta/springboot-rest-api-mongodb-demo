package com.kpliuta.demo;

import com.kpliuta.demo.domain.Order;
import com.kpliuta.demo.domain.OrderItem;
import com.kpliuta.demo.domain.Product;

import java.math.BigDecimal;
import java.util.Arrays;

import static java.util.stream.Collectors.toList;

public final class DummyData {

    public static Order buildOrder(String customerId, Product... products) {
        return Order.builder()
                .customerId(customerId)
                .items(Arrays.stream(products)
                        .map(p -> OrderItem.builder().product(p).quantity(1).build())
                        .collect(toList()))
                .build();
    }

    public static Product buildMercedesBenzVitoProduct() {
        return Product.builder()
                .brand("Porsche")
                .name("911")
                .description("10% discount")
                .price(BigDecimal.valueOf(300_999.99))
                .build();
    }

    public static Product buildPorsche911Product() {
        return Product.builder()
                .brand("Mercedes-Benz")
                .name("Vito")
                .description("10% discount")
                .price(BigDecimal.valueOf(200_999.99))
                .build();
    }

    public static Product buildVwBeetleProduct() {
        return Product.builder()
                .brand("VW")
                .name("Beetle")
                .description("10% discount")
                .price(BigDecimal.valueOf(100_999.99))
                .build();
    }
}
