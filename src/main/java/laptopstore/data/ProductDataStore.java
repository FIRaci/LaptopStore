package laptopstore.data;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import laptopstore.model.Product;
import laptopstore.util.DatabaseConnection;

public class ProductDataStore {
    
    private void resetProductSequence() throws SQLException {
        String sql = "SELECT setval('products_product_id_seq', COALESCE((SELECT MAX(product_id) FROM products), 0))";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    // Sử dụng constructor của Product đã được cập nhật để nhận các trường mới
    public Product addProduct(Product product) throws SQLException {
        if (product == null) throw new IllegalArgumentException("Product object cannot be null.");
        if (product.getSpecificProductName() == null || product.getSpecificProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên sản phẩm (specificProductName) không được để trống.");
        }
        if (product.getModel() == null || product.getModel().trim().isEmpty()) throw new IllegalArgumentException("Model is required.");
        if (product.getBrand() == null || product.getBrand().trim().isEmpty()) throw new IllegalArgumentException("Brand is required.");
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Price must be non-negative.");
        if (product.getStockQuantity() < 0) throw new IllegalArgumentException("Stock quantity must be non-negative.");

        // Reset sequence trước khi thêm 
        resetProductSequence();

        String sql = "INSERT INTO PRODUCTS (product_name, model, brand, description, price, stock_quantity, year_publish, category_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, product.getSpecificProductName().trim());
            pstmt.setString(2, product.getModel().trim());
            pstmt.setString(3, product.getBrand().trim());
            pstmt.setString(4, product.getDescription());
            pstmt.setBigDecimal(5, product.getPrice()); // product.getPrice() đã là BigDecimal
            pstmt.setInt(6, product.getStockQuantity());

            if (product.getYearPublish() != null) {
                pstmt.setTimestamp(7, Timestamp.valueOf(product.getYearPublish()));
            } else {
                pstmt.setNull(7, Types.TIMESTAMP);
            }

            if (product.getCategoryId() != null && product.getCategoryId() > 0) {
                pstmt.setInt(8, product.getCategoryId());
            } else {
                pstmt.setNull(8, Types.INTEGER);
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        product.setProductId(generatedKeys.getInt(1));
                        // Không cần set categoryName ở đây vì INSERT không trả về category_name
                        return product;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi thêm sản phẩm: " + e.getMessage());
            throw e;
        }
        return null;
    }

    public boolean updateProduct(Product product) throws SQLException {
        if (product == null) throw new IllegalArgumentException("Product object cannot be null.");
        if (product.getProductId() <= 0) throw new IllegalArgumentException("Product ID không hợp lệ để cập nhật.");
        // Thêm validate tương tự addProduct

        String sql = "UPDATE PRODUCTS SET product_name = ?, model = ?, brand = ?, description = ?, price = ?, " +
                "stock_quantity = ?, year_publish = ?, category_id = ? " +
                "WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getSpecificProductName().trim());
            pstmt.setString(2, product.getModel().trim());
            pstmt.setString(3, product.getBrand().trim());
            pstmt.setString(4, product.getDescription());
            pstmt.setBigDecimal(5, product.getPrice()); // product.getPrice() là BigDecimal
            pstmt.setInt(6, product.getStockQuantity());
            if (product.getYearPublish() != null) {
                pstmt.setTimestamp(7, Timestamp.valueOf(product.getYearPublish()));
            } else {
                pstmt.setNull(7, Types.TIMESTAMP);
            }
            if (product.getCategoryId() != null && product.getCategoryId() > 0) {
                pstmt.setInt(8, product.getCategoryId());
            } else {
                pstmt.setNull(8, Types.INTEGER);
            }
            pstmt.setInt(9, product.getProductId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi cập nhật sản phẩm: " + e.getMessage());
            throw e;
        }
    }

    public boolean deleteProduct(int productId) throws SQLException {
        if (productId <= 0) throw new IllegalArgumentException("Product ID không hợp lệ để xóa.");
        String sql = "DELETE FROM PRODUCTS WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new SQLException("Không thể xóa sản phẩm ID " + productId + " vì nó đang được tham chiếu trong chi tiết đơn hàng.", e.getSQLState(), e);
            }
            System.err.println("Lỗi SQL khi xóa sản phẩm ID " + productId + ": " + e.getMessage());
            throw e;
        }
    }

    public Product getProductById(int productId) throws SQLException {
        if (productId <= 0) return null;
        String sql = "SELECT p.product_id, p.product_name, p.model, p.brand, p.description, p.price, p.stock_quantity, p.year_publish, p.category_id, c.category_name " +
                "FROM PRODUCTS p LEFT JOIN CATEGORIES c ON p.category_id = c.category_id " +
                "WHERE p.product_id = ?";
        Product product = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    product = mapRowToProduct(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy sản phẩm theo ID " + productId + ": " + e.getMessage());
            throw e;
        }
        return product;
    }

    public List<Product> getAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.product_id, p.product_name, p.model, p.brand, p.description, p.price, p.stock_quantity, p.year_publish, p.category_id, c.category_name " +
                "FROM PRODUCTS p LEFT JOIN CATEGORIES c ON p.category_id = c.category_id " +
                "ORDER BY p.product_name, p.product_id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(mapRowToProduct(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy tất cả sản phẩm: " + e.getMessage());
            throw e;
        }
        return products;
    }

    public List<Product> getProductsByType(String productType) throws SQLException {
        if (productType == null || productType.trim().isEmpty()) return getAllProducts();
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.product_id, p.product_name, p.model, p.brand, p.description, p.price, p.stock_quantity, p.year_publish, p.category_id, c.category_name " +
                "FROM PRODUCTS p LEFT JOIN CATEGORIES c ON p.category_id = c.category_id " +
                "WHERE p.product_type = ? ORDER BY p.product_name, p.product_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, productType);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRowToProduct(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy sản phẩm theo loại '" + productType + "': " + e.getMessage());
            throw e;
        }
        return products;
    }

    public List<Product> getProductsByCategoryId(int categoryId) throws SQLException {
        if (categoryId <= 0) return new ArrayList<>();
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.product_id, p.product_name, p.model, p.brand, p.description, p.price, p.stock_quantity, p.year_publish, p.category_id, c.category_name " +
                "FROM PRODUCTS p LEFT JOIN CATEGORIES c ON p.category_id = c.category_id " + // Dù lọc theo category_id, vẫn join để lấy tên cho nhất quán
                "WHERE p.category_id = ? ORDER BY p.product_name, p.product_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, categoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRowToProduct(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy sản phẩm theo category ID " + categoryId + ": " + e.getMessage());
            throw e;
        }
        return products;
    }

    public List<Product> getLatestProducts(int limit) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.product_id, p.product_name, p.model, p.brand, p.description, " +
                "p.price, p.stock_quantity, p.year_publish, p.category_id, " +
                "c.category_name " +
                "FROM PRODUCTS p LEFT JOIN CATEGORIES c ON p.category_id = c.category_id " +
                "ORDER BY p.year_publish DESC NULLS LAST, p.product_id DESC LIMIT ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRowToProduct(rs));
                }
            }
        }
        return products;
    }

    public List<Product> getProductsWithRecentOrders() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.category_name, COUNT(od.order_id) as order_count " +
                    "FROM products p " +
                    "LEFT JOIN categories c ON p.category_id = c.category_id " +
                    "LEFT JOIN order_details od ON p.product_id = od.product_id " +
                    "LEFT JOIN orders o ON od.order_id = o.order_id " +
                    "WHERE o.order_date >= CURRENT_DATE - INTERVAL '30 days' OR o.order_date IS NULL " +
                    "GROUP BY p.product_id, c.category_name " +
                    "ORDER BY order_count DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(mapRowToProduct(rs));
            }
        }
        return products;
    }

    public List<Product> getTopSellingProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.category_name, " +
                    "SUM(od.quantity) as total_sold, " +
                    "SUM(od.quantity * od.unit_price) as total_revenue " +
                    "FROM products p " +
                    "LEFT JOIN categories c ON p.category_id = c.category_id " +
                    "JOIN order_details od ON p.product_id = od.product_id " +
                    "GROUP BY p.product_id, c.category_name " +
                    "ORDER BY total_sold DESC LIMIT 5";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(mapRowToProduct(rs));
            }
        }
        return products;
    }

    public List<Product> getNeverSoldProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.category_name " +
                    "FROM products p " +
                    "LEFT JOIN categories c ON p.category_id = c.category_id " +
                    "LEFT JOIN order_details od ON p.product_id = od.product_id " +
                    "WHERE od.order_id IS NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(mapRowToProduct(rs));
            }
        }
        return products;
    }

    public List<Product> getProductsWithInventoryAndRevenue() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.category_name, " +
                    "p.stock_quantity as current_stock, " +
                    "COALESCE(SUM(od.quantity), 0) as total_sold, " +
                    "COALESCE(SUM(od.quantity * od.unit_price), 0) as total_revenue " +
                    "FROM products p " +
                    "LEFT JOIN categories c ON p.category_id = c.category_id " +
                    "LEFT JOIN order_details od ON p.product_id = od.product_id " +
                    "GROUP BY p.product_id, c.category_name " +
                    "ORDER BY total_revenue DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(mapRowToProduct(rs));
            }
        }
        return products;
    }

    public List<Product> getProductsWithPendingOrders() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT DISTINCT p.*, c.category_name " +
                    "FROM products p " +
                    "LEFT JOIN categories c ON p.category_id = c.category_id " +
                    "JOIN order_details od ON p.product_id = od.product_id " +
                    "JOIN orders o ON od.order_id = o.order_id " +
                    "WHERE o.status IN ('Processing', 'Pending') " +
                    "ORDER BY p.product_id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(mapRowToProduct(rs));
            }
        }
        return products;
    }

    private Product mapRowToProduct(ResultSet rs) throws SQLException {
        int id = rs.getInt("product_id");
        String specificProductName = rs.getString("product_name");
        String model = rs.getString("model");
        String brand = rs.getString("brand");
        String description = rs.getString("description");
        BigDecimal priceBd = rs.getBigDecimal("price"); // Lấy trực tiếp BigDecimal
        int stockQuantity = rs.getInt("stock_quantity");
        Timestamp yearPublishTs = rs.getTimestamp("year_publish");
        LocalDateTime yearPublish = (yearPublishTs != null) ? yearPublishTs.toLocalDateTime() : null;
        Integer categoryId = rs.getObject("category_id", Integer.class);
        String categoryName = rs.getString("category_name"); // Lấy từ JOIN

        Product p = new Product(id, specificProductName, model, brand, description, priceBd, stockQuantity, yearPublish, categoryId);
        p.setCategoryName(categoryName); // Gán tên category

        return p;
    }
}
