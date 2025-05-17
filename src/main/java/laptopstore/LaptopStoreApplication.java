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
        // ... (Nội dung initializeMockData của bạn giữ nguyên) ...
        // Mock data cho Product
        if (products.isEmpty()) {
            Product product1 = new Product(1, "XPS 13", "Dell", "High-end laptop", BigDecimal.valueOf(1200.0), 10, LocalDateTime.of(2023, 1, 1, 0, 0));
            Product product2 = new Product(2, "MX Master", "Logitech", "Wireless mouse", BigDecimal.valueOf(50.0), 20, LocalDateTime.of(2022, 6, 1, 0, 0));
            Product product3 = new Product(3, "i7-12700K", "Intel", "Powerful CPU", BigDecimal.valueOf(400.0), 8, LocalDateTime.of(2021, 11, 1, 0, 0));
            products.add(product1);
            products.add(product2);
            products.add(product3);

            laptops.add(new Laptop(1, 1, "Dell XPS 13"));
            gears.add(new Gear(1, 2, "Logitech MX Master"));
            components.add(new Components(1, 3, "Intel i7-12700K"));
        }

        // Mock data cho Employee
        if (employees.isEmpty()) {
            employees.add(new Employee(1, "Alice", "Smith", "1234567890", "123 Main St", 'F', "123456789", "Manager", 50000.0, "Mon-Fri", LocalDate.of(2023, 1, 1)));
            employees.add(new Employee(2, "Bob", "Johnson", "0987654321", "456 Oak St", 'M', "987654321", "Sales", 40000.0, "Mon-Fri", LocalDate.of(2023, 6, 1)));
        }

        // Mock data cho Customer
        if (customers.isEmpty()) {
            customers.add(new Customer(1, "johndoe", "john@example.com", "John", "Doe", LocalDateTime.of(2023, 1, 1, 0, 0), 'M', "123 Main St", LocalDate.of(1990, 5, 15), "555-1234"));
            customers.add(new Customer(2, "janedoe", "jane@example.com", "Jane", "Doe", LocalDateTime.of(2023, 2, 1, 0, 0), 'F', "456 Oak St", LocalDate.of(1992, 8, 20), "555-5678"));
        }

        // Mock data cho Order và OrderItem
        if (orders.isEmpty()) {
            Order order1 = new Order(1, 1, 1, LocalDate.of(2023, 3, 1), "Pending", 0, 0, 0);
            OrderItem item1 = new OrderItem(1, 1, 1, 1, 1200.0); // XPS 13
            OrderItem item2 = new OrderItem(2, 1, 2, 1, 50.0);   // MX Master
            order1.addOrderItem(item1);
            order1.addOrderItem(item2);
            orderItems.add(item1); // Giả sử bạn vẫn muốn quản lý danh sách tổng thể
            orderItems.add(item2);
            orders.add(order1);

            Order order2 = new Order(2, 2, 2, LocalDate.of(2023, 4, 1), "Pending", 0, 0, 0);
            OrderItem item3 = new OrderItem(3, 2, 3, 1, 400.0);  // i7-12700K
            order2.addOrderItem(item3);
            orderItems.add(item3);
            orders.add(order2);
        }

        // Mock data cho Payment
        if (payments.isEmpty()) {
            payments.add(new Payment(1, 1, LocalDateTime.of(2023, 3, 1, 10, 30), 1300.0, "Credit Card", "Paid"));
            payments.add(new Payment(2, 2, LocalDateTime.of(2023, 4, 1, 12, 30), 400.0, "Cash", "Paid"));
        }
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