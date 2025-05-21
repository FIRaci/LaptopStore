package laptopstore.data;

import laptopstore.model.Category;
import laptopstore.util.DatabaseConnection; // Đảm bảo import đúng lớp kết nối của bạn

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
        // CSDL đã có ON DELETE SET NULL trên PRODUCTS.category_id,
        // nhưng vẫn nên kiểm tra để thông báo thân thiện hơn nếu muốn.
        // Hoặc nếu bạn muốn logic chặt chẽ hơn là không cho xóa nếu có sản phẩm:
        /*
        String checkProductSql = "SELECT COUNT(*) FROM PRODUCTS WHERE category_id = ?";
        try (Connection connCheck = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = connCheck.prepareStatement(checkProductSql)) {
            checkStmt.setInt(1, categoryId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Không thể xóa category ID " + categoryId + " vì nó đang được sử dụng bởi các sản phẩm.", "CATEGORY_IN_USE");
                }
            }
        }
        */
        String sql = "DELETE FROM CATEGORIES WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, categoryId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            // "23503" là lỗi FK violation chung, có thể không phải do PRODUCTS nếu có bảng khác tham chiếu
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
}
