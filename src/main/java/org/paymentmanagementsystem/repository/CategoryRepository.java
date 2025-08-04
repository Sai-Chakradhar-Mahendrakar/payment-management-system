package org.paymentmanagementsystem.repository;

import org.paymentmanagementsystem.config.DatabaseConfig;
import org.paymentmanagementsystem.model.Category;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryRepository {
    private final DatabaseConfig dbConfig;

    public CategoryRepository() throws SQLException, IOException {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    public List<Category> findAll() throws SQLException {
        String sql = "SELECT category_id, category_name FROM categories";

        Connection conn = null;
        List<Category> categories = new ArrayList<>();

        try {
            conn = dbConfig.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Category category = new Category();
                category.setCategoryId(rs.getInt("category_id"));
                category.setCategoryName(rs.getString("category_name"));
                categories.add(category);
            }
            return categories;
        } finally {
            dbConfig.returnConnection(conn);
        }
    }

    public Optional<Category> findById(int categoryId) throws SQLException {
        String sql = "SELECT category_id, category_name FROM categories WHERE category_id = ?";

        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, categoryId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Category category = new Category();
                category.setCategoryId(rs.getInt("category_id"));
                category.setCategoryName(rs.getString("category_name"));
                return Optional.of(category);
            }
            return Optional.empty();
        } finally {
            dbConfig.returnConnection(conn);
        }
    }

    public Category save(Category category) throws SQLException {
        String sql = "INSERT INTO categories (category_name) VALUES (?) RETURNING category_id";

        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, category.getCategoryName());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                category.setCategoryId(rs.getInt("category_id"));
            }
            return category;
        } finally {
            dbConfig.returnConnection(conn);
        }
    }

    public Category findByName(String categoryName) throws SQLException {
        String sql = "SELECT * FROM categories WHERE category_name = ?";

        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, categoryName);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Category category = new Category();
                        category.setCategoryId(rs.getInt("category_id"));
                        category.setCategoryName(rs.getString("category_name"));
                        return category;
                    }
                }
            }
        } finally {
            dbConfig.returnConnection(conn);
        }
        return null;
    }

    public Category createCategory(String categoryName) throws SQLException {
        String sql = "INSERT INTO categories (category_name) VALUES (?) RETURNING category_id";

        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, categoryName);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Category category = new Category();
                        category.setCategoryId(rs.getInt("category_id"));
                        category.setCategoryName(categoryName);
                        return category;
                    }
                }
            }
        } finally {
            dbConfig.returnConnection(conn);
        }
        return null;
    }
}
