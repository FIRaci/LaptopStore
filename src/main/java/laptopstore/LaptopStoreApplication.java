package laptopstore;

import com.formdev.flatlaf.FlatLightLaf;
import laptopstore.model.*; // Giả sử các model của bạn ở đây
import laptopstore.screen.AdminDashboardScreen;

import javax.swing.*;
import java.awt.EventQueue; // Sử dụng EventQueue thay vì SwingUtilities cho chuẩn
import java.awt.Image; // Import lớp Image của AWT
import java.net.URL; // Import URL để lấy resource
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LaptopStoreApplication {
    public static List<Product> products = new ArrayList<>();
    public static List<Laptop> laptops = new ArrayList<>();
    public static List<Gear> gears = new ArrayList<>();
    public static List<Components> components = new ArrayList<>();
    public static List<Employee> employees = new ArrayList<>();
    public static List<Customer> customers = new ArrayList<>();
    public static List<Order> orders = new ArrayList<>();
    public static List<OrderItem> orderItems = new ArrayList<>();
    public static List<Payment> payments = new ArrayList<>();

    private static void initializeMockData() {
        laptopstore.util.DataLoader.loadDataFromDatabase();
    }


    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Failed to initialize FlatLaf: " + e.getMessage());
        }

        initializeMockData();

        EventQueue.invokeLater(() -> {
            AdminDashboardScreen adminDashboard = new AdminDashboardScreen();
            JFrame mainFrame = null;

            // --- THÊM ICON CHO ỨNG DỤNG SWING ---
            try {
                URL iconURL = LaptopStoreApplication.class.getResource("/images/March7th.jpg"); // Thay đổi đường dẫn nếu cần
                if (iconURL != null) {
                    Image appIcon = new ImageIcon(iconURL).getImage();
                    mainFrame = adminDashboard.getFrame(); // Bạn cần tạo phương thức này
                    if (mainFrame != null) {
                        mainFrame.setIconImage(appIcon);
                    }
                } else {
                    System.err.println("Không tìm thấy file icon: /images/March7th.jpg");
                }
            } catch (Exception e) {
                System.err.println("Không thể tải icon ứng dụng: " + e.getMessage());
                e.printStackTrace();
            }

            if (mainFrame != null) {
                mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
            adminDashboard.showScreen();
        });
    }
}