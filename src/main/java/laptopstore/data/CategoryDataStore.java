package laptopstore.data;

import laptopstore.model.Category;
import laptopstore.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CategoryDataStore {

    public Category addCategory(Category category) throws SQLException {
        if (category == null || category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên category không được để trống.");
        }
        String sql = "INSERT INTO CATEGORIES (category_name) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, category.getCategoryName().trim());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        category.setCategoryId(generatedKeys.getInt(1));
                        return category;
                    }
                }
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) { // UNIQUE constraint violation
                throw new SQLException("Tên category '" + category.getCategoryName().trim() + "' đã tồn tại.", e.getSQLState(), e);
            }
            System.err.println("Lỗi SQL khi thêm category: " + e.getMessage());
            throw e;
        }
        return null;
    }

    public boolean updateCategory(Category category) throws SQLException {
        if (category == null || category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên category không được để trống khi cập nhật.");
        }
        if (category.getCategoryId() <= 0) {
            throw new IllegalArgumentException("Category ID không hợp lệ để cập nhật.");
        }
        String sql = "UPDATE CATEGORIES SET category_name = ? WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category.getCategoryName().trim());
            pstmt.setInt(2, category.getCategoryId());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new SQLException("Tên category '" + category.getCategoryName().trim() + "' đã được sử dụng bởi category khác.", e.getSQLState(), e);
            }
            System.err.println("Lỗi SQL khi cập nhật category: " + e.getMessage());
            throw e;
        }
    }

    public boolean deleteCategory(int categoryId) throws SQLException {
        if (categoryId <= 0) {
            throw new IllegalArgumentException("Category ID không hợp lệ để xóa.");
        }
        String sql = "DELETE FROM CATEGORIES WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, categoryId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new SQLException("Không thể xóa category này vì có ràng buộc khóa ngoại (ví dụ: sản phẩm đang sử dụng).", e.getSQLState(), e);
            }
            System.err.println("Lỗi SQL khi xóa category: " + e.getMessage());
            throw e;
        }
    }

    public Category getCategoryById(int categoryId) throws SQLException {
        if (categoryId <= 0) return null;
        String sql = "SELECT category_id, category_name FROM CATEGORIES WHERE category_id = ?";
        Category category = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, categoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    category = new Category(rs.getInt("category_id"), rs.getString("category_name"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy category theo ID " + categoryId + ": " + e.getMessage());
            throw e;
        }
        return category;
    }

    public List<Category> getAllCategories() throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT category_id, category_name FROM CATEGORIES ORDER BY category_name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                categories.add(new Category(rs.getInt("category_id"), rs.getString("category_name")));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy tất cả categories: " + e.getMessage());
            throw e;
        }
        return categories;
    }

    /**
     * Lấy danh sách các category dựa trên product_type.
     * Chỉ trả về các category có sản phẩm thuộc product_type đó.
     * @param productType Loại sản phẩm (ví dụ: "Laptop", "Accessory")
     * @return Danh sách các Category.
     * @throws SQLException Nếu có lỗi truy vấn CSDL.
     */
    public List<Category> getCategoriesByProductType(String productType) throws SQLException {
        List<Category> categories = new ArrayList<>();
        if (productType == null || productType.trim().isEmpty()) {
            return getAllCategories(); // Hoặc trả về rỗng tùy logic mong muốn
        }
        // Câu SQL này JOIN PRODUCTS và CATEGORIES, lọc theo product_type
        // và chỉ lấy các category riêng biệt có sản phẩm thuộc type đó.
        String sql = "SELECT DISTINCT c.category_id, c.category_name " +
                "FROM categories c " + // Giả sử tên bảng categories là 'categories' (viết thường)
                "JOIN PRODUCTS p ON c.category_id = p.category_id " +
                "WHERE p.product_type = ? ORDER BY c.category_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, productType);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(new Category(rs.getInt("category_id"), rs.getString("category_name")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy categories theo product type '" + productType + "': " + e.getMessage());
            throw e;
        }
        return categories;
    }
}
