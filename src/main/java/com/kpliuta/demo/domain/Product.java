package com.kpliuta.demo.domain;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product extends Entity {

    @NotNull
    private String brand;

    @NotNull
    private String name;

    private String description;

    @NotNull
    private BigDecimal price;
}
