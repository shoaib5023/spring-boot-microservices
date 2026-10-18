package com.example.product_service.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import com.example.product_service.entity.Product;
import com.example.product_service.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PostMapping
    public Product createProduct(
            @Valid @RequestBody Product product) {
        return productService.createProduct(product);
    }
}