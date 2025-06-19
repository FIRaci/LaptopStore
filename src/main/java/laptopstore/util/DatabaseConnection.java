package laptopstore.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/LaptopStore";
    private static final String USER = "postgres";
    private static final String PASSWORD = "admin";

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Lỗi nghiêm trọng: Không tìm thấy PostgreSQL JDBC Driver!");
            System.err.println("Hãy đảm bảo bạn đã thêm file postgresql.jar vào thư viện của project.");
            throw new RuntimeException("PostgreSQL JDBC Driver not found. Please add it to your project libraries.", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {
        try (Connection connection = getConnection()) {
            if (connection != null && !connection.isClosed()) {
                System.out.println("Kết nối CSDL qua DatabaseConnection.java thành công!");
            } else {
                System.out.println("Kết nối CSDL qua DatabaseConnection.java thất bại.");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối CSDL qua DatabaseConnection.java: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
