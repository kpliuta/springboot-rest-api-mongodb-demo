package com.kpliuta.demo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

@Getter
@Setter
@ToString
public abstract class Entity {

    @Id
    @NotNull
    private String id;

    @Version
    @NotNull
    private Integer version;

    @Hidden
    @JsonIgnore
    private boolean removed;
}
