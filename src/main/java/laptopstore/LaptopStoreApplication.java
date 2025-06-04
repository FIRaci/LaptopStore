package laptopstore;

import java.awt.EventQueue;
import java.awt.Image;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.formdev.flatlaf.FlatLightLaf; // Sử dụng FlatLaf

import laptopstore.screen.QueryRunnerScreen; // Thay thế AdminDashboardScreen
import laptopstore.util.DatabaseConnection;

public class LaptopStoreApplication {

    public static void main(String[] args) {
        try {
            // Sử dụng FlatLaf cho giao diện hiện đại hơn
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Lỗi không thể khởi tạo FlatLaf Look and Feel: " + e.getMessage());
            // Có thể tiếp tục với Look and Feel mặc định nếu muốn
        }

        // Kiểm tra kết nối CSDL
        try (Connection testConn = DatabaseConnection.getConnection()) {
            if (testConn == null || testConn.isClosed() || !testConn.isValid(2)) {
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
            System.exit(1);
        } catch (Exception e) { // Bắt các lỗi khác có thể xảy ra khi khởi tạo
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Lỗi không xác định khi khởi tạo ứng dụng: " + e.getMessage() + "\n" +
                            "Ứng dụng sẽ thoát.",
                    "Lỗi Khởi Tạo", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Khởi chạy giao diện người dùng trên Event Dispatch Thread
        EventQueue.invokeLater(() -> {
            QueryRunnerScreen queryScreen = new QueryRunnerScreen(); // Sử dụng màn hình mới
            JFrame mainFrame = queryScreen.getFrame();

            if (mainFrame == null) {
                System.err.println("Lỗi nghiêm trọng: QueryRunnerScreen.getFrame() trả về null.");
                JOptionPane.showMessageDialog(null, "Không thể khởi tạo cửa sổ chính của ứng dụng.", "Lỗi Giao Diện", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Đặt icon cho ứng dụng (tùy chọn)
            try {
                URL iconURL = LaptopStoreApplication.class.getResource("/images/March7th.jpg"); // Thay bằng đường dẫn icon của bạn
                if (iconURL != null) {
                    Image appIcon = new ImageIcon(iconURL).getImage();
                    mainFrame.setIconImage(appIcon);
                } else {
                    System.err.println("Cảnh báo: Không tìm thấy file icon ứng dụng.");
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi tải icon ứng dụng: " + e.getMessage());
            }

            mainFrame.setTitle("Laptop Store - Query Runner"); // Đặt tiêu đề mới
            // mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Không cần full screen cho giao diện đơn giản này
            mainFrame.pack(); // Điều chỉnh kích thước frame cho vừa với components
            mainFrame.setLocationRelativeTo(null); // Căn giữa màn hình
            mainFrame.setVisible(true);
        });
    }
}
