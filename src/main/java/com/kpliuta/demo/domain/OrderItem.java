package com.kpliuta.demo.domain;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    private int quantity = 1;

    @DocumentReference
    @NotNull
    private Product product;
}
