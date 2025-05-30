package laptopstore.data;

import laptopstore.model.Product;
import laptopstore.util.DatabaseConnection;

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

public class ProductDataStore {

    // Helper method to clean currency string and convert to BigDecimal
    private BigDecimal getBigDecimalFromMoneyString(ResultSet rs, String columnName) throws SQLException {
        String moneyString = rs.getString(columnName);
        if (moneyString == null) {
            return null;
        }
        // Loại bỏ ký tự không phải số, ngoại trừ dấu chấm thập phân và dấu trừ (nếu có cho số âm)
        // Cụ thể ở đây là loại bỏ dấu phẩy phân cách hàng nghìn.
        // PostgreSQL MONEY type có thể trả về dạng có ký hiệu tiền tệ ở đầu, ví dụ '$1,234.56' hoặc '1.234,56 €' tùy locale.
        // Cách làm an toàn nhất là chỉ giữ lại số và dấu chấm thập phân chuẩn.
        String cleanedString = moneyString.replaceAll("[^\\d.-]", "").replace(",", "");
        if (cleanedString.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(cleanedString);
        } catch (NumberFormatException e) {
            System.err.println("Error parsing BigDecimal from string: '" + moneyString + "' (cleaned: '" + cleanedString + "') for column: " + columnName);
            throw new SQLException("Bad value for type BigDecimal after cleaning: " + moneyString, e);
        }
    }

    public Product addProduct(Product product) throws SQLException {
        if (product == null) throw new IllegalArgumentException("Product object cannot be null.");
        if (product.getSpecificProductName() == null || product.getSpecificProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name (specificProductName) cannot be empty.");
        }
        if (product.getModel() == null || product.getModel().trim().isEmpty()) throw new IllegalArgumentException("Model is required.");
        if (product.getBrand() == null || product.getBrand().trim().isEmpty()) throw new IllegalArgumentException("Brand is required.");
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be a non-negative value.");
        }
        if (product.getStockQuantity() < 0) throw new IllegalArgumentException("Stock quantity must be non-negative.");

        String sql = "INSERT INTO PRODUCTS (product_name, model, brand, description, price, stock_quantity, year_publish, product_type, category_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
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

            pstmt.setString(8, product.getProductType());

            if (product.getCategoryId() != null && product.getCategoryId() > 0) {
                pstmt.setInt(9, product.getCategoryId());
            } else {
                pstmt.setNull(9, Types.INTEGER);
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
            System.err.println("SQL Error when adding product: " + e.getMessage());
            throw e;
        }
        return null;
    }

    public boolean updateProduct(Product product) throws SQLException {
        if (product == null) throw new IllegalArgumentException("Product object cannot be null.");
        if (product.getProductId() <= 0) throw new IllegalArgumentException("Invalid Product ID for update.");

        String sql = "UPDATE PRODUCTS SET product_name = ?, model = ?, brand = ?, description = ?, price = ?, " +
                "stock_quantity = ?, year_publish = ?, product_type = ?, category_id = ? " +
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
            pstmt.setString(8, product.getProductType());
            if (product.getCategoryId() != null && product.getCategoryId() > 0) {
                pstmt.setInt(9, product.getCategoryId());
            } else {
                pstmt.setNull(9, Types.INTEGER);
            }
            pstmt.setInt(10, product.getProductId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error when updating product: " + e.getMessage());
            throw e;
        }
    }

    public boolean deleteProduct(int productId) throws SQLException {
        if (productId <= 0) throw new IllegalArgumentException("Invalid Product ID for deletion.");
        String sql = "DELETE FROM PRODUCTS WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new SQLException("Cannot delete product ID " + productId + " as it is referenced in order details.", e.getSQLState(), e);
            }
            System.err.println("SQL Error when deleting product ID " + productId + ": " + e.getMessage());
            throw e;
        }
    }

    public Product getProductById(int productId) throws SQLException {
        if (productId <= 0) return null;
        String sql = "SELECT p.product_id, p.product_name, p.model, p.brand, p.description, p.price, p.stock_quantity, p.year_publish, p.product_type, p.category_id, c.category_name " +
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
            System.err.println("SQL Error when fetching product by ID " + productId + ": " + e.getMessage());
            throw e;
        }
        return product;
    }

    public List<Product> getAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.product_id, p.product_name, p.model, p.brand, p.description, p.price, p.stock_quantity, p.year_publish, p.product_type, p.category_id, c.category_name " +
                "FROM PRODUCTS p LEFT JOIN CATEGORIES c ON p.category_id = c.category_id " +
                "ORDER BY p.product_name, p.product_id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(mapRowToProduct(rs));
            }
        } catch (SQLException e) {
            System.err.println("SQL Error when fetching all products: " + e.getMessage());
            throw e;
        }
        return products;
    }

    public List<Product> getProductsByType(String productType) throws SQLException {
        if (productType == null || productType.trim().isEmpty()) return getAllProducts();
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.product_id, p.product_name, p.model, p.brand, p.description, p.price, p.stock_quantity, p.year_publish, p.product_type, p.category_id, c.category_name " +
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
            System.err.println("SQL Error when fetching products by type '" + productType + "': " + e.getMessage());
            throw e;
        }
        return products;
    }

    public List<Product> getProductsByCategoryId(int categoryId) throws SQLException {
        if (categoryId <= 0) return new ArrayList<>();
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.product_id, p.product_name, p.model, p.brand, p.description, p.price, p.stock_quantity, p.year_publish, p.product_type, p.category_id, c.category_name " +
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
            System.err.println("SQL Error when fetching products by category ID " + categoryId + ": " + e.getMessage());
            throw e;
        }
        return products;
    }

    private Product mapRowToProduct(ResultSet rs) throws SQLException {
        int id = rs.getInt("product_id");
        String specificProductName = rs.getString("product_name");
        String model = rs.getString("model");
        String brand = rs.getString("brand");
        String description = rs.getString("description");

        // SỬA Ở ĐÂY: Đọc MONEY từ CSDL
        BigDecimal priceBd = getBigDecimalFromMoneyString(rs, "price");

        int stockQuantity = rs.getInt("stock_quantity");
        Timestamp yearPublishTs = rs.getTimestamp("year_publish");
        LocalDateTime yearPublish = (yearPublishTs != null) ? yearPublishTs.toLocalDateTime() : null;
        String productType = rs.getString("product_type");
        Integer categoryId = rs.getObject("category_id", Integer.class);
        String categoryName = rs.getString("category_name");

        Product p = new Product(id, specificProductName, model, brand, description, priceBd, stockQuantity, yearPublish, productType, categoryId);
        p.setCategoryName(categoryName);

        return p;
    }
}
