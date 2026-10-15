package com.udla.cachedemo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final int BATCH_SIZE = 5_000;
    private static final String[] CATEGORIES = {
            "Electronica", "Hogar", "Deportes", "Juguetes", "Libros",
            "Ropa", "Alimentos", "Automotriz", "Salud", "Jardineria"
    };
    private static final String INSERT_SQL =
            "insert into products (name, category, price, stock, description) values (?, ?, ?, ?, ?)";

    private final JdbcTemplate jdbc;
    private final int total;

    public DataSeeder(JdbcTemplate jdbc, @Value("${app.seed.total}") int total) {
        this.jdbc = jdbc;
        this.total = total;
    }

    @Override
    public void run(ApplicationArguments args) {
        Long existing = jdbc.queryForObject("select count(*) from products", Long.class);
        if (existing != null && existing > 0) {
            log.info("La tabla products ya tiene {} registros. No se generan datos nuevos.", existing);
            return;
        }

        log.info("Generando {} registros de prueba...", total);
        long start = System.nanoTime();
        Random random = new Random(42);
        List<Object[]> batch = new ArrayList<>(BATCH_SIZE);

        for (int i = 1; i <= total; i++) {
            batch.add(new Object[]{
                    "Producto " + i,
                    CATEGORIES[i % CATEGORIES.length],
                    BigDecimal.valueOf(random.nextInt(100, 100_000), 2),
                    random.nextInt(0, 500),
                    "Descripcion del producto numero " + i + " generado para la prueba de cache"
            });
            if (batch.size() == BATCH_SIZE) {
                jdbc.batchUpdate(INSERT_SQL, batch);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            jdbc.batchUpdate(INSERT_SQL, batch);
        }
        log.info("Listo: {} registros insertados en {} ms.", total, (System.nanoTime() - start) / 1_000_000);
    }
}
