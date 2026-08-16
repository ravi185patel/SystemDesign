package com.example.distributedcache.repository;

import com.example.distributedcache.model.Product;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Product> findById(Long id) {

        String sql = """
                SELECT
                    id,
                    name,
                    description,
                    price,
                    created_at,
                    updated_at
                FROM products
                WHERE id = ?
                """;

        List<Product> products =
                jdbcTemplate.query(
                        sql,
                        this::mapRow,
                        id
                );

        return products.stream().findFirst();
    }

    public Product create(Product product) {

        String sql = """
                INSERT INTO products
                (
                    name,
                    description,
                    price
                )
                VALUES (?, ?, ?)
                RETURNING
                    id,
                    name,
                    description,
                    price,
                    created_at,
                    updated_at
                """;

        return jdbcTemplate.queryForObject(
                sql,
                this::mapRow,
                product.name(),
                product.description(),
                product.price()
        );
    }

    public boolean update(
            Long id,
            Product product
    ) {

        String sql = """
                UPDATE products
                SET
                    name = ?,
                    description = ?,
                    price = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;

        int rows =
                jdbcTemplate.update(
                        sql,
                        product.name(),
                        product.description(),
                        product.price(),
                        id
                );

        return rows > 0;
    }

    public boolean delete(Long id) {

        String sql = """
                DELETE FROM products
                WHERE id = ?
                """;

        return jdbcTemplate.update(
                sql,
                id
        ) > 0;
    }

    private Product mapRow(
            java.sql.ResultSet rs,
            int rowNum
    ) throws java.sql.SQLException {

        return new Product(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getBigDecimal("price"),
                rs.getTimestamp("created_at")
                        .toLocalDateTime(),
                rs.getTimestamp("updated_at")
                        .toLocalDateTime()
        );
    }
}