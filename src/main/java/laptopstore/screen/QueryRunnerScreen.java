package laptopstore.screen;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.BorderFactory;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import laptopstore.data.CustomerDataStore;
import laptopstore.data.ProductDataStore;
import laptopstore.data.OrderDataStore;
import laptopstore.data.EmployeeDataStore;
import laptopstore.data.PaymentDataStore;
import laptopstore.data.CategoryDataStore;

import laptopstore.model.Category;
import laptopstore.model.Customer;
import laptopstore.model.Employee;
import laptopstore.model.Order;
import laptopstore.model.Payment;
import laptopstore.model.Product;
import laptopstore.screen.tablemodel.DynamicTableModel;

public class QueryRunnerScreen {

    private JFrame frame;
    private JComboBox<String> queryComboBox;
    private JButton executeButton;
    private JTable resultTable;
    private DynamicTableModel dynamicTableModel;

    private final ProductDataStore productDb;
    private final CustomerDataStore customerDb;
    private final OrderDataStore orderDb;
    private final EmployeeDataStore employeeDb;
    private final PaymentDataStore paymentDb;
    private final CategoryDataStore categoryDb;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private final DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("yyyy-MM");


    private final String[] queryDescriptions = {
            "-- Select a Query --",
            // Basic Listing
            "1. List All Products (Full Details)",
            "2. List All Customers (Full Details)",
            "3. List All Orders (Basic Info)",
            "4. List All Categories",
            "5. List All Employees (Basic Info)",
            "6. List All Payments (Basic Info)",
            // Product Queries (from AdminDashboard)
            "P1. Latest 5 Products",
            "P2. Latest 10 Products",
            "P3. Products with Sales in Last 30 Days",
            "P4. Top 5 Best Selling Products (by quantity)",
            "P5. Never Sold Products",
            "P6. Products Inventory and Revenue",
            "P7. Products in Pending/Processing Orders",
            "P8. Products Low Stock (Stock < 5)",
            // Customer Queries (from AdminDashboard)
            "C1. Latest 5 Customers",
            "C2. Latest 10 Customers",
            "C3. Top 5 Customers by Order Count",
            "C4. Customers with No Orders",
            "C5. Top Gaming Laptop Buyers (Cat ID 1)",
            "C6. High Spending Customers (Order > 5M)",
            "C7. Customers with Pending/Processing Orders",
            // Employee Queries (from AdminDashboard)
            "E1. Latest 5 Employees",
            "E2. Latest 10 Employees",
            // Payment Queries (from AdminDashboard)
            "PY1. Latest 5 Payments",
            "PY2. Latest 10 Payments",
            // Advanced Queries (Set 1 - A_x_)
            "A1. Revenue by Category (Last Month)",
            "A2. Top 10 Loyal Customers (Min 3 Orders, by Total Spent)",
            "A3. Employee Sales Performance (Last Month)",
            "A5. Monthly Sales Trend (Last 12 Months)",
            "A6. Top 10 Products Bought Together",
            "A7. Order Status Distribution",
            "A8. Inactive Customers (No orders in last 6 months)",
            "A9. Revenue: Orders With vs Without Payment Record",
            "A10. Top 5 Products by Return Rate ('Returned' status)",
            // New Queries from User (Set 2 - NQ1-NQ10 - đã được tích hợp ở lượt trước)
            "NQ1. Top 20 Products Sold (Revenue, 01/03/24-01/07/24)",
            "NQ2a. Category Revenue (01/07/24-31/12/24)",
            "NQ2b. Top 3 Products/Category (Revenue, 01/07/24-31/12/24)",
            "NQ2c. Top 3 Employees/Category (Sales, 01/07/24-31/12/24)",
            "NQ3. Customers (18-30yo, >=5 orders, 01/02/24-31/08/24)",
            "NQ4. Top Spender per Top 15 Categories",
            "NQ5. Highest Value Order per Month (2024)",
            "NQ6. Unpaid Orders (Male Customers, name with 'z')",
            "NQ7. Employees NOT selling 'Apple iMac 24 M3' (04/2024)",
            "NQ8. Top 10 Employee Performance (Payment/Day, by 31/12/2024)",
            "NQ9. Top 10 Payments by Most Orders Linked",
            "NQ10. Payments for Single Order by Highest Paid Employee",
            // New Simpler Queries (Set 3 - NQ11-NQ20 - Sửa đổi theo yêu cầu mới)
            "NQ11. Số lượng sản phẩm theo từng Danh mục",
            "NQ12. SL Khách hàng mới (3 năm gần nhất, theo tháng)",
            "NQ13. Đơn hàng > 4,000",
            "NQ14. Nhân viên được thuê (2 năm gần nhất)",
            "NQ15. Tổng tiền & SL Thanh toán theo Phương thức",
            "NQ16. Sản phẩm mới ra mắt (3 năm gần nhất)",
            "NQ17. SL Đơn hàng trong 2 năm theo Trạng thái", // Đã sửa mô tả
            "NQ18. Khách hàng ở 'Green Valley'",
            "NQ19. Sản phẩm giá từ 500 - 1,000",
            "NQ20. Tổng doanh thu (4 năm qua, theo năm)"
    };

    public QueryRunnerScreen() {
        productDb = new ProductDataStore();
        customerDb = new CustomerDataStore();
        orderDb = new OrderDataStore();
        employeeDb = new EmployeeDataStore();
        paymentDb = new PaymentDataStore();
        categoryDb = new CategoryDataStore();
        initComponents();
    }

    private void initComponents() {
        frame = new JFrame("Laptop Store - Advanced Query Runner");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        JLabel selectQueryLabel = new JLabel("Select Query: ");
        selectQueryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        queryComboBox = new JComboBox<>(queryDescriptions);
        queryComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        queryComboBox.setPreferredSize(new Dimension(600, queryComboBox.getPreferredSize().height));

        executeButton = new JButton("Execute Query");
        executeButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        executeButton.addActionListener(new ExecuteQueryListener());

        topPanel.add(selectQueryLabel, BorderLayout.WEST);
        topPanel.add(queryComboBox, BorderLayout.CENTER);
        topPanel.add(executeButton, BorderLayout.EAST);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        dynamicTableModel = new DynamicTableModel();
        resultTable = new JTable(dynamicTableModel);
        styleResultTable();

        JScrollPane scrollPane = new JScrollPane(resultTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        frame.add(mainPanel);
    }

    private void styleResultTable() {
        resultTable.setRowHeight(25);
        resultTable.setIntercellSpacing(new Dimension(1,1));
        resultTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JTableHeader header = resultTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(0, 30));
        header.setReorderingAllowed(true);

        ((DefaultTableCellRenderer)header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
    }

    private class ExecuteQueryListener implements ActionListener {
        @Override
        @SuppressWarnings("unchecked")
        public void actionPerformed(ActionEvent e) {
            String selectedQueryDesc = (String) queryComboBox.getSelectedItem();
            if (selectedQueryDesc == null || selectedQueryDesc.equals(queryDescriptions[0])) {
                dynamicTableModel.clearData();
                JOptionPane.showMessageDialog(frame, "Please select a query to execute.", "No Query Selected", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            List<Object[]> dataRows = new ArrayList<>();
            String[] columnNames = new String[0];
            String errorMessage = null;
            List<?> rawResults = null;

            try {
                // --- Basic Listing & Previous AdminDashboard Queries --- (Giữ nguyên như trước)
                if (selectedQueryDesc.startsWith("1. List All Products")) {
                    rawResults = productDb.getAllProducts();
                    columnNames = new String[]{"ID", "Name", "Model", "Brand", "Category", "Price", "Stock", "Published"};
                    for(Object obj : rawResults) { Product p = (Product) obj; dataRows.add(new Object[]{ p.getProductId(), p.getSpecificProductName(), p.getModel(), p.getBrand(), p.getCategoryName(), p.getPrice(), p.getStockQuantity(), p.getYearPublish() != null ? p.getYearPublish().format(dateFormatter) : "N/A"});}
                } else if (selectedQueryDesc.startsWith("2. List All Customers")) {
                    rawResults = customerDb.getAllCustomers();
                    columnNames = new String[]{"ID", "Username", "First Name", "Last Name", "Email", "Gender", "Address", "DoB", "Phone", "Created At"};
                    for(Object obj : rawResults) { Customer c = (Customer) obj; dataRows.add(new Object[]{c.getCustomerId(), c.getUsername(), c.getFirstName(), c.getLastName(), c.getEmail(), String.valueOf(c.getGender()), c.getAddress(), c.getDateOfBirth() != null ? c.getDateOfBirth().format(dateFormatter) : "N/A", c.getPhone(), c.getCreatedAt() != null ? c.getCreatedAt().format(dateTimeFormatter) : "N/A"});}
                } else if (selectedQueryDesc.startsWith("3. List All Orders")) {
                    rawResults = orderDb.getAllOrders();
                    columnNames = new String[]{"Order ID", "Customer", "Order Date", "Status", "Total Amount"};
                    for(Object obj : rawResults) { Order o = (Order) obj; dataRows.add(new Object[]{o.getOrderId(), o.getCustomerName(), o.getOrderDate() != null ? o.getOrderDate().format(dateFormatter) : "N/A", o.getStatus(), o.getTotalAmount()});}
                } else if (selectedQueryDesc.startsWith("4. List All Categories")) {
                    rawResults = categoryDb.getAllCategories();
                    columnNames = new String[]{"ID", "Name"};
                    for(Object obj : rawResults) { Category cat = (Category) obj; dataRows.add(new Object[]{cat.getCategoryId(), cat.getCategoryName()});}
                } else if (selectedQueryDesc.startsWith("5. List All Employees")) {
                    rawResults = employeeDb.getAllEmployees();
                    columnNames = new String[]{"ID", "Full Name", "Role", "Email", "Hire Date"};
                    for(Object obj : rawResults) { Employee emp = (Employee) obj; dataRows.add(new Object[]{emp.getEmployeeId(), emp.getFirstName() + " " + emp.getLastName(), emp.getRole(), emp.getEmail(), emp.getHireDay() != null ? emp.getHireDay().format(dateFormatter) : "N/A"});}
                } else if (selectedQueryDesc.startsWith("6. List All Payments")) {
                    rawResults = paymentDb.getAllPayments();
                    columnNames = new String[]{"ID", "Employee", "Method", "Amount", "Date"};
                    for(Object obj : rawResults) { Payment p = (Payment) obj; dataRows.add(new Object[]{p.getPaymentId(), p.getEmployeeName(), p.getPaymentMethod(), p.getTotalAmount(), p.getPaymentDate() != null ? p.getPaymentDate().format(dateTimeFormatter) : "N/A"});}
                }
                // Product Queries P1-P8
                else if (selectedQueryDesc.equals("P1. Latest 5 Products")) { rawResults = productDb.getLatestProducts(5); columnNames = new String[]{"ID", "Name", "Model", "Brand", "Price", "Stock"}; for(Object obj : rawResults) { Product p = (Product) obj; dataRows.add(new Object[]{p.getProductId(), p.getSpecificProductName(), p.getModel(), p.getBrand(), p.getPrice(), p.getStockQuantity()});}}
                else if (selectedQueryDesc.equals("P2. Latest 10 Products")) { rawResults = productDb.getLatestProducts(10); columnNames = new String[]{"ID", "Name", "Model", "Brand", "Price", "Stock"}; for(Object obj : rawResults) { Product p = (Product) obj; dataRows.add(new Object[]{p.getProductId(), p.getSpecificProductName(), p.getModel(), p.getBrand(), p.getPrice(), p.getStockQuantity()});}}
                else if (selectedQueryDesc.equals("P3. Products with Sales in Last 30 Days")) { rawResults = productDb.getProductsWithRecentOrders(); columnNames = new String[]{"ID", "Name", "Model", "Category"}; for(Object obj : rawResults) { Product p = (Product) obj; dataRows.add(new Object[]{p.getProductId(), p.getSpecificProductName(), p.getModel(), p.getCategoryName()});}}
                else if (selectedQueryDesc.equals("P4. Top 5 Best Selling Products (by quantity)")) { rawResults = productDb.getTopSellingProducts(); columnNames = new String[]{"ID", "Name", "Category", "Price"}; for(Object obj : rawResults) { Product p = (Product) obj; dataRows.add(new Object[]{p.getProductId(), p.getSpecificProductName(), p.getCategoryName(), p.getPrice()});}}
                else if (selectedQueryDesc.equals("P5. Never Sold Products")) { rawResults = productDb.getNeverSoldProducts(); columnNames = new String[]{"ID", "Name", "Model", "Brand", "Category", "Price", "Stock"}; for(Object obj : rawResults) { Product p = (Product) obj; dataRows.add(new Object[]{p.getProductId(), p.getSpecificProductName(), p.getModel(), p.getBrand(), p.getCategoryName(), p.getPrice(), p.getStockQuantity()});}}
                else if (selectedQueryDesc.equals("P6. Products Inventory and Revenue")) { rawResults = productDb.getProductsWithInventoryAndRevenue(); columnNames = new String[]{"ID", "Name", "Category", "Price", "Stock"}; for(Object obj : rawResults) { Product p = (Product) obj; dataRows.add(new Object[]{p.getProductId(), p.getSpecificProductName(), p.getCategoryName(), p.getPrice(), p.getStockQuantity()});}}
                else if (selectedQueryDesc.equals("P7. Products in Pending/Processing Orders")) { rawResults = productDb.getProductsWithPendingOrders(); columnNames = new String[]{"ID", "Name", "Model", "Brand", "Category"}; for(Object obj : rawResults) { Product p = (Product) obj; dataRows.add(new Object[]{p.getProductId(), p.getSpecificProductName(), p.getModel(), p.getBrand(), p.getCategoryName()});}}
                else if (selectedQueryDesc.equals("P8. Products Low Stock (Stock < 5)")) { rawResults = productDb.getLowStockProducts(5); columnNames = new String[]{"ID", "Name", "Model", "Category", "Stock", "Price"}; for(Object obj : rawResults) { Product p = (Product) obj; dataRows.add(new Object[]{p.getProductId(), p.getSpecificProductName(), p.getModel(), p.getCategoryName(), p.getStockQuantity(), p.getPrice()});}}
                // Customer Queries C1-C7
                else if (selectedQueryDesc.equals("C1. Latest 5 Customers")) { rawResults = customerDb.getLatestCustomers(5); columnNames = new String[]{"ID", "Username", "Full Name", "Email", "Created At"}; for(Object obj : rawResults) { Customer c = (Customer) obj; dataRows.add(new Object[]{c.getCustomerId(), c.getUsername(), c.getFirstName() + " " + c.getLastName(), c.getEmail(), c.getCreatedAt() !=null ? c.getCreatedAt().format(dateTimeFormatter) : "N/A"});}}
                else if (selectedQueryDesc.equals("C2. Latest 10 Customers")) { rawResults = customerDb.getLatestCustomers(10); columnNames = new String[]{"ID", "Username", "Full Name", "Email", "Created At"}; for(Object obj : rawResults) { Customer c = (Customer) obj; dataRows.add(new Object[]{c.getCustomerId(), c.getUsername(), c.getFirstName() + " " + c.getLastName(), c.getEmail(), c.getCreatedAt() !=null ? c.getCreatedAt().format(dateTimeFormatter) : "N/A"});}}
                else if (selectedQueryDesc.equals("C3. Top 5 Customers by Order Count")) { rawResults = customerDb.getTopCustomersByOrders(); columnNames = new String[]{"ID", "Username", "Full Name", "Email"}; for(Object obj : rawResults) { Customer c = (Customer) obj; dataRows.add(new Object[]{c.getCustomerId(), c.getUsername(), c.getFirstName() + " " + c.getLastName(), c.getEmail()});}}
                else if (selectedQueryDesc.equals("C4. Customers with No Orders")) { rawResults = customerDb.getCustomersWithNoOrders(); columnNames = new String[]{"ID", "Username", "Full Name", "Email"}; for(Object obj : rawResults) { Customer c = (Customer) obj; dataRows.add(new Object[]{c.getCustomerId(), c.getUsername(), c.getFirstName() + " " + c.getLastName(), c.getEmail()});}}
                else if (selectedQueryDesc.equals("C5. Top Gaming Laptop Buyers (Cat ID 1)")) { rawResults = customerDb.getTopGamingLaptopCustomers(); columnNames = new String[]{"ID", "Username", "Full Name", "Email"}; for(Object obj : rawResults) { Customer c = (Customer) obj; dataRows.add(new Object[]{c.getCustomerId(), c.getUsername(), c.getFirstName() + " " + c.getLastName(), c.getEmail()});}}
                else if (selectedQueryDesc.equals("C6. High Spending Customers (Order > 5M)")) { rawResults = customerDb.getHighSpendingCustomers(); columnNames = new String[]{"ID", "Username", "Full Name", "Email"}; for(Object obj : rawResults) { Customer c = (Customer) obj; dataRows.add(new Object[]{c.getCustomerId(), c.getUsername(), c.getFirstName() + " " + c.getLastName(), c.getEmail()});}}
                else if (selectedQueryDesc.equals("C7. Customers with Pending/Processing Orders")) { rawResults = customerDb.getCustomersWithPendingOrders(); columnNames = new String[]{"ID", "Username", "Full Name", "Email"}; for(Object obj : rawResults) { Customer c = (Customer) obj; dataRows.add(new Object[]{c.getCustomerId(), c.getUsername(), c.getFirstName() + " " + c.getLastName(), c.getEmail()});}}
                // Employee Queries E1-E2
                else if (selectedQueryDesc.equals("E1. Latest 5 Employees")) { rawResults = employeeDb.getLatestEmployees(5); columnNames = new String[]{"ID", "Full Name", "Role", "Email", "Hire Date"}; for(Object obj : rawResults) { Employee emp = (Employee) obj; dataRows.add(new Object[]{emp.getEmployeeId(), emp.getFirstName() + " " + emp.getLastName(), emp.getRole(), emp.getEmail(), emp.getHireDay()!=null ? emp.getHireDay().format(dateFormatter):"N/A"});}}
                else if (selectedQueryDesc.equals("E2. Latest 10 Employees")) { rawResults = employeeDb.getLatestEmployees(10); columnNames = new String[]{"ID", "Full Name", "Role", "Email", "Hire Date"}; for(Object obj : rawResults) { Employee emp = (Employee) obj; dataRows.add(new Object[]{emp.getEmployeeId(), emp.getFirstName() + " " + emp.getLastName(), emp.getRole(), emp.getEmail(), emp.getHireDay()!=null ? emp.getHireDay().format(dateFormatter):"N/A"});}}
                // Payment Queries PY1-PY2
                else if (selectedQueryDesc.equals("PY1. Latest 5 Payments")) { rawResults = paymentDb.getLatestPayments(5); columnNames = new String[]{"ID", "Employee", "Method", "Amount", "Date"}; for(Object obj : rawResults) { Payment p = (Payment) obj; dataRows.add(new Object[]{p.getPaymentId(), p.getEmployeeName(), p.getPaymentMethod(), p.getTotalAmount(), p.getPaymentDate()!=null ? p.getPaymentDate().format(dateTimeFormatter):"N/A"});}}
                else if (selectedQueryDesc.equals("PY2. Latest 10 Payments")) { rawResults = paymentDb.getLatestPayments(10); columnNames = new String[]{"ID", "Employee", "Method", "Amount", "Date"}; for(Object obj : rawResults) { Payment p = (Payment) obj; dataRows.add(new Object[]{p.getPaymentId(), p.getEmployeeName(), p.getPaymentMethod(), p.getTotalAmount(), p.getPaymentDate()!=null ? p.getPaymentDate().format(dateTimeFormatter):"N/A"});}}
                // Advanced Queries (Set 1 - A_x_)
                else if (selectedQueryDesc.equals("A1. Revenue by Category (Last Month)")) { LocalDate today = LocalDate.now(); LocalDate startDate = today.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth()); LocalDate endDate = today.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()); rawResults = orderDb.getRevenueByCategory(startDate, endDate); columnNames = new String[]{"Category Name", "Total Revenue", "Total Orders"}; for(Object mapObj : rawResults) { Map<String, Object> rowMap = (Map<String, Object>) mapObj; dataRows.add(new Object[]{rowMap.get("category_name"), rowMap.get("total_revenue_category"), rowMap.get("total_orders_category")}); } }
                else if (selectedQueryDesc.equals("A2. Top 10 Loyal Customers (Min 3 Orders, by Total Spent)")) { rawResults = customerDb.getMostLoyalCustomers(3, 10); columnNames = new String[]{"Cust. ID", "First Name", "Last Name", "Email", "Total Orders", "Total Spent", "Avg. Order Value"}; for(Object mapObj : rawResults) { Map<String, Object> rowMap = (Map<String, Object>) mapObj; dataRows.add(new Object[]{rowMap.get("customer_id"), rowMap.get("first_name"), rowMap.get("last_name"), rowMap.get("email"), rowMap.get("total_orders"), rowMap.get("total_spent"), ((BigDecimal)rowMap.get("average_order_value")).setScale(2, RoundingMode.HALF_UP) }); } }
                else if (selectedQueryDesc.equals("A3. Employee Sales Performance (Last Month)")) { LocalDate today = LocalDate.now(); LocalDate startDate = today.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth()); LocalDate endDate = today.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()); rawResults = employeeDb.getEmployeeSalesPerformance(startDate, endDate); columnNames = new String[]{"Emp. ID", "First Name", "Last Name", "Role", "Orders Handled", "Revenue Processed"}; for(Object mapObj : rawResults) { Map<String, Object> rowMap = (Map<String, Object>) mapObj; dataRows.add(new Object[]{rowMap.get("employee_id"), rowMap.get("employee_first_name"), rowMap.get("employee_last_name"), rowMap.get("role"), rowMap.get("orders_handled_count"), rowMap.get("total_revenue_processed")}); } }
                else if (selectedQueryDesc.equals("A5. Monthly Sales Trend (Last 12 Months)")) { LocalDate endDate = LocalDate.now(); LocalDate startDate = endDate.minusYears(1).with(TemporalAdjusters.firstDayOfMonth()); rawResults = orderDb.getMonthlySalesTrend(startDate, endDate); columnNames = new String[]{"Sales Month (YYYY-MM)", "Monthly Orders", "Monthly Revenue"}; for(Object mapObj : rawResults) { Map<String, Object> rowMap = (Map<String, Object>) mapObj; dataRows.add(new Object[]{rowMap.get("sales_month"), rowMap.get("monthly_orders"), rowMap.get("monthly_revenue")}); } }
                else if (selectedQueryDesc.equals("A6. Top 10 Products Bought Together")) { rawResults = orderDb.getFrequentlyBoughtTogether(10); columnNames = new String[]{"Product 1 Name", "Product 2 Name", "Times Bought Together"}; for(Object mapObj : rawResults) { Map<String, Object> rowMap = (Map<String, Object>) mapObj; dataRows.add(new Object[]{rowMap.get("product1_name"), rowMap.get("product2_name"), rowMap.get("times_bought_together")}); } }
                else if (selectedQueryDesc.equals("A7. Order Status Distribution")) { rawResults = orderDb.getOrderStatusDistribution(); columnNames = new String[]{"Status", "Count", "Percentage (%)"}; for(Object mapObj : rawResults) { Map<String, Object> rowMap = (Map<String, Object>) mapObj; dataRows.add(new Object[]{rowMap.get("status"), rowMap.get("count_status"), rowMap.get("percentage_status")}); } }
                else if (selectedQueryDesc.equals("A8. Inactive Customers (No orders in last 6 months)")) { rawResults = customerDb.getInactiveCustomers(6); columnNames = new String[]{"Cust. ID", "First Name", "Last Name", "Email", "Last Order Date"}; for(Object mapObj : rawResults) { Map<String, Object> rowMap = (Map<String, Object>) mapObj; Object lastOrderDateObj = rowMap.get("last_order_date"); String lastOrderDateStr = "N/A"; if (lastOrderDateObj instanceof java.sql.Date) { lastOrderDateStr = ((java.sql.Date) lastOrderDateObj).toLocalDate().format(dateFormatter); } dataRows.add(new Object[]{rowMap.get("customer_id"), rowMap.get("first_name"), rowMap.get("last_name"), rowMap.get("email"), lastOrderDateStr}); } }
                else if (selectedQueryDesc.equals("A9. Revenue: Orders With vs Without Payment Record")) { rawResults = orderDb.getRevenueByPaymentLinkStatus(); columnNames = new String[]{"Payment Link Status", "# Orders", "Avg. Order Value", "Total Revenue"}; for(Object mapObj : rawResults) { Map<String, Object> rowMap = (Map<String, Object>) mapObj; dataRows.add(new Object[]{rowMap.get("payment_link_status"), rowMap.get("number_of_orders"), rowMap.get("average_order_value"), rowMap.get("total_revenue")}); } }
                else if (selectedQueryDesc.equals("A10. Top 5 Products by Return Rate ('Returned' status)")) { rawResults = productDb.getTopProductsByReturnRate(5, "Returned"); columnNames = new String[]{"Prod. ID", "Product Name", "Sold Orders", "Returned Orders", "Return Rate (%)"}; for(Object mapObj : rawResults) { Map<String, Object> rowMap = (Map<String, Object>) mapObj; dataRows.add(new Object[]{rowMap.get("product_id"), rowMap.get("product_name"), rowMap.get("sold_in_orders"), rowMap.get("returned_in_orders"), rowMap.get("return_rate_percentage")}); } }
                // New Queries from User (Set 2 - NQ1-NQ10)
                else if (selectedQueryDesc.equals("NQ1. Top 20 Products Sold (Revenue, 01/03/24-01/07/24)")) { LocalDate startDate = LocalDate.of(2024, 3, 1); LocalDate endDate = LocalDate.of(2024, 7, 1); rawResults = productDb.getTop20SellingProductsByRevenue(startDate, endDate); columnNames = new String[]{"Product Name", "Quantity Sold", "Revenue", "Category Name"}; for(Object mapObj : rawResults) { Map<String, Object> rowMap = (Map<String, Object>) mapObj; dataRows.add(new Object[]{rowMap.get("product_name"), rowMap.get("quantity_sold"), rowMap.get("revenue"), rowMap.get("category_name")}); } }
                else if (selectedQueryDesc.equals("NQ2a. Category Revenue (01/07/24-31/12/24)")) { LocalDate startDate = LocalDate.of(2024, 7, 1); LocalDate endDate = LocalDate.of(2024, 12, 31); rawResults = orderDb.getRevenueByCategory(startDate, endDate); columnNames = new String[]{"Category Name", "Total Revenue", "Total Orders"}; for(Object mapObj : rawResults) { Map<String, Object> rowMap = (Map<String, Object>) mapObj; dataRows.add(new Object[]{rowMap.get("category_name"), rowMap.get("total_revenue_category"), rowMap.get("total_orders_category")}); } }
                else if (selectedQueryDesc.equals("NQ2b. Top 3 Products/Category (Revenue, 01/07/24-31/12/24)")) { LocalDate startDate = LocalDate.of(2024, 7, 1); LocalDate endDate = LocalDate.of(2024, 12, 31); rawResults = orderDb.getTopProductsPerCategoryByRevenue(3, startDate, endDate); columnNames = new String[]{"Category Name", "Rank", "Product Name", "Product Revenue"};  for(Object mapObj : rawResults) { Map<String, Object> rowMap = (Map<String, Object>) mapObj; dataRows.add(new Object[]{rowMap.get("category_name"), rowMap.get("product_rank_in_category"), rowMap.get("product_name"), rowMap.get("product_revenue_in_category")}); } }
                else if (selectedQueryDesc.equals("NQ2c. Top 3 Employees/Category (Sales, 01/07/24-31/12/24)")) { LocalDate startDate = LocalDate.of(2024, 7, 1); LocalDate endDate = LocalDate.of(2024, 12, 31); rawResults = orderDb.getTopEmployeesPerCategoryBySales(3, startDate, endDate); columnNames = new String[]{"Category Name", "Rank", "Employee Name", "Sales in Category"};  for(Object mapObj : rawResults) { Map<String, Object> rowMap = (Map<String, Object>) mapObj; dataRows.add(new Object[]{rowMap.get("category_name"), rowMap.get("employee_rank_in_category"), rowMap.get("employee_name"), rowMap.get("employee_sales_in_category")}); } }
                else if (selectedQueryDesc.equals("NQ3. Customers (18-30yo, >=5 orders, 01/02/24-31/08/24)")) { LocalDate startDate = LocalDate.of(2024, 2, 1); LocalDate endDate = LocalDate.of(2024, 8, 31); rawResults = customerDb.findCustomersByAgeAndOrderCriteria(18, 30, 5, startDate, endDate); columnNames = new String[]{"Customer Name", "Age", "Total Orders", "Total Spent", "Last Order Date"}; for(Object mapObj : rawResults) { Map<String, Object> rowMap = (Map<String, Object>) mapObj; Object lastOrderDateObj = rowMap.get("last_order_date"); String lastOrderDateStr = "N/A"; if (lastOrderDateObj instanceof java.sql.Date) { lastOrderDateStr = ((java.sql.Date) lastOrderDateObj).toLocalDate().format(dateFormatter); } dataRows.add(new Object[]{rowMap.get("customer_name"), rowMap.get("age"), rowMap.get("total_orders"), rowMap.get("total_spent"), lastOrderDateStr}); } }
                else if (selectedQueryDesc.equals("NQ4. Top Spender per Top 15 Categories")) { rawResults = customerDb.getTopSpendersInTopCategories(15, 1); columnNames = new String[]{"Category Name", "Top Customer Name", "Amount Spent in Category"};  for(Object mapObj : rawResults) { Map<String, Object> rowMap = (Map<String, Object>) mapObj; dataRows.add(new Object[]{rowMap.get("category_name"), rowMap.get("customer_name"), rowMap.get("amount_spent_in_category")}); } }
                else if (selectedQueryDesc.equals("NQ5. Highest Value Order per Month (2024)")) { rawResults = orderDb.getHighestValueOrderPerMonth(2024); columnNames = new String[]{"Month (YYYY-MM)", "Order ID", "Customer Name", "Total Amount"}; for(Object mapObj : rawResults) { Map<String, Object> rowMap = (Map<String, Object>) mapObj; dataRows.add(new Object[]{rowMap.get("order_month"), rowMap.get("order_id"), rowMap.get("customer_name"), rowMap.get("total_amount")}); } }
                else if (selectedQueryDesc.equals("NQ6. Unpaid Orders (Male Customers, name with 'z')")) { rawResults = orderDb.getUnpaidOrdersForSpecificMaleCustomers("z"); columnNames = new String[]{"Order ID", "Customer Name", "Order Date", "Total Amount", "Status"}; for(Object mapObj : rawResults) { Map<String, Object> rowMap = (Map<String, Object>) mapObj; Object orderDateObj = rowMap.get("order_date"); String orderDateStr = "N/A"; if (orderDateObj instanceof java.sql.Date) { orderDateStr = ((java.sql.Date) orderDateObj).toLocalDate().format(dateFormatter); } dataRows.add(new Object[]{rowMap.get("order_id"), rowMap.get("customer_name"), orderDateStr, rowMap.get("total_amount"), rowMap.get("status")}); } }
                else if (selectedQueryDesc.equals("NQ7. Employees NOT selling 'Apple iMac 24 M3' (04/2024)")) { rawResults = employeeDb.getEmployeesNotSellingProductInMonth("Apple iMac 24 M3", 4, 2024); columnNames = new String[]{"Emp. ID", "First Name", "Last Name", "Role", "Email"}; for(Object obj : rawResults) { Employee emp = (Employee) obj; dataRows.add(new Object[]{emp.getEmployeeId(), emp.getFirstName(), emp.getLastName(), emp.getRole(), emp.getEmail()});}}
                else if (selectedQueryDesc.equals("NQ8. Top 10 Employee Performance (Payment/Day, by 31/12/2024)")) { LocalDate referenceDate = LocalDate.of(2024, 12, 31); rawResults = employeeDb.getTopPerformingEmployeesByPaymentPerDay(10, referenceDate); columnNames = new String[]{"Emp. ID", "Employee Name", "Role", "Hire Date", "Total Payments", "Days Worked", "Performance (Payment/Day)"}; for(Object mapObj : rawResults) { Map<String, Object> rowMap = (Map<String, Object>) mapObj; Object hireDateObj = rowMap.get("hire_day"); String hireDateStr = "N/A"; if (hireDateObj instanceof java.sql.Date) { hireDateStr = ((java.sql.Date) hireDateObj).toLocalDate().format(dateFormatter); } dataRows.add(new Object[]{rowMap.get("employee_id"), rowMap.get("employee_name"), rowMap.get("role"), hireDateStr, rowMap.get("total_payments_amount"), String.format("%.0f", rowMap.get("days_worked")), ((BigDecimal)rowMap.get("performance_metric")).setScale(2, RoundingMode.HALF_UP) }); } }
                else if (selectedQueryDesc.equals("NQ9. Top 10 Payments by Most Orders Linked")) { rawResults = paymentDb.getTopPaymentsByOrderCount(10); columnNames = new String[]{"Payment ID", "Method", "Payment Amount", "# Orders Linked"}; for(Object mapObj : rawResults) { Map<String, Object> rowMap = (Map<String, Object>) mapObj; dataRows.add(new Object[]{rowMap.get("payment_id"), rowMap.get("payment_method"), rowMap.get("payment_total_amount"), rowMap.get("number_of_orders_paid")}); } }
                else if (selectedQueryDesc.equals("NQ10. Payments for Single Order by Highest Paid Employee")) { rawResults = paymentDb.getSingleOrderPaymentsByHighestPaidEmployee(); columnNames = new String[]{"Employee Name", "Salary", "Payment ID", "Payment Amount", "Order ID Linked"};  for(Object mapObj : rawResults) { Map<String, Object> rowMap = (Map<String, Object>) mapObj; dataRows.add(new Object[]{rowMap.get("employee_name"), rowMap.get("employee_salary"), rowMap.get("payment_id"), rowMap.get("payment_amount"), rowMap.get("order_id_linked")}); } }

                // --- New Simpler Queries (Set 3 - NQ11-NQ20 - Sửa đổi) ---
                else if (selectedQueryDesc.equals("NQ11. Số lượng sản phẩm theo từng Danh mục")) {
                    rawResults = productDb.getProductCountByCategory();
                    columnNames = new String[]{"Category Name", "Product Count"};
                    for(Object mapObj : rawResults) { Map<String, Object> rowMap = (Map<String, Object>) mapObj; dataRows.add(new Object[]{rowMap.get("category_name"), rowMap.get("product_count")}); }
                }
                else if (selectedQueryDesc.equals("NQ12. SL Khách hàng mới (3 năm gần nhất, theo tháng)")) {
                    rawResults = customerDb.getNewCustomersByMonthForLastNYears(3);
                    columnNames = new String[]{"Registration Month (YYYY-MM)", "New Customer Count"};
                    for(Object mapObj : rawResults) { Map<String, Object> rowMap = (Map<String, Object>) mapObj; dataRows.add(new Object[]{rowMap.get("registration_month_year"), rowMap.get("new_customer_count")}); }
                }
                else if (selectedQueryDesc.equals("NQ13. Đơn hàng > 4,000")) {
                    rawResults = orderDb.getOrdersWithValueGreaterThan(new BigDecimal("4000"));
                    columnNames = new String[]{"Order ID", "Customer", "Order Date", "Status", "Total Amount"};
                    for(Object obj : rawResults) { Order o = (Order) obj; dataRows.add(new Object[]{o.getOrderId(), o.getCustomerName(), o.getOrderDate() != null ? o.getOrderDate().format(dateFormatter) : "N/A", o.getStatus(), o.getTotalAmount()});}
                }
                else if (selectedQueryDesc.equals("NQ14. Nhân viên được thuê (2 năm gần nhất)")) {
                    rawResults = employeeDb.getEmployeesHiredInLastNYears(2);
                    columnNames = new String[]{"ID", "Full Name", "Role", "Email", "Hire Date"};
                    for(Object obj : rawResults) { Employee emp = (Employee) obj; dataRows.add(new Object[]{emp.getEmployeeId(), emp.getFirstName() + " " + emp.getLastName(), emp.getRole(), emp.getEmail(), emp.getHireDay()!=null ? emp.getHireDay().format(dateFormatter):"N/A"});}
                }
                else if (selectedQueryDesc.equals("NQ15. Tổng tiền & SL Thanh toán theo Phương thức")) {
                    rawResults = paymentDb.getPaymentSummaryByMethod();
                    columnNames = new String[]{"Payment Method", "Payment Count", "Total Amount Sum"};
                    for(Object mapObj : rawResults) { Map<String, Object> rowMap = (Map<String, Object>) mapObj; dataRows.add(new Object[]{rowMap.get("payment_method"), rowMap.get("payment_count"), rowMap.get("total_amount_sum")}); }
                }
                else if (selectedQueryDesc.equals("NQ16. Sản phẩm mới ra mắt (3 năm gần nhất)")) {
                    rawResults = productDb.getRecentlyPublishedProductsInLastNYears(3);
                    columnNames = new String[]{"ID", "Name", "Model", "Category", "Price", "Published"};
                    for(Object obj : rawResults) { Product p = (Product) obj; dataRows.add(new Object[]{p.getProductId(), p.getSpecificProductName(), p.getModel(), p.getCategoryName(), p.getPrice(), p.getYearPublish() != null ? p.getYearPublish().format(dateFormatter) : "N/A"});}
                }
                else if (selectedQueryDesc.equals("NQ17. SL Đơn hàng trong 2 năm theo Trạng thái")) { // Sửa đổi ở đây
                    rawResults = orderDb.getOrderCountByStatusForLastNYears(2); // Gọi phương thức mới
                    columnNames = new String[]{"Status", "Order Count (Last 2 Years)"}; // Sửa tên cột
                    for(Object mapObj : rawResults) { Map<String, Object> rowMap = (Map<String, Object>) mapObj; dataRows.add(new Object[]{rowMap.get("status"), rowMap.get("order_count")}); } // Sửa key lấy dữ liệu
                }
                else if (selectedQueryDesc.equals("NQ18. Khách hàng ở 'Green Valley'")) {
                    rawResults = customerDb.getCustomersByAddressContaining("Green Valley");
                    columnNames = new String[]{"ID", "Username", "Full Name", "Email", "Address"};
                    for(Object obj : rawResults) { Customer c = (Customer) obj; dataRows.add(new Object[]{c.getCustomerId(), c.getUsername(), c.getFirstName() + " " + c.getLastName(), c.getEmail(), c.getAddress()});}
                }
                else if (selectedQueryDesc.equals("NQ19. Sản phẩm giá từ 500 - 1,000")) {
                    rawResults = productDb.getProductsByPriceRange(new BigDecimal("500"), new BigDecimal("1000"));
                    columnNames = new String[]{"ID", "Name", "Model", "Category", "Price"};
                    for(Object obj : rawResults) { Product p = (Product) obj; dataRows.add(new Object[]{p.getProductId(), p.getSpecificProductName(), p.getModel(), p.getCategoryName(), p.getPrice()});}
                }
                else if (selectedQueryDesc.equals("NQ20. Tổng doanh thu (4 năm qua, theo năm)")) {
                    rawResults = orderDb.getTotalRevenueForLastNYearsByYear(4);
                    columnNames = new String[]{"Sales Year", "Yearly Revenue"};
                    for(Object mapObj : rawResults) { Map<String, Object> rowMap = (Map<String, Object>) mapObj; dataRows.add(new Object[]{rowMap.get("sales_year"), rowMap.get("yearly_revenue")}); }
                }

                else {
                    dynamicTableModel.clearData();
                    JOptionPane.showMessageDialog(frame, "Selected query is not yet implemented.", "Not Implemented", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                dynamicTableModel.setData(dataRows, columnNames);
                if (dataRows.isEmpty() && errorMessage == null) {
                    JOptionPane.showMessageDialog(frame, "No results found for this query.", "Query Results", JOptionPane.INFORMATION_MESSAGE);
                }

            } catch (SQLException ex) {
                errorMessage = "SQL Error executing query '" + selectedQueryDesc + "': " + ex.getMessage();
                ex.printStackTrace();
            } catch (Exception ex) {
                errorMessage = "Unexpected error executing query '" + selectedQueryDesc + "': " + ex.getMessage();
                ex.printStackTrace();
            }

            if (errorMessage != null) {
                dynamicTableModel.clearData();
                JOptionPane.showMessageDialog(frame, errorMessage, "Query Execution Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void showScreen() {
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }

    public JFrame getFrame() {
        return frame;
    }
}
