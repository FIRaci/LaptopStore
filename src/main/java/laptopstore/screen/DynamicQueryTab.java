package laptopstore.screen;

import laptopstore.data.CategoryDataStore;
import laptopstore.data.DynamicQueryDataStore;
import laptopstore.model.Category;
import laptopstore.model.Customer; // For Customer specific queries
import laptopstore.model.Employee; // For Employee specific queries
import laptopstore.model.Order;    // For Order specific queries
import laptopstore.screen.tablemodel.DynamicTableModel;
import org.jdesktop.swingx.JXDatePicker;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DynamicQueryTab extends JPanel {

    private JComboBox<String> entitySelector;
    private JPanel filterPanel;
    private JButton searchButton;
    private JTable resultsTable;
    private DynamicTableModel resultsTableModel;
    private JScrollPane resultsScrollPane;

    // DataStores
    private final DynamicQueryDataStore dynamicQueryDb;
    private final CategoryDataStore categoryDb;

    // --- Filter components ---
    // Products
    private JComboBox<Category> productCategoryCombo;
    private JComboBox<String> productTypeCombo;
    private JComboBox<String> productBrandCombo;
    private JComboBox<String> priceFilterTypeCombo;
    private JTextField minPriceField, maxPriceField, specificPriceField, topNPriceField;
    private JLabel minPriceLabel, maxPriceLabel, specificPriceLabel, topNPriceLabel;
    private JXDatePicker productYearPicker;
    private JTextField productNameKeywordField;

    // Customers
    private JTextField custKeywordField;
    private JComboBox<String> custGenderFilterCombo;
    private JXDatePicker custDobStartPicker, custDobEndPicker;
    private JComboBox<String> custSpendingTierCombo; // Ví dụ: Top Spenders

    // Orders
    private JComboBox<String> orderStatusFilterCombo;
    private JXDatePicker orderDateStartPicker, orderDateEndPicker;
    private JTextField orderMinTotalField, orderMaxTotalField;
    private JTextField orderCustomerKeywordField; // Tìm đơn hàng theo tên/ID khách hàng
    private JTextField orderProductKeywordField;  // Tìm đơn hàng chứa sản phẩm (tên/ID)

    // Employees
    private JTextField empKeywordField;
    private JComboBox<String> empRoleFilterCombo;
    private JXDatePicker empHireDateStartPicker, empHireDateEndPicker;
    private JTextField empMinSalaryField, empMaxSalaryField;


    public DynamicQueryTab() {
        this.dynamicQueryDb = new DynamicQueryDataStore();
        this.categoryDb = new CategoryDataStore();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initComponents();
        layoutComponents();
        addListeners();

        loadInitialFilterData();
        updateFilterPanel("Products"); // Mặc định
    }

    private void initComponents() {
        entitySelector = new JComboBox<>(new String[]{"Products", "Customers", "Orders", "Employees"});
        filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Criteria"));

        searchButton = new JButton("Search");
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchButton.setIcon(UIManager.getIcon("FileView.findIcon"));

        resultsTableModel = new DynamicTableModel();
        resultsTable = new JTable(resultsTableModel);
        styleTable(resultsTable);
        resultsScrollPane = new JScrollPane(resultsTable);

        // Product Filters
        productCategoryCombo = new JComboBox<>();
        productTypeCombo = new JComboBox<>();
        productBrandCombo = new JComboBox<>();
        priceFilterTypeCombo = new JComboBox<>(new String[]{
                "Any Price", "Price Range", "Less than or Equal to",
                "Greater than or Equal to", "Highest Price (Top N)", "Lowest Price (Top N)"
        });
        minPriceField = new JTextField(10);
        maxPriceField = new JTextField(10);
        specificPriceField = new JTextField(10);
        topNPriceField = new JTextField(3); topNPriceField.setText("5");
        minPriceLabel = new JLabel("Min Price (Range):");
        maxPriceLabel = new JLabel("Max Price (Range):");
        specificPriceLabel = new JLabel("Price Point:");
        topNPriceLabel = new JLabel("Number of Results (N):");
        productYearPicker = new JXDatePicker(); productYearPicker.setFormats(new SimpleDateFormat("yyyy"));
        productNameKeywordField = new JTextField(20);

        // Customer Filters
        custKeywordField = new JTextField(20);
        custGenderFilterCombo = new JComboBox<>(new String[]{"Any", "M", "F", "O"});
        custDobStartPicker = new JXDatePicker(); custDobStartPicker.setFormats(new SimpleDateFormat("dd/MM/yyyy"));
        custDobEndPicker = new JXDatePicker(); custDobEndPicker.setFormats(new SimpleDateFormat("dd/MM/yyyy"));
        custSpendingTierCombo = new JComboBox<>(new String[]{"Any", "Top Spenders (by Total Amount)", "Low Activity (Few Orders)"});


        // Order Filters
        orderStatusFilterCombo = new JComboBox<>(new String[]{"Any", "Pending", "Processing", "Shipped", "Delivered", "Cancelled", "Returned", "Awaiting Payment", "Awaiting Stock"});
        orderDateStartPicker = new JXDatePicker(); orderDateStartPicker.setFormats(new SimpleDateFormat("dd/MM/yyyy"));
        orderDateEndPicker = new JXDatePicker(); orderDateEndPicker.setFormats(new SimpleDateFormat("dd/MM/yyyy"));
        orderMinTotalField = new JTextField(10);
        orderMaxTotalField = new JTextField(10);
        orderCustomerKeywordField = new JTextField(15);
        orderProductKeywordField = new JTextField(15);

        // Employee Filters
        empKeywordField = new JTextField(20);
        empRoleFilterCombo = new JComboBox<>(); // Sẽ load từ DB
        empHireDateStartPicker = new JXDatePicker(); empHireDateStartPicker.setFormats(new SimpleDateFormat("dd/MM/yyyy"));
        empHireDateEndPicker = new JXDatePicker(); empHireDateEndPicker.setFormats(new SimpleDateFormat("dd/MM/yyyy"));
        empMinSalaryField = new JTextField(10);
        empMaxSalaryField = new JTextField(10);
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
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select Entity to Query:"));
        topPanel.add(entitySelector);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(filterPanel), BorderLayout.WEST);
        add(resultsScrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(searchButton);
        add(bottomPanel, BorderLayout.SOUTH);
        filterPanel.setPreferredSize(new Dimension(380, 0));
    }

    private void addListeners() {
        entitySelector.addActionListener(e -> {
            String selectedEntity = (String) entitySelector.getSelectedItem();
            updateFilterPanel(selectedEntity);
            resultsTableModel.clearData();
        });
        searchButton.addActionListener(this::performSearch);

        productTypeCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                Object item = e.getItem();
                if (item instanceof String) {
                    String selectedType = (String) item;
                    if ("Any Type".equals(selectedType) || selectedType == null) {
                        loadAllCategoriesForProductFilter();
                    } else {
                        updateProductCategoryCombo(selectedType);
                    }
                } else if (item == null) {
                    loadAllCategoriesForProductFilter();
                }
            }
        });
        priceFilterTypeCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updatePriceInputFieldsVisibility();
            }
        });
    }

    private void addLabelAndComponentToFilterPanel(String labelText, JComponent component, GridBagConstraints gbc) {
        JLabel label = new JLabel(labelText);
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.3;
        filterPanel.add(label, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.7;
        filterPanel.add(component, gbc);
        gbc.gridy++; // Tự động tăng y cho dòng tiếp theo
    }
    private void addLabelAndComponentToFilterPanel(JLabel label, JComponent component, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.3;
        filterPanel.add(label, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.7;
        filterPanel.add(component, gbc);
        gbc.gridy++;
    }

    private void updateFilterPanel(String entity) {
        filterPanel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0; // Reset y cho mỗi lần build lại panel
        gbc.insets = new Insets(5, 5, 5, 5);

        if ("Products".equals(entity)) {
            addLabelAndComponentToFilterPanel("Product Name (Keyword):", productNameKeywordField, gbc);
            addLabelAndComponentToFilterPanel("Product Type:", productTypeCombo, gbc);
            addLabelAndComponentToFilterPanel("Category:", productCategoryCombo, gbc);
            addLabelAndComponentToFilterPanel("Brand:", productBrandCombo, gbc);
            addLabelAndComponentToFilterPanel("Publish Year:", productYearPicker, gbc);
            addLabelAndComponentToFilterPanel("Price Options:", priceFilterTypeCombo, gbc);
            addLabelAndComponentToFilterPanel(specificPriceLabel, specificPriceField, gbc);
            addLabelAndComponentToFilterPanel(minPriceLabel, minPriceField, gbc);
            addLabelAndComponentToFilterPanel(maxPriceLabel, maxPriceField, gbc);
            addLabelAndComponentToFilterPanel(topNPriceLabel, topNPriceField, gbc);
            updatePriceInputFieldsVisibility();
        } else if ("Customers".equals(entity)) {
            addLabelAndComponentToFilterPanel("Keyword (Name, User, Email):", custKeywordField, gbc);
            addLabelAndComponentToFilterPanel("Gender:", custGenderFilterCombo, gbc);
            addLabelAndComponentToFilterPanel("Date of Birth (Start):", custDobStartPicker, gbc);
            addLabelAndComponentToFilterPanel("Date of Birth (End):", custDobEndPicker, gbc);
            addLabelAndComponentToFilterPanel("Spending Tier:", custSpendingTierCombo, gbc);
        } else if ("Orders".equals(entity)) {
            addLabelAndComponentToFilterPanel("Status:", orderStatusFilterCombo, gbc);
            addLabelAndComponentToFilterPanel("Order Date (Start):", orderDateStartPicker, gbc);
            addLabelAndComponentToFilterPanel("Order Date (End):", orderDateEndPicker, gbc);
            addLabelAndComponentToFilterPanel("Min Total Amount:", orderMinTotalField, gbc);
            addLabelAndComponentToFilterPanel("Max Total Amount:", orderMaxTotalField, gbc);
            addLabelAndComponentToFilterPanel("Customer (Name/ID/User):", orderCustomerKeywordField, gbc);
            addLabelAndComponentToFilterPanel("Product in Order (Name/ID):", orderProductKeywordField, gbc);
        } else if ("Employees".equals(entity)) {
            addLabelAndComponentToFilterPanel("Keyword (Name, Email, Phone):", empKeywordField, gbc);
            addLabelAndComponentToFilterPanel("Role:", empRoleFilterCombo, gbc);
            addLabelAndComponentToFilterPanel("Hire Date (Start):", empHireDateStartPicker, gbc);
            addLabelAndComponentToFilterPanel("Hire Date (End):", empHireDateEndPicker, gbc);
            addLabelAndComponentToFilterPanel("Min Salary:", empMinSalaryField, gbc);
            addLabelAndComponentToFilterPanel("Max Salary:", empMaxSalaryField, gbc);
            loadDistinctRolesForEmployeeFilter(empRoleFilterCombo);
        }
        GridBagConstraints fillerGbc = (GridBagConstraints) gbc.clone();
        fillerGbc.weighty = 1.0; // Đẩy các component lên trên
        fillerGbc.gridy = gbc.gridy; // Đảm bảo nó ở dòng tiếp theo
        filterPanel.add(new JPanel(), fillerGbc); // JPanel trống làm filler

        filterPanel.revalidate();
        filterPanel.repaint();
    }

    private void loadInitialFilterData() {
        try {
            List<String> types = dynamicQueryDb.getDistinctStringValues("PRODUCTS", "product_type");
            productTypeCombo.removeAllItems();
            productTypeCombo.addItem("Any Type");
            for (String type : types) {
                if (type != null && !type.trim().isEmpty()) productTypeCombo.addItem(type);
            }
        } catch (SQLException e) { showError("Error loading product types: " + e.getMessage()); }

        loadAllCategoriesForProductFilter();

        try {
            List<String> brands = dynamicQueryDb.getDistinctStringValues("PRODUCTS", "brand");
            productBrandCombo.removeAllItems();
            productBrandCombo.addItem("Any Brand");
            for (String brand : brands) {
                if (brand != null && !brand.trim().isEmpty()) productBrandCombo.addItem(brand);
            }
        } catch (SQLException e) { showError("Error loading brands: " + e.getMessage()); }

        // Load roles for employee filter
        loadDistinctRolesForEmployeeFilter(empRoleFilterCombo);
    }

    private void loadAllCategoriesForProductFilter() {
        try {
            List<Category> categories = categoryDb.getAllCategories();
            productCategoryCombo.removeAllItems();
            productCategoryCombo.addItem(null);
            for (Category cat : categories) {
                productCategoryCombo.addItem(cat);
            }
            productCategoryCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Category) setText(((Category) value).getCategoryName());
                    else setText("Any Category");
                    return this;
                }
            });
            if (productCategoryCombo.getItemCount() > 0) productCategoryCombo.setSelectedIndex(0);
        } catch (SQLException e) { showError("Error loading all categories: " + e.getMessage()); }
    }

    private void updateProductCategoryCombo(String productType) {
        try {
            List<Category> categories = categoryDb.getCategoriesByProductType(productType);
            productCategoryCombo.removeAllItems();
            productCategoryCombo.addItem(null);
            for (Category cat : categories) {
                productCategoryCombo.addItem(cat);
            }
            if (productCategoryCombo.getItemCount() > 0) productCategoryCombo.setSelectedIndex(0);
        } catch (SQLException e) {
            showError("Error loading categories for type '" + productType + "': " + e.getMessage());
            productCategoryCombo.removeAllItems(); productCategoryCombo.addItem(null);
            if (productCategoryCombo.getItemCount() > 0) productCategoryCombo.setSelectedIndex(0);
        }
    }

    private void updatePriceInputFieldsVisibility() {
        String selectedPriceFilter = (String) priceFilterTypeCombo.getSelectedItem();
        if (selectedPriceFilter == null) return;

        specificPriceField.setVisible(false); specificPriceLabel.setVisible(false);
        minPriceField.setVisible(false); minPriceLabel.setVisible(false);
        maxPriceField.setVisible(false); maxPriceLabel.setVisible(false);
        topNPriceField.setVisible(false); topNPriceLabel.setVisible(false);

        switch (selectedPriceFilter) {
            case "Price Range":
                minPriceField.setVisible(true); minPriceLabel.setVisible(true);
                maxPriceField.setVisible(true); maxPriceLabel.setVisible(true);
                break;
            case "Less than or Equal to":
                specificPriceLabel.setText("Max Price:");
                specificPriceField.setVisible(true); specificPriceLabel.setVisible(true);
                break;
            case "Greater than or Equal to":
                specificPriceLabel.setText("Min Price:");
                specificPriceField.setVisible(true); specificPriceLabel.setVisible(true);
                break;
            case "Highest Price (Top N)":
            case "Lowest Price (Top N)":
                topNPriceField.setVisible(true); topNPriceLabel.setVisible(true);
                break;
        }
        filterPanel.revalidate(); filterPanel.repaint();
    }

    private void loadDistinctRolesForEmployeeFilter(JComboBox<String> comboBox) {
        try {
            List<String> roles = dynamicQueryDb.getDistinctStringValues("EMPLOYEES", "role");
            comboBox.removeAllItems();
            comboBox.addItem("Any Role");
            for (String role : roles) {
                if (role != null && !role.trim().isEmpty()) comboBox.addItem(role.trim()); // Trim roles
            }
        } catch (SQLException e) { showError("Error loading employee roles: " + e.getMessage()); }
    }

    private void performSearch(ActionEvent e) {
        String selectedEntity = (String) entitySelector.getSelectedItem();
        if (selectedEntity == null) { showError("Please select an entity to query."); return; }

        StringBuilder queryBuilder = new StringBuilder();
        List<Object> params = new ArrayList<>();
        String baseSelect;
        String defaultOrderByClause = "";
        String specificOrderByClause = ""; // For price high/low, spending, etc.
        String limitClause = " LIMIT 100";

        try {
            switch (selectedEntity) {
                case "Products":
                    baseSelect = "SELECT p.product_id, p.product_name, p.model, p.brand, p.price, p.stock_quantity, p.year_publish, c.category_name, p.product_type ";
                    queryBuilder.append(baseSelect)
                            .append("FROM PRODUCTS p LEFT JOIN categories c ON p.category_id = c.category_id WHERE 1=1");
                    defaultOrderByClause = " ORDER BY p.product_name";

                    String nameKeyword = productNameKeywordField.getText().trim();
                    if (!nameKeyword.isEmpty()) {
                        queryBuilder.append(" AND (p.product_name ILIKE ? OR p.model ILIKE ? OR p.brand ILIKE ? OR p.description ILIKE ?)");
                        String kw = "%" + nameKeyword + "%"; params.add(kw); params.add(kw); params.add(kw); params.add(kw);
                    }
                    Category selCat = (Category) productCategoryCombo.getSelectedItem();
                    if (selCat != null) { queryBuilder.append(" AND p.category_id = ?"); params.add(selCat.getCategoryId()); }
                    String selType = (String) productTypeCombo.getSelectedItem();
                    if (selType != null && !"Any Type".equals(selType)) { queryBuilder.append(" AND p.product_type = ?"); params.add(selType); }
                    String selBrand = (String) productBrandCombo.getSelectedItem();
                    if (selBrand != null && !"Any Brand".equals(selBrand)) { queryBuilder.append(" AND p.brand = ?"); params.add(selBrand); }
                    Date selDate = productYearPicker.getDate();
                    if (selDate != null) {
                        queryBuilder.append(" AND EXTRACT(YEAR FROM p.year_publish) = ?");
                        params.add(selDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear());
                    }

                    String priceFilterType = (String) priceFilterTypeCombo.getSelectedItem();
                    if (priceFilterType != null) {
                        switch (priceFilterType) {
                            case "Price Range":
                                if (!minPriceField.getText().trim().isEmpty()) { params.add(new BigDecimal(minPriceField.getText().trim().replace(",", ""))); queryBuilder.append(" AND p.price >= ?"); }
                                if (!maxPriceField.getText().trim().isEmpty()) { params.add(new BigDecimal(maxPriceField.getText().trim().replace(",", ""))); queryBuilder.append(" AND p.price <= ?"); }
                                break;
                            case "Less than or Equal to":
                                if (!specificPriceField.getText().trim().isEmpty()) { params.add(new BigDecimal(specificPriceField.getText().trim().replace(",", ""))); queryBuilder.append(" AND p.price <= ?"); }
                                break;
                            case "Greater than or Equal to":
                                if (!specificPriceField.getText().trim().isEmpty()) { params.add(new BigDecimal(specificPriceField.getText().trim().replace(",", ""))); queryBuilder.append(" AND p.price >= ?"); }
                                break;
                            case "Highest Price (Top N)":
                                specificOrderByClause = " ORDER BY p.price DESC";
                                try { limitClause = " LIMIT " + Math.max(1, Integer.parseInt(topNPriceField.getText().trim())); } catch (NumberFormatException ignored) { limitClause = " LIMIT 5"; }
                                break;
                            case "Lowest Price (Top N)":
                                specificOrderByClause = " ORDER BY p.price ASC";
                                try { limitClause = " LIMIT " + Math.max(1, Integer.parseInt(topNPriceField.getText().trim())); } catch (NumberFormatException ignored) { limitClause = " LIMIT 5"; }
                                break;
                        }
                    }
                    break;

                case "Customers":
                    baseSelect = "SELECT c.customer_id, c.username, c.email, c.first_name, c.last_name, c.phone, c.address, c.date_of_birth, c.gender ";
                    String custSpendingFilter = (String) custSpendingTierCombo.getSelectedItem();
                    if ("Top Spenders (by Total Amount)".equals(custSpendingFilter)) {
                        baseSelect += ", SUM(o.total_amount) AS total_spent ";
                        queryBuilder.append(baseSelect)
                                .append("FROM CUSTOMERS c LEFT JOIN ORDERS o ON c.customer_id = o.customer_id WHERE 1=1 ");
                        defaultOrderByClause = " GROUP BY c.customer_id, c.username, c.email, c.first_name, c.last_name, c.phone, c.address, c.date_of_birth, c.gender ORDER BY total_spent DESC NULLS LAST";
                        limitClause = " LIMIT 10"; // Default top 10 spenders
                    } else {
                        queryBuilder.append(baseSelect).append("FROM CUSTOMERS c WHERE 1=1");
                        defaultOrderByClause = " ORDER BY c.last_name, c.first_name";
                    }

                    String cKeyword = custKeywordField.getText().trim();
                    if (!cKeyword.isEmpty()) {
                        queryBuilder.append(" AND (c.first_name ILIKE ? OR c.last_name ILIKE ? OR c.username ILIKE ? OR c.email ILIKE ?)");
                        String kw = "%" + cKeyword + "%"; params.add(kw); params.add(kw); params.add(kw); params.add(kw);
                    }
                    String cGender = (String) custGenderFilterCombo.getSelectedItem();
                    if (cGender != null && !"Any".equals(cGender)) { queryBuilder.append(" AND c.gender = ?"); params.add(cGender.charAt(0)); }
                    Date cDobStart = custDobStartPicker.getDate();
                    if (cDobStart != null) { queryBuilder.append(" AND c.date_of_birth >= ?"); params.add(java.sql.Date.valueOf(cDobStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())); }
                    Date cDobEnd = custDobEndPicker.getDate();
                    if (cDobEnd != null) { queryBuilder.append(" AND c.date_of_birth <= ?"); params.add(java.sql.Date.valueOf(cDobEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())); }

                    if ("Low Activity (Few Orders)".equals(custSpendingFilter) && !baseSelect.contains("SUM(o.total_amount)")) { // Ensure not conflicting with top spender
                        // This query is a bit more complex to do efficiently in one go with other filters.
                        // For simplicity, one way is to find customers with 0 or 1 order.
                        // This might need a subquery or a different approach for better performance.
                        // Example: Find customers with 0 or 1 order.
                        queryBuilder.append(" AND c.customer_id IN (SELECT cust_id FROM (SELECT c2.customer_id as cust_id, COUNT(o2.order_id) as order_count FROM CUSTOMERS c2 LEFT JOIN ORDERS o2 ON c2.customer_id = o2.customer_id GROUP BY c2.customer_id) AS counts WHERE order_count <= 1)");
                        defaultOrderByClause = " ORDER BY c.last_name, c.first_name"; // Reset order by if it was changed by top spender
                    }
                    break;

                case "Orders":
                    baseSelect = "SELECT o.order_id, o.customer_id, cust.first_name || ' ' || cust.last_name AS customer_name, o.order_date, o.status, o.total_amount ";
                    queryBuilder.append(baseSelect)
                            .append("FROM ORDERS o JOIN CUSTOMERS cust ON o.customer_id = cust.customer_id WHERE 1=1");
                    defaultOrderByClause = " ORDER BY o.order_date DESC";

                    String oStatus = (String) orderStatusFilterCombo.getSelectedItem();
                    if (oStatus != null && !"Any".equals(oStatus)) { queryBuilder.append(" AND o.status = ?"); params.add(oStatus); }
                    Date oDateStart = orderDateStartPicker.getDate();
                    if (oDateStart != null) { queryBuilder.append(" AND o.order_date >= ?"); params.add(java.sql.Date.valueOf(oDateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())); }
                    Date oDateEnd = orderDateEndPicker.getDate();
                    if (oDateEnd != null) { queryBuilder.append(" AND o.order_date <= ?"); params.add(java.sql.Date.valueOf(oDateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())); }
                    if (!orderMinTotalField.getText().trim().isEmpty()) { params.add(new BigDecimal(orderMinTotalField.getText().trim().replace(",", ""))); queryBuilder.append(" AND o.total_amount >= ?"); }
                    if (!orderMaxTotalField.getText().trim().isEmpty()) { params.add(new BigDecimal(orderMaxTotalField.getText().trim().replace(",", ""))); queryBuilder.append(" AND o.total_amount <= ?"); }

                    String oCustKeyword = orderCustomerKeywordField.getText().trim();
                    if (!oCustKeyword.isEmpty()) {
                        queryBuilder.append(" AND (cust.first_name ILIKE ? OR cust.last_name ILIKE ? OR cust.username ILIKE ? OR CAST(o.customer_id AS VARCHAR) = ?)");
                        String kw = "%" + oCustKeyword + "%"; params.add(kw); params.add(kw); params.add(kw); params.add(oCustKeyword); // For ID exact match
                    }
                    String oProdKeyword = orderProductKeywordField.getText().trim();
                    if (!oProdKeyword.isEmpty()) {
                        queryBuilder.append(" AND o.order_id IN (SELECT od.order_id FROM ORDER_DETAILS od JOIN PRODUCTS p_od ON od.product_id = p_od.product_id WHERE p_od.product_name ILIKE ? OR CAST(p_od.product_id AS VARCHAR) = ?)");
                        params.add("%" + oProdKeyword + "%"); params.add(oProdKeyword);
                    }
                    break;

                case "Employees":
                    baseSelect = "SELECT e.employee_id, e.first_name, e.last_name, e.email, e.phone, e.role, e.hire_day, e.salary ";
                    queryBuilder.append(baseSelect)
                            .append("FROM EMPLOYEES e WHERE 1=1");
                    defaultOrderByClause = " ORDER BY e.last_name, e.first_name";

                    String eKeyword = empKeywordField.getText().trim();
                    if (!eKeyword.isEmpty()) {
                        queryBuilder.append(" AND (e.first_name ILIKE ? OR e.last_name ILIKE ? OR e.email ILIKE ? OR e.phone ILIKE ?)");
                        String kw = "%" + eKeyword + "%"; params.add(kw); params.add(kw); params.add(kw); params.add(kw);
                    }
                    String eRole = (String) empRoleFilterCombo.getSelectedItem();
                    if (eRole != null && !"Any Role".equals(eRole)) { queryBuilder.append(" AND e.role = ?"); params.add(eRole); }
                    Date eHireStart = empHireDateStartPicker.getDate();
                    if (eHireStart != null) { queryBuilder.append(" AND e.hire_day >= ?"); params.add(java.sql.Date.valueOf(eHireStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())); }
                    Date eHireEnd = empHireDateEndPicker.getDate();
                    if (eHireEnd != null) { queryBuilder.append(" AND e.hire_day <= ?"); params.add(java.sql.Date.valueOf(eHireEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())); }
                    if (!empMinSalaryField.getText().trim().isEmpty()) { params.add(new BigDecimal(empMinSalaryField.getText().trim().replace(",", ""))); queryBuilder.append(" AND e.salary >= ?"); }
                    if (!empMaxSalaryField.getText().trim().isEmpty()) { params.add(new BigDecimal(empMaxSalaryField.getText().trim().replace(",", ""))); queryBuilder.append(" AND e.salary <= ?"); }
                    break;

                default:
                    showError("Selected entity type is not yet supported for dynamic query.");
                    return;
            }

            // Append ORDER BY and LIMIT
            if (!specificOrderByClause.isEmpty()) { // Price high/low, spending tier takes precedence
                queryBuilder.append(specificOrderByClause).append(limitClause);
            } else {
                queryBuilder.append(defaultOrderByClause).append(limitClause);
            }


            // Execute query
            if (queryBuilder.length() > 0 && !queryBuilder.toString().contains("WHERE 1=1 AND ()")) { // Basic check
                System.out.println("Final Query: " + queryBuilder.toString()); // Debug
                System.out.println("Final Params: " + params); // Debug
                List<Map<String, Object>> resultData = dynamicQueryDb.executeQuery(queryBuilder.toString(), params);
                resultsTableModel.setData(resultData);
                if (resultData.isEmpty()) {
                    showMessage("No results found for the given criteria.");
                }
            } else {
                resultsTableModel.clearData(); // Clear table if no valid query was built
                showMessage("Please select at least one filter to perform a search.");
            }

        } catch (NumberFormatException ex) {
            showError("Invalid number format in one of the filter fields: " + ex.getMessage());
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
