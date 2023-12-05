package com.kpliuta.demo.web;

import com.kpliuta.demo.domain.Product;
import com.kpliuta.demo.repository.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(path = "/api/product")
@Tag(name = "Product", description = "Product API")
@RestController
@RequiredArgsConstructor
public class ProductRestController {

    private final ProductRepository productRepository;

    @Operation(summary = "Return a list of products", description = "Return a list of products")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Invalid page number or page size input", content = @Content)
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Product> findProducts(PageableRequest request) {
        Product product = new Product();
        product.setRemoved(false);
        return productRepository.findAll(
                Example.of(product),
                PageRequest.of(request.pageNumber(), request.pageSize())
        ).getContent();
    }

    @Operation(summary = "Return a single product", description = "Return a single product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Product findProduct(@PathVariable String id) {
        return productRepository.findById(id).orElseThrow(ObjectNotFoundException::new);
    }

    @Operation(summary = "Create a new product", description = "Create a new product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Validation exception", content = @Content)    // TODO
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Product createProduct(@RequestBody Product product) {
        return productRepository.save(product);
    }

    @Operation(summary = "Update an existing product", description = "Update an existing product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Validation exception", content = @Content),   // TODO
            @ApiResponse(responseCode = "404", description = "Product not found", content = @Content),
            @ApiResponse(responseCode = "412", description = "Product has been modified meanwhile", content = @Content)
    })
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Product updateProduct(@RequestBody Product product) {
        productRepository.findById(product.getId()).orElseThrow(ObjectNotFoundException::new);
        return productRepository.save(product);
    }


    @Operation(summary = "Delete a product", description = "Delete a product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation")
    })
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable String id) {
        productRepository.findAndSetRemovedById(id);
    }
}
