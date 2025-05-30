package laptopstore.screen;

import laptopstore.data.DynamicQueryDataStore;
import laptopstore.model.PredefinedQuery;
import laptopstore.screen.tablemodel.DynamicTableModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RandomQueryTab extends JPanel {

    private JComboBox<PredefinedQuery> querySelectorCombo;
    private JButton runQueryButton;
    private JTable resultsTable;
    private DynamicTableModel resultsTableModel;
    private JScrollPane resultsScrollPane;

    private final DynamicQueryDataStore dynamicQueryDb;
    private final List<PredefinedQuery> predefinedQueries;

    public RandomQueryTab() {
        this.dynamicQueryDb = new DynamicQueryDataStore();
        this.predefinedQueries = new ArrayList<>();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        populatePredefinedQueries();
        initComponents();
        layoutComponents();
        addListeners();
    }

    private void populatePredefinedQueries() {
        // Query 1: Khách hàng mua nhiều mặt hàng nhất (sắp xếp theo tổng số lượng sản phẩm)
        String mostItemsQuerySQL = "SELECT " +
                "c.customer_id, c.first_name, c.last_name, c.email, " +
                "COUNT(DISTINCT o.order_id) AS total_orders, " +
                "SUM(o.total_amount) AS grand_total_spent, " + // Tổng tiền khách hàng này đã chi
                "SUM(od.quantity) AS total_items_quantity_for_ordering " + // Để sắp xếp, có thể không hiển thị nếu không muốn
                "FROM CUSTOMERS c " +
                "JOIN ORDERS o ON c.customer_id = o.customer_id " +
                "JOIN ORDER_DETAILS od ON o.order_id = od.order_id " +
                "GROUP BY c.customer_id, c.first_name, c.last_name, c.email " +
                "ORDER BY total_items_quantity_for_ordering DESC " +
                "LIMIT 10"; // Lấy top 10 khách hàng
        predefinedQueries.add(new PredefinedQuery("Khách hàng mua nhiều SP nhất (Top 10 theo SL)", mostItemsQuerySQL));

        // Query 2: Khách hàng mua ít mặt hàng nhất (sắp xếp theo tổng số lượng sản phẩm, có mua hàng)
        String leastItemsQuerySQL = "SELECT " +
                "c.customer_id, c.first_name, c.last_name, c.email, " +
                "COUNT(DISTINCT o.order_id) AS total_orders, " +
                "SUM(o.total_amount) AS grand_total_spent, " +
                "SUM(od.quantity) AS total_items_quantity_for_ordering " +
                "FROM CUSTOMERS c " +
                "JOIN ORDERS o ON c.customer_id = o.customer_id " +
                "JOIN ORDER_DETAILS od ON o.order_id = od.order_id " +
                "GROUP BY c.customer_id, c.first_name, c.last_name, c.email " +
                "ORDER BY total_items_quantity_for_ordering ASC " +
                "LIMIT 10"; // Lấy 10 khách hàng mua ít nhất (nhưng có mua)
        predefinedQueries.add(new PredefinedQuery("Khách hàng mua ít SP nhất (Top 10 theo SL, có mua)", leastItemsQuerySQL));

        // Thêm các câu truy vấn được định nghĩa sẵn khác của bro vào đây
        // Ví dụ: Sản phẩm bán chạy nhất
        String topSellingProductsSQL = "SELECT p.product_id, p.product_name, p.brand, SUM(od.quantity) AS total_quantity_sold " +
                "FROM PRODUCTS p JOIN ORDER_DETAILS od ON p.product_id = od.product_id " +
                "GROUP BY p.product_id, p.product_name, p.brand " +
                "ORDER BY total_quantity_sold DESC LIMIT 10";
        predefinedQueries.add(new PredefinedQuery("Sản phẩm bán chạy nhất (Top 10 theo SL)", topSellingProductsSQL));

        // Ví dụ: Doanh thu theo tháng
        String monthlyRevenueSQL = "SELECT TO_CHAR(order_date, 'YYYY-MM') AS month_year, SUM(total_amount) AS monthly_revenue " +
                "FROM ORDERS WHERE status NOT IN ('Cancelled', 'Returned', 'Failed') " + // Chỉ tính đơn thành công
                "GROUP BY month_year ORDER BY month_year DESC";
        predefinedQueries.add(new PredefinedQuery("Doanh thu theo tháng", monthlyRevenueSQL));
    }


    private void initComponents() {
        querySelectorCombo = new JComboBox<>(predefinedQueries.toArray(new PredefinedQuery[0]));
        querySelectorCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        if (!predefinedQueries.isEmpty()) {
            querySelectorCombo.setSelectedIndex(0);
        }

        runQueryButton = new JButton("Run Selected Query");
        runQueryButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        runQueryButton.setIcon(UIManager.getIcon("द्भ.奔跑")); // Find a suitable icon, e.g., execute, play

        resultsTableModel = new DynamicTableModel();
        resultsTable = new JTable(resultsTableModel);
        styleTable(resultsTable);
        resultsScrollPane = new JScrollPane(resultsTable);
    }

    private void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setShowGrid(true);
        table.setGridColor(Color.LIGHT_GRAY);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setPreferredSize(new Dimension(0, 35));
        header.setOpaque(false);
        header.setBackground(new Color(230, 230, 230));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
        table.setAutoCreateRowSorter(true);
    }

    private void layoutComponents() {
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 0, 5, 5);
        topPanel.add(new JLabel("Select Query:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(querySelectorCombo, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 10, 5, 0);
        topPanel.add(runQueryButton, gbc);

        add(topPanel, BorderLayout.NORTH);
        add(resultsScrollPane, BorderLayout.CENTER);
    }

    private void addListeners() {
        runQueryButton.addActionListener(this::executeSelectedQuery);
        // Hoặc nếu bro muốn query chạy ngay khi chọn ComboBox:
        // querySelectorCombo.addActionListener(this::executeSelectedQuery);
    }

    private void executeSelectedQuery(ActionEvent e) {
        PredefinedQuery selectedQuery = (PredefinedQuery) querySelectorCombo.getSelectedItem();
        if (selectedQuery == null) {
            showMessage("Please select a query to run.");
            return;
        }

        try {
            // Các truy vấn này không có tham số động từ người dùng
            List<Map<String, Object>> resultData = dynamicQueryDb.executeQuery(selectedQuery.sqlQuery(), null);
            resultsTableModel.setData(resultData);
            if (resultData.isEmpty()) {
                showMessage("Query executed, but no results found.");
            }
        } catch (SQLException ex) {
            showError("Database query error: " + ex.getMessage());
            ex.printStackTrace();
        } catch (Exception ex) {
            showError("An unexpected error occurred: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
}
