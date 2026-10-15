package com.udla.cachedemo.service;

import com.udla.cachedemo.model.Product;
import com.udla.cachedemo.repository.ProductRepository;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    public static final String CACHE_NAME = "productsByCategory";

    private final ProductRepository repository;
    private final CacheManager cacheManager;

    public ProductService(ProductRepository repository, CacheManager cacheManager) {
        this.repository = repository;
        this.cacheManager = cacheManager;
    }

    /** Siempre consulta la base de datos. */
    @Transactional(readOnly = true)
    public List<Product> findByCategoryNoCache(String category) {
        return repository.findByCategory(category);
    }


    @Cacheable(cacheNames = CACHE_NAME, key = "#category")
    @Transactional(readOnly = true)
    public List<Product> findByCategoryCached(String category) {
        return List.copyOf(repository.findByCategory(category));
    }

    @Transactional(readOnly = true)
    public List<String> findCategories() {
        return repository.findCategories();
    }

    public void clearCache() {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.clear();
        }
    }
}
