package com.kpliuta.demo.web;

import com.kpliuta.demo.domain.Order;
import com.kpliuta.demo.repository.OrderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(path = "/api/order")
@Tag(name = "Order", description = "Order API")
@RestController
@RequiredArgsConstructor
public class OrderRestController {

    private final OrderRepository orderRepository;

    @Operation(summary = "Return a list of orders", description = "Return a list of orders",
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "pageNumber", description = "Page number", required = true),
                    @Parameter(in = ParameterIn.QUERY, name = "pageSize", description = "Page size", required = true)
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Invalid page number or page size input", content = @Content)
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Order> findOrders(@Schema(hidden = true) PageableRequest request) {
        return orderRepository.findAll(PageRequest.of(request.pageNumber(), request.pageSize())).getContent();
    }

    @Operation(summary = "Return a single order", description = "Return a single order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    })
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Order findOrder(@PathVariable String id) {
        return orderRepository.findById(id).orElseThrow(ObjectNotFoundException::new);
    }

    @Operation(summary = "Create a new order", description = "Create a new order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Validation exception", content = @Content)    // TODO
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Order createOrder(@RequestBody Order order) {
        return orderRepository.save(order);
    }

    @Operation(summary = "Update an existing order", description = "Update an existing order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Validation exception", content = @Content),   // TODO
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content),
            @ApiResponse(responseCode = "412", description = "Order has been modified meanwhile", content = @Content)
    })
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Order updateOrder(@RequestBody Order order) {
        orderRepository.findById(order.getId()).orElseThrow(ObjectNotFoundException::new);
        return orderRepository.save(order);
    }


    @Operation(summary = "Delete an order", description = "Delete an order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation")
    })
    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable String id) {
        orderRepository.deleteById(id);
    }
}
