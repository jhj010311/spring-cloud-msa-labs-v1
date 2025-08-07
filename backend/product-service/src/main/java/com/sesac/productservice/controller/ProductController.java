package com.sesac.productservice.controller;

import com.sesac.productservice.entity.Product;
import com.sesac.productservice.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "상품 컨트롤러")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "상품 전체조회", description = "모든 상품 정보를 조회합니다")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "상품 조회", description = "ID로 상품 정보를 조회합니다")
    public ResponseEntity<Product> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }
}
