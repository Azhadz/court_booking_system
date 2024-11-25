package com.store.runcitStore.services;

import com.store.runcitStore.models.Product;
import com.store.runcitStore.services.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // Fetch all products with sorting
    public List<Product> findAll() {
        return productRepository.findAll(Sort.by("id"));
    }

    // Search for products by name or category
    public List<Product> searchByNameOrCategory(String query) {
        return productRepository.findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(query, query);
    }
}