package laptopstore;

import com.formdev.flatlaf.FlatLightLaf;
// KHÔNG CẦN IMPORT CÁC MODEL Ở ĐÂY NỮA VÌ KHÔNG DÙNG LIST TĨNH
// import laptopstore.model.*;
import laptopstore.screen.AdminDashboardScreen;
import laptopstore.util.DatabaseConnection; // SỬ DỤNG CLASS KẾT NỐI CỦA BẠN

import javax.swing.*;
import java.awt.EventQueue;
import java.awt.Image;
import java.net.URL;
import java.sql.Connection; // Cho việc kiểm tra kết nối
import java.sql.SQLException; // Cho việc kiểm tra kết nối

public class LaptopStoreApplication {

    // XÓA TẤT CẢ CÁC DANH SÁCH TĨNH (static List) Ở ĐÂY
    // public static List<Product> products = new ArrayList<>();
    // public static List<Laptop> laptops = new ArrayList<>();
    // public static List<Gear> gears = new ArrayList<>();
    // public static List<Components> components = new ArrayList<>();
    // public static List<Employee> employees = new ArrayList<>();
    // public static List<Customer> customers = new ArrayList<>();
    // public static List<Order> orders = new ArrayList<>();
    // public static List<OrderItem> orderItems = new ArrayList<>();
    // public static List<Payment> payments = new ArrayList<>();

    // XÓA HOÀN TOÀN PHƯƠNG THỨC initializeMockData()
    /*
    private static void initializeMockData() {
        // ... nội dung cũ đã bị xóa ...
    }
    */

    public static void main(String[] args) {
        // 1. Thiết lập Look and Feel (nên làm đầu tiên)
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Lỗi không thể khởi tạo FlatLaf Look and Feel: " + e.getMessage());
            // Có thể dùng Look and Feel mặc định của hệ thống nếu FlatLaf lỗi
        }

        // 2. XÓA LỜI GỌI initializeMockData();
        // initializeMockData();

        // 3. (Khuyến nghị) Kiểm tra kết nối CSDL trước khi chạy UI
        try (Connection testConn = DatabaseConnection.getConnection()) { // Sử dụng class của bạn
            if (testConn == null || testConn.isClosed() || !testConn.isValid(2)) { // Kiểm tra trong 2 giây
                JOptionPane.showMessageDialog(null,
                        "Không thể kết nối đến cơ sở dữ liệu.\n" +
                                "Vui lòng kiểm tra cấu hình trong DatabaseConnection.java và trạng thái CSDL.\n" +
                                "Ứng dụng sẽ thoát.",
                        "Lỗi Kết Nối Cơ Sở Dữ Liệu", JOptionPane.ERROR_MESSAGE);
                System.exit(1); // Thoát ứng dụng nếu không kết nối được
            }
            System.out.println("Kiểm tra kết nối CSDL ban đầu thành công!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Lỗi nghiêm trọng khi kết nối CSDL: " + e.getMessage() + "\n" +
                            "Chi tiết: " + e.getClass().getName() + "\n" +
                            "Ứng dụng sẽ thoát.",
                    "Lỗi Kết Nối Cơ Sở Dữ Liệu", JOptionPane.ERROR_MESSAGE);
            System.exit(1); // Thoát ứng dụng nếu có lỗi
        } catch (Exception e) { // Bắt các lỗi khác có thể xảy ra khi kiểm tra kết nối
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Lỗi không xác định khi khởi tạo ứng dụng: " + e.getMessage() + "\n" +
                            "Ứng dụng sẽ thoát.",
                    "Lỗi Khởi Tạo", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }


        // 4. Khởi chạy giao diện người dùng trên Event Dispatch Thread
        EventQueue.invokeLater(() -> {
            AdminDashboardScreen adminDashboard = new AdminDashboardScreen();
            JFrame mainFrame = adminDashboard.getFrame();

            if (mainFrame == null) {
                System.err.println("Lỗi nghiêm trọng: AdminDashboardScreen.getFrame() trả về null. Không thể hiển thị ứng dụng.");
                JOptionPane.showMessageDialog(null, "Không thể khởi tạo cửa sổ chính của ứng dụng.", "Lỗi Giao Diện", JOptionPane.ERROR_MESSAGE);
                return; // Không tiếp tục nếu không có frame chính
            }

            // Thiết lập icon cho ứng dụng
            try {
                URL iconURL = LaptopStoreApplication.class.getResource("/images/March7th.jpg"); // Đường dẫn đến icon của bạn
                if (iconURL != null) {
                    Image appIcon = new ImageIcon(iconURL).getImage();
                    mainFrame.setIconImage(appIcon);
                } else {
                    System.err.println("Cảnh báo: Không tìm thấy file icon ứng dụng tại /images/March7th.jpg");
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi tải icon ứng dụng: " + e.getMessage());
                // Không cần thiết phải dừng ứng dụng nếu chỉ là lỗi tải icon
            }

            mainFrame.setTitle("Laptop Store Management"); // Đặt tiêu đề cho cửa sổ
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Đảm bảo ứng dụng thoát khi đóng cửa sổ
            mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Mở full màn hình
            // adminDashboard.showScreen(); // Nếu AdminDashboardScreen tự quản lý việc hiển thị frame
            mainFrame.setVisible(true); // Cách chuẩn để hiển thị JFrame
        });
    }
}
