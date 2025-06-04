package laptopstore.data;

import java.math.BigDecimal;
// import java.math.RoundingMode; // Not directly used in this file's new methods
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
// import java.time.LocalDate; // Not directly used in this file's new methods
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate; // Import LocalDate for NQ16

import laptopstore.model.Product;
import laptopstore.util.DatabaseConnection;

public class ProductDataStore {

    // --- Các phương thức CRUD và truy vấn cũ giữ nguyên ---
    public Product addProduct(Product product) throws SQLException {
        if (product == null) throw new IllegalArgumentException("Product object cannot be null.");
        if (product.getSpecificProductName() == null || product.getSpecificProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên sản phẩm (specificProductName) không được để trống.");
        }
        if (product.getModel() == null || product.getModel().trim().isEmpty()) throw new IllegalArgumentException("Model is required.");
        if (product.getBrand() == null || product.getBrand().trim().isEmpty()) throw new IllegalArgumentException("Brand is required.");
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Price must be non-negative.");
        if (product.getStockQuantity() < 0) throw new IllegalArgumentException("Stock quantity must be non-negative.");

        String sql = "INSERT INTO PRODUCTS (product_name, model, brand, description, price, stock_quantity, year_publish, category_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, product.getSpecificProductName().trim());
            pstmt.setString(2, product.getModel().trim());
            pstmt.setString(3, product.getBrand().trim());
            pstmt.setString(4, product.getDescription());
            pstmt.setBigDecimal(5, product.getPrice());
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

        String sql = "UPDATE PRODUCTS SET product_name = ?, model = ?, brand = ?, description = ?, price = ?, " +
                "stock_quantity = ?, year_publish = ?, category_id = ? " +
                "WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getSpecificProductName().trim());
            pstmt.setString(2, product.getModel().trim());
            pstmt.setString(3, product.getBrand().trim());
            pstmt.setString(4, product.getDescription());
            pstmt.setBigDecimal(5, product.getPrice());
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

    public List<Product> getProductsByCategoryId(int categoryId) throws SQLException {
        if (categoryId <= 0) return new ArrayList<>();
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.product_id, p.product_name, p.model, p.brand, p.description, p.price, p.stock_quantity, p.year_publish, p.category_id, c.category_name " +
                "FROM PRODUCTS p LEFT JOIN CATEGORIES c ON p.category_id = c.category_id " +
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
        } catch (SQLException e) {
            System.err.println("Error fetching latest " + limit + " products: " + e.getMessage());
            throw e;
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
                "GROUP BY p.product_id, c.category_name " + // Phải group by tất cả các cột non-aggregated của p
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
                "SUM(od.quantity) as total_sold_for_ranking, " +
                "SUM(od.quantity * p.price) as total_revenue_for_ranking " +
                "FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.category_id " +
                "JOIN order_details od ON p.product_id = od.product_id " +
                "GROUP BY p.product_id, c.category_name " + // Phải group by tất cả các cột non-aggregated của p
                "ORDER BY total_sold_for_ranking DESC LIMIT 5";
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
                "p.stock_quantity as current_stock_alias, " +
                "COALESCE(SUM(od.quantity), 0) as total_sold_alias, " +
                "COALESCE(SUM(od.quantity * p.price), 0) as total_revenue_alias " +
                "FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.category_id " +
                "LEFT JOIN order_details od ON p.product_id = od.product_id " +
                "GROUP BY p.product_id, c.category_name " + // Phải group by tất cả các cột non-aggregated của p
                "ORDER BY total_revenue_alias DESC";
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

    public List<Product> getLowStockProducts(int threshold) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.product_id, p.product_name, p.model, p.brand, c.category_name, p.stock_quantity, p.price " +
                "FROM PRODUCTS p " +
                "LEFT JOIN CATEGORIES c ON p.category_id = c.category_id " +
                "WHERE p.stock_quantity < ? " +
                "ORDER BY p.stock_quantity ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, threshold);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRowToProduct(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy sản phẩm tồn kho thấp: " + e.getMessage());
            throw e;
        }
        return products;
    }

    public List<Map<String, Object>> getTopProductsByReturnRate(int limit, String returnedStatus) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "WITH ProductSales AS (" +
                "    SELECT " +
                "        od.product_id, " +
                "        COUNT(DISTINCT od.order_id) AS total_sold_orders " +
                "    FROM ORDER_DETAILS od " +
                "    GROUP BY od.product_id " +
                "), ProductReturns AS (" +
                "    SELECT " +
                "        od.product_id, " +
                "        COUNT(DISTINCT o.order_id) AS total_returned_orders " +
                "    FROM ORDERS o " +
                "    JOIN ORDER_DETAILS od ON o.order_id = od.order_id " +
                "    WHERE o.status = ? " +
                "    GROUP BY od.product_id " +
                ") " +
                "SELECT " +
                "    p.product_id, " +
                "    p.product_name, " +
                "    COALESCE(ps.total_sold_orders, 0) AS sold_in_orders, " +
                "    COALESCE(pr.total_returned_orders, 0) AS returned_in_orders, " +
                "    CASE " +
                "        WHEN COALESCE(ps.total_sold_orders, 0) > 0 " +
                "        THEN ROUND((COALESCE(pr.total_returned_orders, 0) * 100.0 / ps.total_sold_orders), 2) " +
                "        ELSE 0 " +
                "    END AS return_rate_percentage " +
                "FROM PRODUCTS p " +
                "LEFT JOIN ProductSales ps ON p.product_id = ps.product_id " +
                "LEFT JOIN ProductReturns pr ON p.product_id = pr.product_id " +
                "WHERE COALESCE(ps.total_sold_orders, 0) > 0 " +
                "ORDER BY return_rate_percentage DESC, returned_in_orders DESC " +
                "LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, returnedStatus);
            pstmt.setInt(2, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("product_id", rs.getInt("product_id"));
                    row.put("product_name", rs.getString("product_name"));
                    row.put("sold_in_orders", rs.getInt("sold_in_orders"));
                    row.put("returned_in_orders", rs.getInt("returned_in_orders"));
                    row.put("return_rate_percentage", rs.getBigDecimal("return_rate_percentage"));
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy top sản phẩm theo tỷ lệ trả hàng: " + e.getMessage());
            throw e;
        }
        return results;
    }

    public List<Map<String, Object>> getTop20SellingProductsByRevenue(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT " +
                "    p.product_name, " +
                "    SUM(od.quantity) AS quantity_sold, " +
                "    SUM(od.quantity * p.price) AS revenue, " +
                "    c.category_name " +
                "FROM ORDERS o " +
                "JOIN ORDER_DETAILS od ON o.order_id = od.order_id " +
                "JOIN PRODUCTS p ON od.product_id = p.product_id " +
                "LEFT JOIN CATEGORIES c ON p.category_id = c.category_id " +
                "WHERE o.order_date BETWEEN ? AND ? " +
                "GROUP BY p.product_id, p.product_name, c.category_name " +
                "ORDER BY revenue DESC " +
                "LIMIT 20";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(startDate));
            pstmt.setDate(2, java.sql.Date.valueOf(endDate));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("product_name", rs.getString("product_name"));
                    row.put("quantity_sold", rs.getInt("quantity_sold"));
                    row.put("revenue", rs.getBigDecimal("revenue"));
                    row.put("category_name", rs.getString("category_name"));
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy top 20 sản phẩm bán chạy theo doanh thu: " + e.getMessage());
            throw e;
        }
        return results;
    }

    // NQ11: Số lượng sản phẩm theo từng Category
    public List<Map<String, Object>> getProductCountByCategory() throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT c.category_name, COUNT(p.product_id) AS product_count " +
                "FROM CATEGORIES c " +
                "LEFT JOIN PRODUCTS p ON c.category_id = p.category_id " +
                "GROUP BY c.category_id, c.category_name " +
                "ORDER BY product_count DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("category_name", rs.getString("category_name"));
                row.put("product_count", rs.getInt("product_count"));
                results.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi đếm sản phẩm theo danh mục: " + e.getMessage());
            throw e;
        }
        return results;
    }

    // NQ16: Sản phẩm được công bố/nhập trong N năm gần nhất.
    public List<Product> getRecentlyPublishedProductsInLastNYears(int years) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.category_name " +
                "FROM PRODUCTS p " +
                "LEFT JOIN CATEGORIES c ON p.category_id = c.category_id " +
                "WHERE p.year_publish >= (CURRENT_DATE - CAST(? || ' years' AS INTERVAL)) " +
                "ORDER BY p.year_publish DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, years);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRowToProduct(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy sản phẩm mới ra mắt trong " + years + " năm: " + e.getMessage());
            throw e;
        }
        return products;
    }


    // NQ19: Sản phẩm có giá trong một khoảng nhất định
    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.category_name " +
                "FROM PRODUCTS p " +
                "LEFT JOIN CATEGORIES c ON p.category_id = c.category_id " +
                "WHERE p.price BETWEEN ? AND ? " +
                "ORDER BY p.price ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, minPrice);
            pstmt.setBigDecimal(2, maxPrice);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRowToProduct(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy sản phẩm theo khoảng giá: " + e.getMessage());
            throw e;
        }
        return products;
    }

    private Product mapRowToProduct(ResultSet rs) throws SQLException {
        int id = rs.getInt("product_id");
        String specificProductName = rs.getString("product_name");
        String model = null;
        try { model = rs.getString("model"); } catch (SQLException e) {/* ignore if not present */}
        String brand = null;
        try { brand = rs.getString("brand"); } catch (SQLException e) {/* ignore if not present */}
        String description = null;
        try { description = rs.getString("description"); } catch (SQLException e) {/* ignore if not present */}

        BigDecimal priceBd = null;
        try { priceBd = rs.getBigDecimal("price"); } catch (SQLException e) {/* ignore if not present */}

        int stockQuantity = 0;
        try { stockQuantity = rs.getInt("stock_quantity"); } catch (SQLException e) {/* ignore if not present */}

        Timestamp yearPublishTs = null;
        try { yearPublishTs = rs.getTimestamp("year_publish"); } catch (SQLException e) {/* ignore if not present */}
        LocalDateTime yearPublish = (yearPublishTs != null) ? yearPublishTs.toLocalDateTime() : null;

        Integer categoryId = null;
        String categoryName = null;
        try { categoryId = rs.getObject("category_id", Integer.class); } catch (SQLException e) { /* ignore */ }
        try { categoryName = rs.getString("category_name"); } catch (SQLException e) { /* ignore */ }

        Product p = new Product(id, specificProductName, model, brand, description, priceBd, stockQuantity, yearPublish, categoryId);
        if (categoryName != null) {
            p.setCategoryName(categoryName);
        }
        return p;
    }
}
