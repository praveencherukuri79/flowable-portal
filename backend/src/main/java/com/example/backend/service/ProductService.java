package com.example.backend.service;

import com.example.backend.dto.ProductDto;
import com.example.backend.model.Product;

import java.util.List;

public interface ProductService {
    Product createProduct(ProductDto productDto);
    Product updateProduct(Long id, ProductDto productDto);
    void deleteProduct(Long id);
    Product getProduct(Long id);
    List<Product> getProductsBySheet(String sheetId);
    void approveProduct(Long productId, String approvedBy);
    void rejectProduct(Long productId, String approvedBy, String comments);
    void approveAllProducts(String sheetId, String approvedBy);
    List<Product> saveProductsFromTask(String sheetId, List<ProductDto> products, String editedBy);
}

