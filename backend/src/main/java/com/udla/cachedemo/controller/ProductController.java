package com.udla.cachedemo.controller;

import com.udla.cachedemo.model.Product;
import com.udla.cachedemo.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    // ---------- DTOs ----------
    public record QueryResponse(String source, String category, int totalRecords,
                                double serverTimeMs, List<Product> preview) {
    }

    public record BenchmarkResponse(String category, int totalRecords, int runs,
                                    List<Double> noCacheMs, List<Double> cacheMs,
                                    double avgNoCacheMs, double avgCacheHitMs, double speedup) {
    }

    private record Timed<T>(T value, double ms) {
    }

    // ---------- Endpoints ----------
    @GetMapping("/categories")
    public List<String> categories() {
        return service.findCategories();
    }

    /* Lee TODOS los registros de la categoría directamente de la BD.
    @GetMapping("/products/no-cache")
    public QueryResponse noCache(@RequestParam String category,
                                 @RequestParam(defaultValue = "50") int limit) {
        Timed<List<Product>> result = time(() -> service.findByCategoryNoCache(category));
        return toResponse("BASE DE DATOS", category, result, limit);
    }

    /** Igual que el anterior, pero pasando por la caché de Spring. */
    @GetMapping("/products/cached")
    public QueryResponse cached(@RequestParam String category,
                                @RequestParam(defaultValue = "50") int limit) {
        Timed<List<Product>> result = time(() -> service.findByCategoryCached(category));
        return toResponse("CACHE", category, result, limit);
    }

    @DeleteMapping("/cache")
    public Map<String, String> clearCache() {
        service.clearCache();
        return Map.of("message", "Caché limpiada");
    }

    @GetMapping("/benchmark")
    public BenchmarkResponse benchmark(@RequestParam String category,
                                       @RequestParam(defaultValue = "5") int runs) {
        int n = Math.max(2, Math.min(runs, 20));

        List<Double> noCacheTimes = new ArrayList<>();
        int total = 0;
        for (int i = 0; i < n; i++) {
            Timed<List<Product>> t = time(() -> service.findByCategoryNoCache(category));
            noCacheTimes.add(t.ms());
            total = t.value().size();
        }

        service.clearCache();
        List<Double> cacheTimes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            cacheTimes.add(time(() -> service.findByCategoryCached(category)).ms());
        }

        double avgNoCache = average(noCacheTimes);
        double avgHit = average(cacheTimes.subList(1, cacheTimes.size())); // se excluye el cache miss
        double speedup = avgNoCache / Math.max(avgHit, 0.001);

        return new BenchmarkResponse(category, total, n, noCacheTimes, cacheTimes,
                round(avgNoCache), round(avgHit), round(speedup));
    }

    // ---------- Utilidades ----------
    private <T> Timed<T> time(Supplier<T> action) {
        long start = System.nanoTime();
        T value = action.get();
        return new Timed<>(value, round((System.nanoTime() - start) / 1_000_000.0));
    }

    private QueryResponse toResponse(String source, String category, Timed<List<Product>> t, int limit) {
        List<Product> all = t.value();
        int safeLimit = Math.max(0, Math.min(limit, 500));
        return new QueryResponse(source, category, all.size(), t.ms(),
                all.stream().limit(safeLimit).toList());
    }

    private double average(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}
