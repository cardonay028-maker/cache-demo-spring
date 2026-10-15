package com.udla.cachedemo.repository;

import com.udla.cachedemo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategory(String category);

    @Query("select distinct p.category from Product p order by p.category")
    List<String> findCategories();
}
