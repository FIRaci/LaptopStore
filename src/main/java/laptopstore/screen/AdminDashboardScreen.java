package laptopstore.screen;

import laptopstore.data.*;
import laptopstore.model.*;
import laptopstore.screen.tablemodel.*;

import org.jdesktop.swingx.JXDatePicker;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class AdminDashboardScreen {

    private JFrame frame;
    private JTabbedPane tabPane;

    // --- DataStores ---
    private final ProductDataStore productDb;
    private final CategoryDataStore categoryDb;
    private final CustomerDataStore customerDb;
    private final EmployeeDataStore employeeDb;
    private final PaymentDataStore paymentDb;
    private final OrderDataStore orderDb;

    // --- Components chung ---
    private final SimpleDateFormat jxDatePickerFormatter = new SimpleDateFormat("dd/MM/yyyy");
    private final DateTimeFormatter localDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter localDateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // --- Tab Products ---
    private JTable productTable;
    private ProductTableModel productTableModel;
    private JTextField productIdFieldProd;
    private JTextField productNameFieldProd;
    private JTextField modelFieldProd;
    private JTextField brandFieldProd;
    private JTextArea descriptionAreaProd;
    private JTextField priceFieldProd;
    private JTextField stockFieldProd;
    private JComboBox<String> productTypeComboProd;
    private JComboBox<Category> categoryComboProd;
    private JXDatePicker publishDatePickerProd;

    // --- Tab Customers ---
    private JTable customerTable;
    private CustomerTableModel customerTableModel;
    private JTextField custIdField;
    private JTextField custUsernameField, custEmailField, custFirstNameField, custLastNameField, custAddressField, custPhoneField;
    private JComboBox<String> custGenderCombo;
    private JXDatePicker custDateOfBirthPicker;

    // --- Tab Employees ---
    private JTable employeeTable;
    private EmployeeTableModel employeeTableModel;
    private JTextField empIdField;
    private JTextField empFirstNameField, empLastNameField, empPhoneField, empEmailField, empAddressField, empBankNumberField, empRoleField, empSalaryField, empWorkDayField;
    private JComboBox<String> empGenderCombo;
    private JXDatePicker empHireDatePicker;

    // --- Tab Payments ---
    private JTable paymentTable;
    private PaymentTableModel paymentTableModel;
    private JTextField paymentIdField;
    private JComboBox<Employee> paymentEmployeeCombo;
    private JTextField paymentAmountField;
    private JXDatePicker paymentDatePicker;
    private JComboBox<String> paymentMethodCombo, paymentStatusCombo;
    private JTextArea paymentNotesArea;

    // --- Tab Orders ---
    private JTable orderTable;
    private OrderTableModel orderTableModel;
    private JTable orderItemTable;
    private OrderItemTableModel orderItemTableModel;
    private JTextField orderIdField;
    private JComboBox<Customer> orderCustomerCombo;
    private JComboBox<Payment> orderPaymentCombo;
    private JXDatePicker orderDatePicker;
    private JComboBox<String> orderStatusCombo;
    private JTextField orderShippingAddressField;
    private JTextArea orderNotesArea;
    private JComboBox<Product> orderItemProductCombo;
    private JTextField orderItemQuantityField;
    private List<OrderItem> tempOrderItems = new ArrayList<>();
    private JLabel lblOrderTotalAmount; // Để hiển thị tổng tiền của đơn hàng đang tạo/sửa


    public AdminDashboardScreen() {
        productDb = new ProductDataStore();
        categoryDb = new CategoryDataStore();
        customerDb = new CustomerDataStore();
        employeeDb = new EmployeeDataStore();
        paymentDb = new PaymentDataStore();
        orderDb = new OrderDataStore();

        frame = new JFrame("Laptop Store Admin Dashboard (JDBC)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1700, 1000); // Tăng kích thước
        frame.setLocationRelativeTo(null);

        tabPane = new JTabbedPane();
        tabPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        createManageProductsTab();
        createManageCustomersTab();
        createManageEmployeesTab();
        createPaymentHistoryTab();
        createManageOrdersTab();

        frame.add(tabPane);
        loadInitialDataForAllTabs();

        tabPane.addChangeListener(e -> {
            int selectedIndex = tabPane.getSelectedIndex();
            switch (selectedIndex) {
                case 0: loadProductsData(); loadCategoriesForProductForm(); break;
                case 1: loadCustomersData(); break;
                case 2: loadEmployeesData(); break;
                case 3: loadPaymentsData(); loadEmployeesForPaymentForm(); break;
                case 4: loadOrdersData(); loadCustomersForOrderForm(); loadPaymentsForOrderForm(); loadProductsForOrderItemForm(); break;
            }
        });
    }

    private void loadInitialDataForAllTabs() {
        // Tab Products
        loadProductsData();
        loadCategoriesForProductForm();
        // Tab Customers
        loadCustomersData();
        // Tab Employees
        loadEmployeesData();
        // Tab Payments
        loadPaymentsData();
        loadEmployeesForPaymentForm();
        // Tab Orders
        loadOrdersData();
        loadCustomersForOrderForm();
        loadPaymentsForOrderForm(); // Load payment cho JComboBox
        loadProductsForOrderItemForm(); // Load product cho JComboBox thêm item
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
        header.setBackground(new Color(230, 230, 230)); // Màu nền header nhạt hơn
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
        table.setAutoCreateRowSorter(true);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, String labelText, JComponent component, int yPos) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 0;
        gbc.gridy = yPos;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.2;
        gbc.insets = new Insets(5, 5, 5, 10);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.gridy = yPos;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.8;
        gbc.insets = new Insets(5, 0, 5, 5);
        panel.add(component, gbc);
    }

    private void styleButtonForVerticalLayout(JButton button, int preferredWidth) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        Dimension buttonSize = new Dimension(preferredWidth, 35);
        button.setPreferredSize(buttonSize);
        button.setMinimumSize(buttonSize);
        button.setMaximumSize(new Dimension(preferredWidth, 40));
    }

    private void showAlert(String title, String message, int messageType) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, message, title, messageType));
    }

    private BigDecimal parseBigDecimal(String text, String fieldName) throws NumberFormatException {
        if (text == null || text.trim().isEmpty()) {
            throw new NumberFormatException(fieldName + " không được để trống.");
        }
        try {
            return new BigDecimal(text.trim().replace(",", "")); // Loại bỏ dấu phẩy nếu có
        } catch (NumberFormatException e) {
            throw new NumberFormatException(fieldName + " phải là một số hợp lệ (ví dụ: 123456.78).");
        }
    }

    private int parseInt(String text, String fieldName) throws NumberFormatException {
        if (text == null || text.trim().isEmpty()) {
            throw new NumberFormatException(fieldName + " không được để trống.");
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new NumberFormatException(fieldName + " phải là một số nguyên hợp lệ.");
        }
    }

    private LocalDate getLocalDateFromPicker(JXDatePicker picker) {
        Date date = picker.getDate();
        return (date != null) ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
    }

    private void setLocalDateToPicker(JXDatePicker picker, LocalDate localDate) {
        picker.setDate(localDate != null ? Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null);
    }

    private LocalDateTime getLocalDateTimeFromPicker(JXDatePicker picker) {
        Date date = picker.getDate();
        return (date != null) ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().withHour(0).withMinute(0).withSecond(0).withNano(0) : null;
    }

    private void setLocalDateTimeToPicker(JXDatePicker picker, LocalDateTime localDateTime) {
        picker.setDate(localDateTime != null ? Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()) : null);
    }

    // --- TAB MANAGE PRODUCTS ---
    private void createManageProductsTab() {
        JPanel productsPanel = new JPanel(new BorderLayout(10, 10));
        productsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        productTableModel = new ProductTableModel();
        productTable = new JTable(productTableModel);
        styleTable(productTable);

        DefaultTableCellRenderer leftRendererProd = new DefaultTableCellRenderer(); leftRendererProd.setHorizontalAlignment(JLabel.LEFT);
        DefaultTableCellRenderer centerRendererProd = new DefaultTableCellRenderer(); centerRendererProd.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer rightRendererProd = new DefaultTableCellRenderer(); rightRendererProd.setHorizontalAlignment(JLabel.RIGHT);
        productTable.getColumnModel().getColumn(0).setCellRenderer(centerRendererProd); // ID
        productTable.getColumnModel().getColumn(1).setCellRenderer(leftRendererProd);  // Name
        productTable.getColumnModel().getColumn(2).setCellRenderer(leftRendererProd);  // Model
        productTable.getColumnModel().getColumn(3).setCellRenderer(leftRendererProd);  // Brand
        productTable.getColumnModel().getColumn(4).setCellRenderer(centerRendererProd); // Type
        productTable.getColumnModel().getColumn(5).setCellRenderer(leftRendererProd);  // Category Name
        productTable.getColumnModel().getColumn(6).setCellRenderer(rightRendererProd); // Price
        productTable.getColumnModel().getColumn(7).setCellRenderer(centerRendererProd); // Stock
        productTable.getColumnModel().getColumn(8).setCellRenderer(centerRendererProd); // Published Year

        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && productTable.getSelectedRow() != -1) {
                int modelRow = productTable.convertRowIndexToModel(productTable.getSelectedRow());
                populateProductForm(productTableModel.getProductAt(modelRow));
            }
        });
        JScrollPane productTableScrollPane = new JScrollPane(productTable);

        JPanel productFormPanel = new JPanel(new GridBagLayout());
        productFormPanel.setBorder(BorderFactory.createTitledBorder("Product Details"));
        GridBagConstraints gbcForm = new GridBagConstraints();
        gbcForm.insets = new Insets(5, 5, 5, 5);

        productIdFieldProd = new JTextField(5);
        productIdFieldProd.setEditable(false);
        productNameFieldProd = new JTextField(25);
        modelFieldProd = new JTextField(25);
        brandFieldProd = new JTextField(25);
        descriptionAreaProd = new JTextArea(3, 25);
        descriptionAreaProd.setLineWrap(true);
        descriptionAreaProd.setWrapStyleWord(true);
        JScrollPane descriptionScrollPane = new JScrollPane(descriptionAreaProd);
        priceFieldProd = new JTextField(15);
        stockFieldProd = new JTextField(10);
        productTypeComboProd = new JComboBox<>(new String[]{"Laptop", "Gear", "Components", "Accessory", "Other"});
        categoryComboProd = new JComboBox<>();
        publishDatePickerProd = new JXDatePicker();
        publishDatePickerProd.setFormats(jxDatePickerFormatter); // Sử dụng SimpleDateFormat
        publishDatePickerProd.setPreferredSize(new Dimension(140, publishDatePickerProd.getPreferredSize().height));

        int y = 0;
        addFormField(productFormPanel, gbcForm, "Product ID:", productIdFieldProd, y++);
        addFormField(productFormPanel, gbcForm, "Product Name:", productNameFieldProd, y++);
        addFormField(productFormPanel, gbcForm, "Model:", modelFieldProd, y++);
        addFormField(productFormPanel, gbcForm, "Brand:", brandFieldProd, y++);
        addFormField(productFormPanel, gbcForm, "Description:", descriptionScrollPane, y++);
        addFormField(productFormPanel, gbcForm, "Price (VNĐ):", priceFieldProd, y++);
        addFormField(productFormPanel, gbcForm, "Stock Quantity:", stockFieldProd, y++);
        addFormField(productFormPanel, gbcForm, "Product Type:", productTypeComboProd, y++);
        addFormField(productFormPanel, gbcForm, "Category:", categoryComboProd, y++);
        addFormField(productFormPanel, gbcForm, "Publish Date:", publishDatePickerProd, y++);

        JPanel buttonPanelVertical = new JPanel();
        buttonPanelVertical.setLayout(new BoxLayout(buttonPanelVertical, BoxLayout.Y_AXIS));
        JButton addButton = new JButton("Add New Product");
        JButton updateButton = new JButton("Update Selected");
        JButton deleteButton = new JButton("Delete Selected");
        JButton clearButton = new JButton("Clear Form");

        int buttonPreferredWidth = 180;
        styleButtonForVerticalLayout(addButton, buttonPreferredWidth);
        styleButtonForVerticalLayout(updateButton, buttonPreferredWidth);
        styleButtonForVerticalLayout(deleteButton, buttonPreferredWidth);
        styleButtonForVerticalLayout(clearButton, buttonPreferredWidth);

        buttonPanelVertical.add(addButton);
        buttonPanelVertical.add(Box.createRigidArea(new Dimension(0, 8)));
        buttonPanelVertical.add(updateButton);
        buttonPanelVertical.add(Box.createRigidArea(new Dimension(0, 8)));
        buttonPanelVertical.add(deleteButton);
        buttonPanelVertical.add(Box.createRigidArea(new Dimension(0, 8)));
        buttonPanelVertical.add(clearButton);

        gbcForm.gridx = 0; gbcForm.gridy = y; gbcForm.gridwidth = 2;
        gbcForm.anchor = GridBagConstraints.NORTHEAST;
        gbcForm.fill = GridBagConstraints.NONE;
        gbcForm.weighty = 0;
        gbcForm.insets = new Insets(15, 5, 5, 5);
        productFormPanel.add(buttonPanelVertical, gbcForm);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, productTableScrollPane, productFormPanel);
        splitPane.setResizeWeight(0.70);
        productsPanel.add(splitPane, BorderLayout.CENTER);

        addButton.addActionListener(e -> addProductAction());
        updateButton.addActionListener(e -> updateProductAction());
        deleteButton.addActionListener(e -> deleteProductAction());
        clearButton.addActionListener(e -> resetProductForm());

        tabPane.addTab("Manage Products", productsPanel);
    }

    private void loadProductsData() {
        try {
            List<Product> products = productDb.getAllProducts();
            productTableModel.setProducts(products);
        } catch (SQLException e) {
            showAlert("Database Error", "Could not load products: " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void loadCategoriesForProductForm() {
        try {
            List<Category> categories = categoryDb.getAllCategories();
            categoryComboProd.removeAllItems();
            categoryComboProd.addItem(null);
            for (Category cat : categories) {
                categoryComboProd.addItem(cat);
            }
            categoryComboProd.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Category) {
                        setText(((Category) value).getCategoryName());
                    } else {
                        setText("Select Category...");
                    }
                    return this;
                }
            });
            if (categoryComboProd.getItemCount() > 0) {
                categoryComboProd.setSelectedIndex(0);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Could not load categories: " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void populateProductForm(Product product) {
        if (product != null) {
            productIdFieldProd.setText(String.valueOf(product.getProductId()));
            productNameFieldProd.setText(product.getSpecificProductName());
            modelFieldProd.setText(product.getModel());
            brandFieldProd.setText(product.getBrand());
            descriptionAreaProd.setText(product.getDescription());
            priceFieldProd.setText(product.getPrice() != null ? product.getPrice().toPlainString() : "");
            stockFieldProd.setText(String.valueOf(product.getStockQuantity()));
            productTypeComboProd.setSelectedItem(product.getProductType());
            setLocalDateTimeToPicker(publishDatePickerProd, product.getYearPublish());

            if (product.getCategoryId() != null) {
                boolean categoryFound = false;
                for (int i = 0; i < categoryComboProd.getItemCount(); i++) {
                    Category catItem = categoryComboProd.getItemAt(i);
                    if (catItem != null && catItem.getCategoryId() == product.getCategoryId()) {
                        categoryComboProd.setSelectedItem(catItem);
                        categoryFound = true;
                        break;
                    }
                }
                if (!categoryFound && categoryComboProd.getItemCount() > 0) categoryComboProd.setSelectedIndex(0);
            } else {
                if (categoryComboProd.getItemCount() > 0) categoryComboProd.setSelectedIndex(0);
            }
        } else {
            resetProductForm();
        }
    }

    private void addProductAction() {
        try {
            String specificName = productNameFieldProd.getText().trim();
            String model = modelFieldProd.getText().trim();
            String brand = brandFieldProd.getText().trim();
            String description = descriptionAreaProd.getText();
            String productType = (String) productTypeComboProd.getSelectedItem();
            Category selectedCategory = (Category) categoryComboProd.getSelectedItem();

            if (specificName.isEmpty() || model.isEmpty() || brand.isEmpty() || productType == null || productType.isEmpty()) {
                showAlert("Input Error", "Product Name, Model, Brand, and Type are required.", JOptionPane.ERROR_MESSAGE); return;
            }

            BigDecimal price = parseBigDecimal(priceFieldProd.getText(), "Price");
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                showAlert("Input Error", "Price must be non-negative.", JOptionPane.ERROR_MESSAGE); return;
            }
            int stock = parseInt(stockFieldProd.getText(), "Stock Quantity");
            if (stock < 0) {
                showAlert("Input Error", "Stock quantity must be non-negative.", JOptionPane.ERROR_MESSAGE); return;
            }

            LocalDateTime publishDateTime = getLocalDateTimeFromPicker(publishDatePickerProd);
            Integer categoryId = (selectedCategory != null) ? selectedCategory.getCategoryId() : null;

            Product newProduct = new Product(0, specificName, model, brand, description, price, stock, publishDateTime, productType, categoryId);
            if (selectedCategory != null) newProduct.setCategoryName(selectedCategory.getCategoryName()); // Gán tên category cho model

            Product addedProduct = productDb.addProduct(newProduct);
            if (addedProduct != null) {
                loadProductsData();
                resetProductForm();
                showAlert("Success", "Product added successfully with ID: " + addedProduct.getProductId(), JOptionPane.INFORMATION_MESSAGE);
            } else {
                showAlert("Error", "Failed to add product to database.", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            showAlert("Input Error", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            showAlert("Input Error", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            showAlert("Database Error", "Error adding product: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void updateProductAction() {
        int selectedViewRow = productTable.getSelectedRow();
        if (selectedViewRow == -1) {
            showAlert("Selection Error", "Please select a product to update.", JOptionPane.WARNING_MESSAGE); return;
        }
        int modelRow = productTable.convertRowIndexToModel(selectedViewRow);
        Product productToUpdate = productTableModel.getProductAt(modelRow);
        if (productToUpdate == null) return;

        try {
            String specificName = productNameFieldProd.getText().trim();
            String model = modelFieldProd.getText().trim();
            String brand = brandFieldProd.getText().trim();
            String description = descriptionAreaProd.getText();
            String productType = (String) productTypeComboProd.getSelectedItem();
            Category selectedCategory = (Category) categoryComboProd.getSelectedItem();

            if (specificName.isEmpty() || model.isEmpty() || brand.isEmpty() || productType == null || productType.isEmpty()) {
                showAlert("Input Error", "Product Name, Model, Brand, and Type are required.", JOptionPane.ERROR_MESSAGE); return;
            }
            BigDecimal price = parseBigDecimal(priceFieldProd.getText(), "Price");
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                showAlert("Input Error", "Price must be non-negative.", JOptionPane.ERROR_MESSAGE); return;
            }
            int stock = parseInt(stockFieldProd.getText(), "Stock Quantity");
            if (stock < 0) {
                showAlert("Input Error", "Stock quantity must be non-negative.", JOptionPane.ERROR_MESSAGE); return;
            }

            LocalDateTime publishDateTime = getLocalDateTimeFromPicker(publishDatePickerProd);
            Integer categoryId = (selectedCategory != null) ? selectedCategory.getCategoryId() : null;

            productToUpdate.setSpecificProductName(specificName);
            productToUpdate.setModel(model);
            productToUpdate.setBrand(brand);
            productToUpdate.setDescription(description);
            productToUpdate.setPrice(price);
            productToUpdate.setStockQuantity(stock);
            productToUpdate.setYearPublish(publishDateTime);
            productToUpdate.setProductType(productType);
            productToUpdate.setCategoryId(categoryId);
            productToUpdate.setCategoryName((selectedCategory != null) ? selectedCategory.getCategoryName() : null);

            boolean success = productDb.updateProduct(productToUpdate);
            if (success) {
                loadProductsData();
                resetProductForm();
                showAlert("Success", "Product updated successfully.", JOptionPane.INFORMATION_MESSAGE);
            } else {
                showAlert("Error", "Failed to update product (no changes or product not found).", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            showAlert("Input Error", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            showAlert("Database Error", "Error updating product: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void deleteProductAction() {
        int selectedViewRow = productTable.getSelectedRow();
        if (selectedViewRow == -1) {
            showAlert("Selection Error", "Please select a product to delete.", JOptionPane.WARNING_MESSAGE); return;
        }
        int modelRow = productTable.convertRowIndexToModel(selectedViewRow);
        Product productToDelete = productTableModel.getProductAt(modelRow);
        if (productToDelete == null) return;

        int confirmation = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to delete product: " + productToDelete.getSpecificProductName() + " (ID: " + productToDelete.getProductId() + ")?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                boolean success = productDb.deleteProduct(productToDelete.getProductId());
                if (success) {
                    loadProductsData();
                    resetProductForm();
                    showAlert("Success", "Product deleted successfully.", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    showAlert("Error", "Failed to delete product (it might have already been deleted or does not exist).", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                if ("23503".equals(ex.getSQLState()) || (ex.getMessage() != null && ex.getMessage().contains("constraint"))) { // PostgreSQL FK violation
                    showAlert("Deletion Error", "Cannot delete product: It is referenced in existing orders.", JOptionPane.ERROR_MESSAGE);
                } else {
                    showAlert("Database Error", "Error deleting product: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                }
                ex.printStackTrace();
            }
        }
    }

    private void resetProductForm() {
        productIdFieldProd.setText("");
        productNameFieldProd.setText("");
        modelFieldProd.setText("");
        brandFieldProd.setText("");
        descriptionAreaProd.setText("");
        priceFieldProd.setText("");
        stockFieldProd.setText("");
        if (productTypeComboProd.getItemCount() > 0) productTypeComboProd.setSelectedIndex(0);
        if (categoryComboProd.getItemCount() > 0) categoryComboProd.setSelectedIndex(0);
        publishDatePickerProd.setDate(null);
        productTable.clearSelection();
    }
    // --- KẾT THÚC TAB PRODUCTS ---

    // --- TAB MANAGE CUSTOMERS ---
    private void createManageCustomersTab() {
        JPanel customersPanel = new JPanel(new BorderLayout(10, 10));
        customersPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        customerTableModel = new CustomerTableModel();
        customerTable = new JTable(customerTableModel);
        styleTable(customerTable);
        // Căn lề cho Customer Table
        DefaultTableCellRenderer leftRendererCust = new DefaultTableCellRenderer(); leftRendererCust.setHorizontalAlignment(JLabel.LEFT);
        DefaultTableCellRenderer centerRendererCust = new DefaultTableCellRenderer(); centerRendererCust.setHorizontalAlignment(JLabel.CENTER);
        customerTable.getColumnModel().getColumn(0).setCellRenderer(centerRendererCust); // ID
        customerTable.getColumnModel().getColumn(5).setCellRenderer(centerRendererCust); // Gender
        for(int i=1; i<customerTable.getColumnCount(); i++){
            if(i != 5 && i != 0) customerTable.getColumnModel().getColumn(i).setCellRenderer(leftRendererCust);
        }


        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && customerTable.getSelectedRow() != -1) {
                int modelRow = customerTable.convertRowIndexToModel(customerTable.getSelectedRow());
                populateCustomerForm(customerTableModel.getCustomerAt(modelRow));
            }
        });
        JScrollPane customerTableScrollPane = new JScrollPane(customerTable);

        JPanel customerFormPanel = new JPanel(new GridBagLayout());
        customerFormPanel.setBorder(BorderFactory.createTitledBorder("Customer Details"));
        GridBagConstraints gbcForm = new GridBagConstraints();
        gbcForm.insets = new Insets(5, 5, 5, 5);

        custIdField = new JTextField(5);
        custIdField.setEditable(false);
        custUsernameField = new JTextField(20);
        custEmailField = new JTextField(20);
        custFirstNameField = new JTextField(20);
        custLastNameField = new JTextField(20);
        custGenderCombo = new JComboBox<>(new String[]{"Select...", "M", "F", "O"}); // O for Other
        custAddressField = new JTextField(20);
        custDateOfBirthPicker = new JXDatePicker();
        custDateOfBirthPicker.setFormats(jxDatePickerFormatter);
        custDateOfBirthPicker.setPreferredSize(new Dimension(140, custDateOfBirthPicker.getPreferredSize().height));
        custPhoneField = new JTextField(15);

        int y = 0;
        addFormField(customerFormPanel, gbcForm, "Customer ID:", custIdField, y++);
        addFormField(customerFormPanel, gbcForm, "Username:", custUsernameField, y++);
        addFormField(customerFormPanel, gbcForm, "Email:", custEmailField, y++);
        addFormField(customerFormPanel, gbcForm, "First Name:", custFirstNameField, y++);
        addFormField(customerFormPanel, gbcForm, "Last Name:", custLastNameField, y++);
        addFormField(customerFormPanel, gbcForm, "Gender:", custGenderCombo, y++);
        addFormField(customerFormPanel, gbcForm, "Address:", custAddressField, y++);
        addFormField(customerFormPanel, gbcForm, "Date of Birth:", custDateOfBirthPicker, y++);
        addFormField(customerFormPanel, gbcForm, "Phone:", custPhoneField, y++);

        JPanel buttonPanelVertical = new JPanel();
        buttonPanelVertical.setLayout(new BoxLayout(buttonPanelVertical, BoxLayout.Y_AXIS));
        JButton addButton = new JButton("Add New Customer");
        JButton updateButton = new JButton("Update Selected");
        JButton deleteButton = new JButton("Delete Selected");
        JButton clearButton = new JButton("Clear Form");

        int buttonPreferredWidth = 180;
        styleButtonForVerticalLayout(addButton, buttonPreferredWidth);
        styleButtonForVerticalLayout(updateButton, buttonPreferredWidth);
        styleButtonForVerticalLayout(deleteButton, buttonPreferredWidth);
        styleButtonForVerticalLayout(clearButton, buttonPreferredWidth);

        buttonPanelVertical.add(addButton);
        buttonPanelVertical.add(Box.createRigidArea(new Dimension(0, 8)));
        buttonPanelVertical.add(updateButton);
        buttonPanelVertical.add(Box.createRigidArea(new Dimension(0, 8)));
        buttonPanelVertical.add(deleteButton);
        buttonPanelVertical.add(Box.createRigidArea(new Dimension(0, 8)));
        buttonPanelVertical.add(clearButton);

        gbcForm.gridx = 0; gbcForm.gridy = y; gbcForm.gridwidth = 2;
        gbcForm.anchor = GridBagConstraints.NORTHEAST; gbcForm.fill = GridBagConstraints.NONE;
        gbcForm.weighty = 0; gbcForm.insets = new Insets(15, 5, 5, 5);
        customerFormPanel.add(buttonPanelVertical, gbcForm);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, customerTableScrollPane, customerFormPanel);
        splitPane.setResizeWeight(0.70);
        customersPanel.add(splitPane, BorderLayout.CENTER);

        addButton.addActionListener(e -> addCustomerAction());
        updateButton.addActionListener(e -> updateCustomerAction());
        deleteButton.addActionListener(e -> deleteCustomerAction());
        clearButton.addActionListener(e -> resetCustomerForm());

        tabPane.addTab("Manage Customers", customersPanel);
    }

    private void loadCustomersData() {
        try {
            List<Customer> customers = customerDb.getAllCustomers();
            customerTableModel.setCustomers(customers);
        } catch (SQLException e) {
            showAlert("Database Error", "Could not load customers: " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void populateCustomerForm(Customer customer) {
        if (customer != null) {
            custIdField.setText(String.valueOf(customer.getCustomerId()));
            custUsernameField.setText(customer.getUsername());
            custEmailField.setText(customer.getEmail());
            custFirstNameField.setText(customer.getFirstName());
            custLastNameField.setText(customer.getLastName());
            custGenderCombo.setSelectedItem(String.valueOf(customer.getGender()));
            custAddressField.setText(customer.getAddress());
            setLocalDateToPicker(custDateOfBirthPicker, customer.getDateOfBirth());
            custPhoneField.setText(customer.getPhone());
        } else {
            resetCustomerForm();
        }
    }

    private void addCustomerAction() {
        try {
            String username = custUsernameField.getText().trim();
            String email = custEmailField.getText().trim();
            String firstName = custFirstNameField.getText().trim();
            String lastName = custLastNameField.getText().trim();
            String genderStr = (String) custGenderCombo.getSelectedItem();
            String address = custAddressField.getText().trim();
            LocalDate dob = getLocalDateFromPicker(custDateOfBirthPicker);
            String phone = custPhoneField.getText().trim();

            if (username.isEmpty() || email.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
                showAlert("Input Error", "Username, Email, First Name, and Last Name are required.", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Validate email format (simple check)
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                showAlert("Input Error", "Invalid email format.", JOptionPane.ERROR_MESSAGE); return;
            }

            char gender = (genderStr != null && !genderStr.equals("Select...") && !genderStr.isEmpty()) ? genderStr.charAt(0) : '\0'; // '\0' for unspecified

            Customer newCustomer = new Customer(0, username, email, firstName, lastName, LocalDateTime.now(), gender, address, dob, phone);

            Customer addedCustomer = customerDb.addCustomer(newCustomer);
            if (addedCustomer != null) {
                loadCustomersData();
                resetCustomerForm();
                showAlert("Success", "Customer added successfully with ID: " + addedCustomer.getCustomerId(), JOptionPane.INFORMATION_MESSAGE);
            } else {
                showAlert("Error", "Failed to add customer.", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            showAlert("Input Error", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            showAlert("Database Error", "Error adding customer: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void updateCustomerAction() {
        int selectedViewRow = customerTable.getSelectedRow();
        if (selectedViewRow == -1) {
            showAlert("Selection Error", "Please select a customer to update.", JOptionPane.WARNING_MESSAGE); return;
        }
        int modelRow = customerTable.convertRowIndexToModel(selectedViewRow);
        Customer customerToUpdate = customerTableModel.getCustomerAt(modelRow);
        if (customerToUpdate == null) return;

        try {
            String username = custUsernameField.getText().trim();
            String email = custEmailField.getText().trim();
            String firstName = custFirstNameField.getText().trim();
            String lastName = custLastNameField.getText().trim();
            String genderStr = (String) custGenderCombo.getSelectedItem();
            String address = custAddressField.getText().trim();
            LocalDate dob = getLocalDateFromPicker(custDateOfBirthPicker);
            String phone = custPhoneField.getText().trim();

            if (username.isEmpty() || email.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
                showAlert("Input Error", "Username, Email, First Name, and Last Name are required.", JOptionPane.ERROR_MESSAGE); return;
            }
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                showAlert("Input Error", "Invalid email format.", JOptionPane.ERROR_MESSAGE); return;
            }

            char gender = (genderStr != null && !genderStr.equals("Select...") && !genderStr.isEmpty()) ? genderStr.charAt(0) : customerToUpdate.getGender();

            customerToUpdate.setUsername(username);
            customerToUpdate.setEmail(email);
            customerToUpdate.setFirstName(firstName);
            customerToUpdate.setLastName(lastName);
            customerToUpdate.setGender(gender);
            customerToUpdate.setAddress(address);
            customerToUpdate.setDateOfBirth(dob);
            customerToUpdate.setPhone(phone);
            // customerToUpdate.setCreatedAt() không nên thay đổi ở đây

            boolean success = customerDb.updateCustomer(customerToUpdate);
            if (success) {
                loadCustomersData();
                resetCustomerForm();
                showAlert("Success", "Customer updated successfully.", JOptionPane.INFORMATION_MESSAGE);
            } else {
                showAlert("Error", "Failed to update customer.", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            showAlert("Input Error", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            showAlert("Database Error", "Error updating customer: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void deleteCustomerAction() {
        int selectedViewRow = customerTable.getSelectedRow();
        if (selectedViewRow == -1) {
            showAlert("Selection Error", "Please select a customer to delete.", JOptionPane.WARNING_MESSAGE); return;
        }
        int modelRow = customerTable.convertRowIndexToModel(selectedViewRow);
        Customer customerToDelete = customerTableModel.getCustomerAt(modelRow);
        if (customerToDelete == null) return;

        int confirmation = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to delete customer: " + customerToDelete.getFirstName() + " " + customerToDelete.getLastName() + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                boolean success = customerDb.deleteCustomer(customerToDelete.getCustomerId());
                if (success) {
                    loadCustomersData();
                    resetCustomerForm();
                    showAlert("Success", "Customer deleted successfully.", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    showAlert("Error", "Failed to delete customer.", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                if ("23503".equals(ex.getSQLState()) || (ex.getMessage() != null && ex.getMessage().contains("constraint"))) {
                    showAlert("Deletion Error", "Cannot delete customer: They have existing orders.", JOptionPane.ERROR_MESSAGE);
                } else {
                    showAlert("Database Error", "Error deleting customer: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                }
                ex.printStackTrace();
            }
        }
    }

    private void resetCustomerForm() {
        custIdField.setText("");
        custUsernameField.setText("");
        custEmailField.setText("");
        custFirstNameField.setText("");
        custLastNameField.setText("");
        if (custGenderCombo.getItemCount() > 0) custGenderCombo.setSelectedIndex(0);
        custAddressField.setText("");
        custDateOfBirthPicker.setDate(null);
        custPhoneField.setText("");
        customerTable.clearSelection();
    }
    // --- KẾT THÚC TAB CUSTOMERS ---

    // --- TAB MANAGE EMPLOYEES ---
    private void createManageEmployeesTab() {
        JPanel employeesPanel = new JPanel(new BorderLayout(10, 10));
        employeesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        employeeTableModel = new EmployeeTableModel();
        employeeTable = new JTable(employeeTableModel);
        styleTable(employeeTable);
        // Căn lề cho Employee Table
        DefaultTableCellRenderer leftRendererEmp = new DefaultTableCellRenderer(); leftRendererEmp.setHorizontalAlignment(JLabel.LEFT);
        DefaultTableCellRenderer centerRendererEmp = new DefaultTableCellRenderer(); centerRendererEmp.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer rightRendererEmp = new DefaultTableCellRenderer(); rightRendererEmp.setHorizontalAlignment(JLabel.RIGHT);
        employeeTable.getColumnModel().getColumn(0).setCellRenderer(centerRendererEmp); // ID
        employeeTable.getColumnModel().getColumn(5).setCellRenderer(centerRendererEmp); // Gender
        employeeTable.getColumnModel().getColumn(8).setCellRenderer(rightRendererEmp); // Salary
        employeeTable.getColumnModel().getColumn(10).setCellRenderer(centerRendererEmp); // Hire Date

        employeeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        employeeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && employeeTable.getSelectedRow() != -1) {
                int modelRow = employeeTable.convertRowIndexToModel(employeeTable.getSelectedRow());
                populateEmployeeForm(employeeTableModel.getEmployeeAt(modelRow));
            }
        });
        JScrollPane employeeTableScrollPane = new JScrollPane(employeeTable);

        JPanel employeeFormPanel = new JPanel(new GridBagLayout());
        employeeFormPanel.setBorder(BorderFactory.createTitledBorder("Employee Details"));
        GridBagConstraints gbcForm = new GridBagConstraints();
        gbcForm.insets = new Insets(5, 5, 5, 5);

        empIdField = new JTextField(5);
        empIdField.setEditable(false);
        empFirstNameField = new JTextField(20);
        empLastNameField = new JTextField(20);
        empPhoneField = new JTextField(15);
        empEmailField = new JTextField(20); // Thêm email field
        empAddressField = new JTextField(20);
        empGenderCombo = new JComboBox<>(new String[]{"Select...", "M", "F", "O"});
        empBankNumberField = new JTextField(15);
        empRoleField = new JTextField(15);
        empSalaryField = new JTextField(15);
        empWorkDayField = new JTextField(20);
        empHireDatePicker = new JXDatePicker();
        empHireDatePicker.setFormats(jxDatePickerFormatter);
        empHireDatePicker.setPreferredSize(new Dimension(140, empHireDatePicker.getPreferredSize().height));

        int y = 0;
        addFormField(employeeFormPanel, gbcForm, "Employee ID:", empIdField, y++);
        addFormField(employeeFormPanel, gbcForm, "First Name:", empFirstNameField, y++);
        addFormField(employeeFormPanel, gbcForm, "Last Name:", empLastNameField, y++);
        addFormField(employeeFormPanel, gbcForm, "Phone:", empPhoneField, y++);
        addFormField(employeeFormPanel, gbcForm, "Email:", empEmailField, y++);
        addFormField(employeeFormPanel, gbcForm, "Address:", empAddressField, y++);
        addFormField(employeeFormPanel, gbcForm, "Gender:", empGenderCombo, y++);
        addFormField(employeeFormPanel, gbcForm, "Bank Number:", empBankNumberField, y++);
        addFormField(employeeFormPanel, gbcForm, "Role:", empRoleField, y++);
        addFormField(employeeFormPanel, gbcForm, "Salary (VNĐ):", empSalaryField, y++);
        addFormField(employeeFormPanel, gbcForm, "Work Days:", empWorkDayField, y++);
        addFormField(employeeFormPanel, gbcForm, "Hire Date:", empHireDatePicker, y++);

        JPanel buttonPanelVertical = new JPanel();
        buttonPanelVertical.setLayout(new BoxLayout(buttonPanelVertical, BoxLayout.Y_AXIS));
        JButton addButton = new JButton("Add New Employee");
        JButton updateButton = new JButton("Update Selected");
        JButton deleteButton = new JButton("Delete Selected");
        JButton clearButton = new JButton("Clear Form");

        int buttonPreferredWidth = 180;
        styleButtonForVerticalLayout(addButton, buttonPreferredWidth);
        styleButtonForVerticalLayout(updateButton, buttonPreferredWidth);
        styleButtonForVerticalLayout(deleteButton, buttonPreferredWidth);
        styleButtonForVerticalLayout(clearButton, buttonPreferredWidth);

        buttonPanelVertical.add(addButton);
        buttonPanelVertical.add(Box.createRigidArea(new Dimension(0, 8)));
        buttonPanelVertical.add(updateButton);
        buttonPanelVertical.add(Box.createRigidArea(new Dimension(0, 8)));
        buttonPanelVertical.add(deleteButton);
        buttonPanelVertical.add(Box.createRigidArea(new Dimension(0, 8)));
        buttonPanelVertical.add(clearButton);

        gbcForm.gridx = 0; gbcForm.gridy = y; gbcForm.gridwidth = 2;
        gbcForm.anchor = GridBagConstraints.NORTHEAST; gbcForm.fill = GridBagConstraints.NONE;
        gbcForm.weighty = 0; gbcForm.insets = new Insets(15, 5, 5, 5);
        employeeFormPanel.add(buttonPanelVertical, gbcForm);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, employeeTableScrollPane, employeeFormPanel);
        splitPane.setResizeWeight(0.70);
        employeesPanel.add(splitPane, BorderLayout.CENTER);

        addButton.addActionListener(e -> addEmployeeAction());
        updateButton.addActionListener(e -> updateEmployeeAction());
        deleteButton.addActionListener(e -> deleteEmployeeAction());
        clearButton.addActionListener(e -> resetEmployeeForm());

        tabPane.addTab("Manage Employees", employeesPanel);
    }

    private void loadEmployeesData() {
        try {
            List<Employee> employees = employeeDb.getAllEmployees();
            employeeTableModel.setEmployees(employees);
        } catch (SQLException e) {
            showAlert("Database Error", "Could not load employees: " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void populateEmployeeForm(Employee employee) {
        if (employee != null) {
            empIdField.setText(String.valueOf(employee.getEmployeeId()));
            empFirstNameField.setText(employee.getFirstName());
            empLastNameField.setText(employee.getLastName());
            empPhoneField.setText(employee.getPhone());
            empEmailField.setText(employee.getEmail());
            empAddressField.setText(employee.getAddress());
            empGenderCombo.setSelectedItem(String.valueOf(employee.getGender()));
            empBankNumberField.setText(employee.getBankNumber());
            empRoleField.setText(employee.getRole());
            empSalaryField.setText(employee.getSalary() != null ? employee.getSalary().toPlainString() : "");
            empWorkDayField.setText(employee.getWorkDay());
            setLocalDateToPicker(empHireDatePicker, employee.getHireDay());
        } else {
            resetEmployeeForm();
        }
    }

    private void addEmployeeAction() {
        try {
            String firstName = empFirstNameField.getText().trim();
            String lastName = empLastNameField.getText().trim();
            String phone = empPhoneField.getText().trim();
            String email = empEmailField.getText().trim();
            String address = empAddressField.getText().trim();
            String genderStr = (String) empGenderCombo.getSelectedItem();
            String bankNumber = empBankNumberField.getText().trim();
            String role = empRoleField.getText().trim();
            String workDay = empWorkDayField.getText().trim();
            LocalDate hireDate = getLocalDateFromPicker(empHireDatePicker);

            if (firstName.isEmpty() || lastName.isEmpty() || role.isEmpty() || email.isEmpty() || hireDate == null) {
                showAlert("Input Error", "First Name, Last Name, Role, Email, and Hire Date are required.", JOptionPane.ERROR_MESSAGE); return;
            }
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                showAlert("Input Error", "Invalid email format.", JOptionPane.ERROR_MESSAGE); return;
            }

            BigDecimal salary = parseBigDecimal(empSalaryField.getText(), "Salary");
            if (salary.compareTo(BigDecimal.ZERO) < 0) {
                showAlert("Input Error", "Salary must be non-negative.", JOptionPane.ERROR_MESSAGE); return;
            }
            char gender = (genderStr != null && !genderStr.equals("Select...") && !genderStr.isEmpty()) ? genderStr.charAt(0) : '\0';


            Employee newEmployee = new Employee(0, firstName, lastName, phone, address, gender, bankNumber, role, salary, workDay, hireDate, email);

            Employee addedEmployee = employeeDb.addEmployee(newEmployee);
            if (addedEmployee != null) {
                loadEmployeesData();
                resetEmployeeForm();
                showAlert("Success", "Employee added successfully with ID: " + addedEmployee.getEmployeeId(), JOptionPane.INFORMATION_MESSAGE);
            } else {
                showAlert("Error", "Failed to add employee.", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            showAlert("Input Error", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            showAlert("Database Error", "Error adding employee: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void updateEmployeeAction() {
        int selectedViewRow = employeeTable.getSelectedRow();
        if (selectedViewRow == -1) {
            showAlert("Selection Error", "Please select an employee to update.", JOptionPane.WARNING_MESSAGE); return;
        }
        int modelRow = employeeTable.convertRowIndexToModel(selectedViewRow);
        Employee employeeToUpdate = employeeTableModel.getEmployeeAt(modelRow);
        if (employeeToUpdate == null) return;

        try {
            String firstName = empFirstNameField.getText().trim();
            String lastName = empLastNameField.getText().trim();
            String phone = empPhoneField.getText().trim();
            String email = empEmailField.getText().trim();
            String address = empAddressField.getText().trim();
            String genderStr = (String) empGenderCombo.getSelectedItem();
            String bankNumber = empBankNumberField.getText().trim();
            String role = empRoleField.getText().trim();
            String workDay = empWorkDayField.getText().trim();
            LocalDate hireDate = getLocalDateFromPicker(empHireDatePicker);

            if (firstName.isEmpty() || lastName.isEmpty() || role.isEmpty() || email.isEmpty() || hireDate == null) {
                showAlert("Input Error", "First Name, Last Name, Role, Email, and Hire Date are required.", JOptionPane.ERROR_MESSAGE); return;
            }
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                showAlert("Input Error", "Invalid email format.", JOptionPane.ERROR_MESSAGE); return;
            }
            BigDecimal salary = parseBigDecimal(empSalaryField.getText(), "Salary");
            if (salary.compareTo(BigDecimal.ZERO) < 0) {
                showAlert("Input Error", "Salary must be non-negative.", JOptionPane.ERROR_MESSAGE); return;
            }
            char gender = (genderStr != null && !genderStr.equals("Select...") && !genderStr.isEmpty()) ? genderStr.charAt(0) : employeeToUpdate.getGender();

            employeeToUpdate.setFirstName(firstName);
            employeeToUpdate.setLastName(lastName);
            employeeToUpdate.setPhone(phone);
            employeeToUpdate.setEmail(email);
            employeeToUpdate.setAddress(address);
            employeeToUpdate.setGender(gender);
            employeeToUpdate.setBankNumber(bankNumber);
            employeeToUpdate.setRole(role);
            employeeToUpdate.setSalary(salary);
            employeeToUpdate.setWorkDay(workDay);
            employeeToUpdate.setHireDay(hireDate);

            boolean success = employeeDb.updateEmployee(employeeToUpdate);
            if (success) {
                loadEmployeesData();
                resetEmployeeForm();
                showAlert("Success", "Employee updated successfully.", JOptionPane.INFORMATION_MESSAGE);
            } else {
                showAlert("Error", "Failed to update employee.", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            showAlert("Input Error", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            showAlert("Database Error", "Error updating employee: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void deleteEmployeeAction() {
        int selectedViewRow = employeeTable.getSelectedRow();
        if (selectedViewRow == -1) {
            showAlert("Selection Error", "Please select an employee to delete.", JOptionPane.WARNING_MESSAGE); return;
        }
        int modelRow = employeeTable.convertRowIndexToModel(selectedViewRow);
        Employee employeeToDelete = employeeTableModel.getEmployeeAt(modelRow);
        if (employeeToDelete == null) return;

        int confirmation = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to delete employee: " + employeeToDelete.getFirstName() + " " + employeeToDelete.getLastName() + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                boolean success = employeeDb.deleteEmployee(employeeToDelete.getEmployeeId());
                if (success) {
                    loadEmployeesData();
                    resetEmployeeForm();
                    showAlert("Success", "Employee deleted successfully.", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    showAlert("Error", "Failed to delete employee.", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                if ("23503".equals(ex.getSQLState()) || (ex.getMessage() != null && ex.getMessage().contains("constraint"))) {
                    showAlert("Deletion Error", "Cannot delete employee: They are referenced in existing payments or other records.", JOptionPane.ERROR_MESSAGE);
                } else {
                    showAlert("Database Error", "Error deleting employee: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                }
                ex.printStackTrace();
            }
        }
    }

    private void resetEmployeeForm() {
        empIdField.setText("");
        empFirstNameField.setText("");
        empLastNameField.setText("");
        empPhoneField.setText("");
        empEmailField.setText("");
        empAddressField.setText("");
        if(empGenderCombo.getItemCount() > 0) empGenderCombo.setSelectedIndex(0);
        empBankNumberField.setText("");
        empRoleField.setText("");
        empSalaryField.setText("");
        empWorkDayField.setText("");
        empHireDatePicker.setDate(null);
        employeeTable.clearSelection();
    }
    // --- KẾT THÚC TAB EMPLOYEES ---

    // --- TAB PAYMENT HISTORY ---
    private void createPaymentHistoryTab() {
        JPanel paymentsPanel = new JPanel(new BorderLayout(10, 10));
        paymentsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        paymentTableModel = new PaymentTableModel();
        paymentTable = new JTable(paymentTableModel);
        styleTable(paymentTable);
        // Căn lề cho Payment Table
        DefaultTableCellRenderer centerRendererPay = new DefaultTableCellRenderer(); centerRendererPay.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer rightRendererPay = new DefaultTableCellRenderer(); rightRendererPay.setHorizontalAlignment(JLabel.RIGHT);
        paymentTable.getColumnModel().getColumn(0).setCellRenderer(centerRendererPay); // ID
        paymentTable.getColumnModel().getColumn(2).setCellRenderer(centerRendererPay); // Date
        paymentTable.getColumnModel().getColumn(3).setCellRenderer(rightRendererPay); // Amount
        paymentTable.getColumnModel().getColumn(4).setCellRenderer(centerRendererPay); // Method
        paymentTable.getColumnModel().getColumn(5).setCellRenderer(centerRendererPay); // Status


        paymentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        paymentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && paymentTable.getSelectedRow() != -1) {
                int modelRow = paymentTable.convertRowIndexToModel(paymentTable.getSelectedRow());
                populatePaymentForm(paymentTableModel.getPaymentAt(modelRow));
            }
        });
        JScrollPane paymentTableScrollPane = new JScrollPane(paymentTable);

        JPanel paymentFormPanel = new JPanel(new GridBagLayout());
        paymentFormPanel.setBorder(BorderFactory.createTitledBorder("Payment Details"));
        GridBagConstraints gbcForm = new GridBagConstraints();
        gbcForm.insets = new Insets(5, 5, 5, 5);

        paymentIdField = new JTextField(5);
        paymentIdField.setEditable(false);
        paymentEmployeeCombo = new JComboBox<>(); // Load sau
        paymentAmountField = new JTextField(15);
        paymentDatePicker = new JXDatePicker();
        paymentDatePicker.setFormats(jxDatePickerFormatter);
        paymentDatePicker.setPreferredSize(new Dimension(140, paymentDatePicker.getPreferredSize().height));
        paymentMethodCombo = new JComboBox<>(new String[]{"Cash", "Credit Card", "Bank Transfer", "Momo", "Other"});
        paymentStatusCombo = new JComboBox<>(new String[]{"Pending", "Paid", "Failed", "Refunded", "Cancelled"});
        paymentNotesArea = new JTextArea(2, 20);
        paymentNotesArea.setLineWrap(true);
        paymentNotesArea.setWrapStyleWord(true);
        JScrollPane notesScrollPane = new JScrollPane(paymentNotesArea);


        int y = 0;
        addFormField(paymentFormPanel, gbcForm, "Payment ID:", paymentIdField, y++);
        addFormField(paymentFormPanel, gbcForm, "Employee (Payer):", paymentEmployeeCombo, y++);
        addFormField(paymentFormPanel, gbcForm, "Total Amount (VNĐ):", paymentAmountField, y++);
        addFormField(paymentFormPanel, gbcForm, "Payment Date:", paymentDatePicker, y++);
        addFormField(paymentFormPanel, gbcForm, "Method:", paymentMethodCombo, y++);
        addFormField(paymentFormPanel, gbcForm, "Status:", paymentStatusCombo, y++);
        addFormField(paymentFormPanel, gbcForm, "Notes:", notesScrollPane, y++);


        JPanel buttonPanelVertical = new JPanel();
        buttonPanelVertical.setLayout(new BoxLayout(buttonPanelVertical, BoxLayout.Y_AXIS));
        JButton addButton = new JButton("Add New Payment");
        JButton updateButton = new JButton("Update Selected");
        JButton deleteButton = new JButton("Delete Selected");
        JButton clearButton = new JButton("Clear Form");

        int buttonPreferredWidth = 180;
        styleButtonForVerticalLayout(addButton, buttonPreferredWidth);
        styleButtonForVerticalLayout(updateButton, buttonPreferredWidth);
        styleButtonForVerticalLayout(deleteButton, buttonPreferredWidth);
        styleButtonForVerticalLayout(clearButton, buttonPreferredWidth);

        buttonPanelVertical.add(addButton);
        buttonPanelVertical.add(Box.createRigidArea(new Dimension(0, 8)));
        buttonPanelVertical.add(updateButton);
        buttonPanelVertical.add(Box.createRigidArea(new Dimension(0, 8)));
        buttonPanelVertical.add(deleteButton);
        buttonPanelVertical.add(Box.createRigidArea(new Dimension(0, 8)));
        buttonPanelVertical.add(clearButton);

        gbcForm.gridx = 0; gbcForm.gridy = y; gbcForm.gridwidth = 2;
        gbcForm.anchor = GridBagConstraints.NORTHEAST; gbcForm.fill = GridBagConstraints.NONE;
        gbcForm.weighty = 0; gbcForm.insets = new Insets(15, 5, 5, 5);
        paymentFormPanel.add(buttonPanelVertical, gbcForm);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, paymentTableScrollPane, paymentFormPanel);
        splitPane.setResizeWeight(0.70);
        paymentsPanel.add(splitPane, BorderLayout.CENTER);

        addButton.addActionListener(e -> addPaymentAction());
        updateButton.addActionListener(e -> updatePaymentAction());
        deleteButton.addActionListener(e -> deletePaymentAction());
        clearButton.addActionListener(e -> resetPaymentForm());

        tabPane.addTab("Manage Payments", paymentsPanel); // Đổi tên tab
    }

    private void loadPaymentsData() {
        try {
            List<Payment> payments = paymentDb.getAllPayments();
            paymentTableModel.setPayments(payments);
        } catch (SQLException e) {
            showAlert("Database Error", "Could not load payments: " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void loadEmployeesForPaymentForm() {
        try {
            List<Employee> employees = employeeDb.getAllEmployees();
            paymentEmployeeCombo.removeAllItems();
            paymentEmployeeCombo.addItem(null); // Cho phép không chọn employee (nếu CSDL cho phép employee_id NULL)
            for (Employee emp : employees) {
                paymentEmployeeCombo.addItem(emp);
            }
            paymentEmployeeCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Employee) {
                        setText(((Employee) value).getFirstName() + " " + ((Employee) value).getLastName());
                    } else {
                        setText("Select Employee (Optional)");
                    }
                    return this;
                }
            });
            if(paymentEmployeeCombo.getItemCount() > 0) paymentEmployeeCombo.setSelectedIndex(0);
        } catch (SQLException e) {
            showAlert("Database Error", "Could not load employees for payment form: " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void populatePaymentForm(Payment payment) {
        if (payment != null) {
            paymentIdField.setText(String.valueOf(payment.getPaymentId()));
            paymentAmountField.setText(payment.getTotalAmount() != null ? payment.getTotalAmount().toPlainString() : "");
            setLocalDateTimeToPicker(paymentDatePicker, payment.getPaymentDate());
            paymentMethodCombo.setSelectedItem(payment.getPaymentMethod());
            paymentStatusCombo.setSelectedItem(payment.getStatus());
            paymentNotesArea.setText(payment.getNotes());

            if (payment.getEmployeeId() > 0) {
                boolean employeeFound = false;
                for (int i = 0; i < paymentEmployeeCombo.getItemCount(); i++) {
                    Employee empItem = paymentEmployeeCombo.getItemAt(i);
                    if (empItem != null && empItem.getEmployeeId() == payment.getEmployeeId()) {
                        paymentEmployeeCombo.setSelectedItem(empItem);
                        employeeFound = true;
                        break;
                    }
                }
                if (!employeeFound && paymentEmployeeCombo.getItemCount() > 0) paymentEmployeeCombo.setSelectedIndex(0);
            } else {
                if (paymentEmployeeCombo.getItemCount() > 0) paymentEmployeeCombo.setSelectedIndex(0);
            }
        } else {
            resetPaymentForm();
        }
    }

    private void addPaymentAction() {
        try {
            Employee selectedEmployee = (Employee) paymentEmployeeCombo.getSelectedItem();
            String amountText = paymentAmountField.getText();
            String method = (String) paymentMethodCombo.getSelectedItem();
            String status = (String) paymentStatusCombo.getSelectedItem();
            LocalDateTime paymentDateTime = getLocalDateTimeFromPicker(paymentDatePicker);
            String notes = paymentNotesArea.getText();

            if (method == null || method.isEmpty() || status == null || status.isEmpty() || paymentDateTime == null) {
                showAlert("Input Error", "Payment Date, Method, and Status are required.", JOptionPane.ERROR_MESSAGE); return;
            }
            BigDecimal amount = parseBigDecimal(amountText, "Total Amount");
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                showAlert("Input Error", "Total amount must be non-negative.", JOptionPane.ERROR_MESSAGE); return;
            }

            int employeeId = (selectedEmployee != null) ? selectedEmployee.getEmployeeId() : 0; // 0 hoặc giá trị biểu thị NULL

            Payment newPayment = new Payment(0, employeeId, paymentDateTime, amount, method, status, notes);

            Payment addedPayment = paymentDb.addPayment(newPayment);
            if (addedPayment != null) {
                loadPaymentsData();
                resetPaymentForm();
                showAlert("Success", "Payment added successfully with ID: " + addedPayment.getPaymentId(), JOptionPane.INFORMATION_MESSAGE);
            } else {
                showAlert("Error", "Failed to add payment.", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            showAlert("Input Error", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            showAlert("Database Error", "Error adding payment: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void updatePaymentAction() {
        int selectedViewRow = paymentTable.getSelectedRow();
        if (selectedViewRow == -1) {
            showAlert("Selection Error", "Please select a payment to update.", JOptionPane.WARNING_MESSAGE); return;
        }
        int modelRow = paymentTable.convertRowIndexToModel(selectedViewRow);
        Payment paymentToUpdate = paymentTableModel.getPaymentAt(modelRow);
        if (paymentToUpdate == null) return;

        try {
            Employee selectedEmployee = (Employee) paymentEmployeeCombo.getSelectedItem();
            String amountText = paymentAmountField.getText();
            String method = (String) paymentMethodCombo.getSelectedItem();
            String status = (String) paymentStatusCombo.getSelectedItem();
            LocalDateTime paymentDateTime = getLocalDateTimeFromPicker(paymentDatePicker);
            String notes = paymentNotesArea.getText();

            if (method == null || method.isEmpty() || status == null || status.isEmpty() || paymentDateTime == null) {
                showAlert("Input Error", "Payment Date, Method, and Status are required.", JOptionPane.ERROR_MESSAGE); return;
            }
            BigDecimal amount = parseBigDecimal(amountText, "Total Amount");
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                showAlert("Input Error", "Total amount must be non-negative.", JOptionPane.ERROR_MESSAGE); return;
            }
            int employeeId = (selectedEmployee != null) ? selectedEmployee.getEmployeeId() : 0;

            paymentToUpdate.setEmployeeId(employeeId);
            paymentToUpdate.setPaymentDate(paymentDateTime);
            paymentToUpdate.setPaymentMethod(method);
            paymentToUpdate.setTotalAmount(amount);
            paymentToUpdate.setStatus(status);
            paymentToUpdate.setNotes(notes);
            paymentToUpdate.setEmployeeName((selectedEmployee != null) ? selectedEmployee.getFirstName() + " " + selectedEmployee.getLastName() : null);


            boolean success = paymentDb.updatePayment(paymentToUpdate);
            if (success) {
                loadPaymentsData();
                resetPaymentForm();
                showAlert("Success", "Payment updated successfully.", JOptionPane.INFORMATION_MESSAGE);
            } else {
                showAlert("Error", "Failed to update payment.", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            showAlert("Input Error", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            showAlert("Database Error", "Error updating payment: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void deletePaymentAction() {
        int selectedViewRow = paymentTable.getSelectedRow();
        if (selectedViewRow == -1) {
            showAlert("Selection Error", "Please select a payment to delete.", JOptionPane.WARNING_MESSAGE); return;
        }
        int modelRow = paymentTable.convertRowIndexToModel(selectedViewRow);
        Payment paymentToDelete = paymentTableModel.getPaymentAt(modelRow);
        if (paymentToDelete == null) return;

        int confirmation = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to delete Payment ID: " + paymentToDelete.getPaymentId() + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                boolean success = paymentDb.deletePayment(paymentToDelete.getPaymentId());
                if (success) {
                    loadPaymentsData();
                    resetPaymentForm();
                    showAlert("Success", "Payment deleted successfully.", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    showAlert("Error", "Failed to delete payment.", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                if ("23503".equals(ex.getSQLState()) || (ex.getMessage() != null && ex.getMessage().contains("constraint"))) {
                    showAlert("Deletion Error", "Cannot delete payment: It is referenced in existing orders.", JOptionPane.ERROR_MESSAGE);
                } else {
                    showAlert("Database Error", "Error deleting payment: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                }
                ex.printStackTrace();
            }
        }
    }

    private void resetPaymentForm() {
        paymentIdField.setText("");
        if(paymentEmployeeCombo.getItemCount() > 0) paymentEmployeeCombo.setSelectedIndex(0);
        paymentAmountField.setText("");
        paymentDatePicker.setDate(null);
        if(paymentMethodCombo.getItemCount() > 0) paymentMethodCombo.setSelectedIndex(0);
        if(paymentStatusCombo.getItemCount() > 0) paymentStatusCombo.setSelectedIndex(0);
        paymentNotesArea.setText("");
        paymentTable.clearSelection();
    }
    // --- KẾT THÚC TAB PAYMENTS ---

    // --- TAB MANAGE ORDERS ---
    private void createManageOrdersTab() {
        JPanel ordersOuterPanel = new JPanel(new BorderLayout(10, 10));
        ordersOuterPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel chính cho bảng Orders và OrderItems (bên trái)
        JPanel leftPanel = new JPanel(new BorderLayout(5,5));

        orderTableModel = new OrderTableModel();
        orderTable = new JTable(orderTableModel);
        styleTable(orderTable);
        // Căn lề cho Order Table
        DefaultTableCellRenderer centerRendererOrder = new DefaultTableCellRenderer(); centerRendererOrder.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer rightRendererOrder = new DefaultTableCellRenderer(); rightRendererOrder.setHorizontalAlignment(JLabel.RIGHT);
        orderTable.getColumnModel().getColumn(0).setCellRenderer(centerRendererOrder); // Order ID
        orderTable.getColumnModel().getColumn(2).setCellRenderer(centerRendererOrder); // Payment ID
        orderTable.getColumnModel().getColumn(3).setCellRenderer(centerRendererOrder); // Order Date
        orderTable.getColumnModel().getColumn(4).setCellRenderer(centerRendererOrder); // Status
        orderTable.getColumnModel().getColumn(5).setCellRenderer(rightRendererOrder); // Net
        orderTable.getColumnModel().getColumn(6).setCellRenderer(rightRendererOrder); // Tax
        orderTable.getColumnModel().getColumn(7).setCellRenderer(rightRendererOrder); // Total

        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane orderTableScrollPane = new JScrollPane(orderTable);

        orderItemTableModel = new OrderItemTableModel();
        orderItemTable = new JTable(orderItemTableModel);
        styleTable(orderItemTable);
        // Căn lề cho OrderItem Table
        DefaultTableCellRenderer centerRendererItem = new DefaultTableCellRenderer(); centerRendererItem.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer rightRendererItem = new DefaultTableCellRenderer(); rightRendererItem.setHorizontalAlignment(JLabel.RIGHT);
        orderItemTable.getColumnModel().getColumn(0).setCellRenderer(centerRendererItem); // OD ID
        orderItemTable.getColumnModel().getColumn(2).setCellRenderer(centerRendererItem); // Qty
        orderItemTable.getColumnModel().getColumn(3).setCellRenderer(rightRendererItem); // Unit Price
        orderItemTable.getColumnModel().getColumn(4).setCellRenderer(rightRendererItem); // Item Total


        JScrollPane orderItemTableScrollPane = new JScrollPane(orderItemTable);
        orderItemTableScrollPane.setPreferredSize(new Dimension(0, 200)); // Giới hạn chiều cao

        JSplitPane leftSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, orderTableScrollPane, orderItemTableScrollPane);
        leftSplitPane.setResizeWeight(0.65); // Cho bảng Order nhiều không gian hơn
        leftPanel.add(leftSplitPane, BorderLayout.CENTER);

        orderTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && orderTable.getSelectedRow() != -1) {
                int modelRow = orderTable.convertRowIndexToModel(orderTable.getSelectedRow());
                Order selectedOrder = orderTableModel.getOrderAt(modelRow);
                if (selectedOrder != null) {
                    populateOrderFormWithSelectedOrder(selectedOrder); // Điền form chính
                    // Load lại items cho order đã chọn
                    try {
                        Order orderWithDetails = orderDb.getOrderById(selectedOrder.getOrderId());
                        if (orderWithDetails != null) {
                            tempOrderItems.clear();
                            tempOrderItems.addAll(orderWithDetails.getOrderItems()); // Dùng cho sửa
                            orderItemTableModel.setItems(new ArrayList<>(tempOrderItems)); // Hiển thị bản sao
                            updateTempOrderTotalLabel();
                        } else {
                            orderItemTableModel.clearItems();
                            tempOrderItems.clear();
                            updateTempOrderTotalLabel();
                        }
                    } catch (SQLException ex) {
                        showAlert("Database Error", "Could not load order items: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                        orderItemTableModel.clearItems();
                        tempOrderItems.clear();
                        updateTempOrderTotalLabel();
                    }
                }
            } else if (orderTable.getSelectedRow() == -1) { // Nếu không có dòng nào được chọn (ví dụ sau khi xóa hoặc clear)
                resetOrderFormAndItems();
            }
        });


        // Panel Form Orders (bên phải)
        JPanel orderRightPanel = new JPanel(new BorderLayout(5,5));
        JPanel orderFormPanel = new JPanel(new GridBagLayout());
        orderFormPanel.setBorder(BorderFactory.createTitledBorder("Order Details"));
        GridBagConstraints gbcForm = new GridBagConstraints();
        gbcForm.insets = new Insets(5, 5, 5, 5);

        orderIdField = new JTextField(5);
        orderIdField.setEditable(false);
        orderCustomerCombo = new JComboBox<>();
        orderPaymentCombo = new JComboBox<>();
        orderDatePicker = new JXDatePicker();
        orderDatePicker.setFormats(jxDatePickerFormatter);
        orderStatusCombo = new JComboBox<>(new String[]{"Pending", "Processing", "Shipped", "Delivered", "Cancelled", "Returned"});
        orderShippingAddressField = new JTextField(25);
        orderNotesArea = new JTextArea(2, 25);
        orderNotesArea.setLineWrap(true);
        orderNotesArea.setWrapStyleWord(true);
        JScrollPane orderNotesScrollPane = new JScrollPane(orderNotesArea);

        int y = 0;
        addFormField(orderFormPanel, gbcForm, "Order ID:", orderIdField, y++);
        addFormField(orderFormPanel, gbcForm, "Customer:", orderCustomerCombo, y++);
        addFormField(orderFormPanel, gbcForm, "Payment ID:", orderPaymentCombo, y++);
        addFormField(orderFormPanel, gbcForm, "Order Date:", orderDatePicker, y++);
        addFormField(orderFormPanel, gbcForm, "Status:", orderStatusCombo, y++);
        addFormField(orderFormPanel, gbcForm, "Shipping Address:", orderShippingAddressField, y++);
        addFormField(orderFormPanel, gbcForm, "Notes:", orderNotesScrollPane, y++);

        orderRightPanel.add(orderFormPanel, BorderLayout.NORTH);

        // Panel thêm OrderItem
        JPanel addItemPanel = new JPanel(new GridBagLayout());
        addItemPanel.setBorder(BorderFactory.createTitledBorder("Add/Update Item to Current List"));
        GridBagConstraints gbcItem = new GridBagConstraints();
        gbcItem.insets = new Insets(5,5,5,5);

        orderItemProductCombo = new JComboBox<>();
        orderItemQuantityField = new JTextField(5);
        JButton btnAddOrUpdateItemToList = new JButton("Add/Update Item");
        JButton btnRemoveItemFromList = new JButton("Remove Selected Item");
        lblOrderTotalAmount = new JLabel("Current Order Total: 0.00 VNĐ");
        lblOrderTotalAmount.setFont(new Font("Segoe UI", Font.BOLD, 14));

        y=0;
        addFormField(addItemPanel, gbcItem, "Product:", orderItemProductCombo, y++);
        addFormField(addItemPanel, gbcItem, "Quantity:", orderItemQuantityField, y++);

        gbcItem.gridx = 0; gbcItem.gridy = y; gbcItem.gridwidth=1; gbcItem.anchor = GridBagConstraints.EAST;
        addItemPanel.add(btnAddOrUpdateItemToList, gbcItem);
        gbcItem.gridx = 1; gbcItem.gridy = y++; gbcItem.anchor = GridBagConstraints.WEST;
        addItemPanel.add(btnRemoveItemFromList, gbcItem);

        gbcItem.gridx = 0; gbcItem.gridy = y; gbcItem.gridwidth = 2; gbcItem.anchor = GridBagConstraints.CENTER;
        gbcItem.insets = new Insets(10,5,5,5);
        addItemPanel.add(lblOrderTotalAmount, gbcItem);

        orderRightPanel.add(addItemPanel, BorderLayout.CENTER);


        // Panel nút chính cho Order
        JPanel orderButtonPanel = new JPanel();
        orderButtonPanel.setLayout(new BoxLayout(orderButtonPanel, BoxLayout.Y_AXIS));
        JButton btnCreateOrder = new JButton("Create New Order with Items");
        JButton btnUpdateOrder = new JButton("Save Changes to Order"); // Chỉ update thông tin chính của Order
        JButton btnDeleteOrder = new JButton("Delete Selected Order");
        JButton btnClearOrderForm = new JButton("Clear Form & Item List");

        styleButtonForVerticalLayout(btnCreateOrder, 250);
        styleButtonForVerticalLayout(btnUpdateOrder, 250);
        styleButtonForVerticalLayout(btnDeleteOrder, 250);
        styleButtonForVerticalLayout(btnClearOrderForm, 250);

        orderButtonPanel.add(btnCreateOrder);
        orderButtonPanel.add(Box.createRigidArea(new Dimension(0,8)));
        orderButtonPanel.add(btnUpdateOrder);
        orderButtonPanel.add(Box.createRigidArea(new Dimension(0,8)));
        orderButtonPanel.add(btnDeleteOrder);
        orderButtonPanel.add(Box.createRigidArea(new Dimension(0,8)));
        orderButtonPanel.add(btnClearOrderForm);

        JPanel southButtonContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        southButtonContainer.add(orderButtonPanel);
        orderRightPanel.add(southButtonContainer, BorderLayout.SOUTH);


        JSplitPane mainOrderSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, orderRightPanel);
        mainOrderSplitPane.setResizeWeight(0.60); // Cho phần bảng nhiều không gian hơn
        ordersOuterPanel.add(mainOrderSplitPane, BorderLayout.CENTER);

        // Action Listeners
        btnAddOrUpdateItemToList.addActionListener(e -> addOrUpdateOrderItemInTempListAction());
        btnRemoveItemFromList.addActionListener(e -> removeOrderItemFromTempListAction());
        btnCreateOrder.addActionListener(e -> addNewOrderAction());
        btnUpdateOrder.addActionListener(e -> updateSelectedOrderAction());
        btnDeleteOrder.addActionListener(e -> deleteSelectedOrderAction());
        btnClearOrderForm.addActionListener(e -> resetOrderFormAndItems());


        tabPane.addTab("Manage Orders", ordersOuterPanel);
    }

    private void loadOrdersData() {
        try {
            List<Order> orders = orderDb.getAllOrders(); // getAllOrders đã JOIN để lấy customerName
            orderTableModel.setOrders(orders);
        } catch (SQLException e) {
            showAlert("Database Error", "Could not load orders: " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void loadCustomersForOrderForm() {
        try {
            List<Customer> customers = customerDb.getAllCustomers();
            orderCustomerCombo.removeAllItems();
            orderCustomerCombo.addItem(null); // Mục trống
            for (Customer cust : customers) {
                orderCustomerCombo.addItem(cust);
            }
            orderCustomerCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Customer) {
                        setText(((Customer) value).getFirstName() + " " + ((Customer) value).getLastName());
                    } else {
                        setText("Select Customer...");
                    }
                    return this;
                }
            });
            if(orderCustomerCombo.getItemCount() > 0) orderCustomerCombo.setSelectedIndex(0);
        } catch (SQLException e) {
            showAlert("Database Error", "Could not load customers for order form: " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPaymentsForOrderForm() {
        try {
            List<Payment> payments = paymentDb.getAllPayments(); // Giả sử getAllPayments đã JOIN lấy employeeName
            orderPaymentCombo.removeAllItems();
            orderPaymentCombo.addItem(null); // Cho phép không chọn payment ban đầu
            for (Payment p : payments) {
                orderPaymentCombo.addItem(p);
            }
            orderPaymentCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Payment) {
                        Payment p = (Payment) value;
                        setText("ID: " + p.getPaymentId() + " (" + p.getStatus() + ") - " + (p.getTotalAmount() != null ? p.getTotalAmount().toPlainString() + " VNĐ" : "N/A"));
                    } else {
                        setText("Select Payment (Optional)");
                    }
                    return this;
                }
            });
            if(orderPaymentCombo.getItemCount() > 0) orderPaymentCombo.setSelectedIndex(0);
        } catch (SQLException e) {
            showAlert("Database Error", "Could not load payments for order form: " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadProductsForOrderItemForm() {
        try {
            List<Product> products = productDb.getAllProducts(); // Giả sử getAllProducts đã JOIN lấy categoryName
            orderItemProductCombo.removeAllItems();
            orderItemProductCombo.addItem(null); // Mục trống
            for (Product p : products) {
                orderItemProductCombo.addItem(p);
            }
            orderItemProductCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Product) {
                        setText(((Product) value).getSpecificProductName() + " (ID: " + ((Product)value).getProductId() + ")");
                    } else {
                        setText("Select Product...");
                    }
                    return this;
                }
            });
            if(orderItemProductCombo.getItemCount() > 0) orderItemProductCombo.setSelectedIndex(0);
        } catch (SQLException e) {
            showAlert("Database Error", "Could not load products for order item form: " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateOrderFormWithSelectedOrder(Order order) {
        if (order != null) {
            orderIdField.setText(String.valueOf(order.getOrderId()));
            setLocalDateToPicker(orderDatePicker, order.getOrderDate());
            orderStatusCombo.setSelectedItem(order.getStatus());
            orderShippingAddressField.setText(order.getShippingAddress());
            orderNotesArea.setText(order.getNotes());

            // Populate Customer ComboBox
            if (order.getCustomerId() > 0) {
                boolean found = false;
                for (int i = 0; i < orderCustomerCombo.getItemCount(); i++) {
                    Customer c = orderCustomerCombo.getItemAt(i);
                    if (c != null && c.getCustomerId() == order.getCustomerId()) {
                        orderCustomerCombo.setSelectedItem(c);
                        found = true;
                        break;
                    }
                }
                if (!found && orderCustomerCombo.getItemCount() > 0) orderCustomerCombo.setSelectedIndex(0);
            } else {
                if (orderCustomerCombo.getItemCount() > 0) orderCustomerCombo.setSelectedIndex(0);
            }

            // Populate Payment ComboBox
            if (order.getPaymentId() > 0) {
                boolean found = false;
                for (int i = 0; i < orderPaymentCombo.getItemCount(); i++) {
                    Payment p = orderPaymentCombo.getItemAt(i);
                    if (p != null && p.getPaymentId() == order.getPaymentId()) {
                        orderPaymentCombo.setSelectedItem(p);
                        found = true;
                        break;
                    }
                }
                if (!found && orderPaymentCombo.getItemCount() > 0) orderPaymentCombo.setSelectedIndex(0);
            } else {
                if (orderPaymentCombo.getItemCount() > 0) orderPaymentCombo.setSelectedIndex(0);
            }
        } else {
            resetOrderFormFields(); // Chỉ reset các field của order chính
        }
    }

    private void addOrUpdateOrderItemInTempListAction() {
        Product selectedProduct = (Product) orderItemProductCombo.getSelectedItem();
        String quantityText = orderItemQuantityField.getText();

        if (selectedProduct == null) {
            showAlert("Input Error", "Please select a product for the item.", JOptionPane.ERROR_MESSAGE); return;
        }
        if (selectedProduct.getPrice() == null) {
            showAlert("Data Error", "Selected product does not have a price defined.", JOptionPane.ERROR_MESSAGE); return;
        }

        try {
            int quantity = parseInt(quantityText, "Quantity");
            if (quantity <= 0) {
                showAlert("Input Error", "Item quantity must be greater than 0.", JOptionPane.ERROR_MESSAGE); return;
            }
            if (quantity > selectedProduct.getStockQuantity()) {
                showAlert("Input Error", "Quantity exceeds available stock ("+ selectedProduct.getStockQuantity() +").", JOptionPane.WARNING_MESSAGE);
                // Không return, cho phép thêm nhưng với số lượng bằng stock
                // quantity = selectedProduct.getStockQuantity();
                // orderItemQuantityField.setText(String.valueOf(quantity));
                // if(quantity <=0) return; // Nếu stock = 0 thì không cho thêm
                return; // Hoặc đơn giản là không cho phép nếu vượt quá
            }


            Optional<OrderItem> existingItemOpt = tempOrderItems.stream()
                    .filter(item -> item.getProductId() == selectedProduct.getProductId())
                    .findFirst();

            if (existingItemOpt.isPresent()) { // Update existing item in temp list
                OrderItem existingItem = existingItemOpt.get();
                existingItem.setQuantity(quantity);
                existingItem.setUnitPrice(selectedProduct.getPrice()); // Luôn cập nhật giá mới nhất từ Product
                existingItem.setProductName(selectedProduct.getSpecificProductName());
            } else { // Add new item to temp list
                // odId và orderId sẽ là 0 hoặc sẽ được gán khi lưu Order chính
                OrderItem newItem = new OrderItem(0, 0, selectedProduct.getProductId(), quantity, selectedProduct.getPrice());
                newItem.setProductName(selectedProduct.getSpecificProductName());
                tempOrderItems.add(newItem);
            }

            orderItemTableModel.setItems(new ArrayList<>(tempOrderItems)); // Hiển thị bản sao
            updateTempOrderTotalLabel();

            // Reset item form
            if(orderItemProductCombo.getItemCount() > 0) orderItemProductCombo.setSelectedIndex(0);
            orderItemQuantityField.setText("");

        } catch (NumberFormatException ex) {
            showAlert("Input Error", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeOrderItemFromTempListAction() {
        int selectedRowInItemTable = orderItemTable.getSelectedRow();
        if (selectedRowInItemTable == -1) {
            showAlert("Selection Error", "Please select an item from the list to remove.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = orderItemTable.convertRowIndexToModel(selectedRowInItemTable);
        tempOrderItems.remove(modelRow);
        orderItemTableModel.setItems(new ArrayList<>(tempOrderItems));
        updateTempOrderTotalLabel();
    }

    private void updateTempOrderTotalLabel() {
        BigDecimal currentTotal = BigDecimal.ZERO;
        for (OrderItem item : tempOrderItems) {
            if (item.getUnitPrice() != null && item.getQuantity() > 0) {
                currentTotal = currentTotal.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }
        // Giả sử thuế 10%
        BigDecimal tax = currentTotal.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal finalTotal = currentTotal.add(tax);
        lblOrderTotalAmount.setText("Current Order Total: " + finalTotal.toPlainString() + " VNĐ (Net: " + currentTotal.toPlainString() + ", Tax: " + tax.toPlainString() +")");
    }


    private void addNewOrderAction() {
        Customer selectedCustomer = (Customer) orderCustomerCombo.getSelectedItem();
        Payment selectedPayment = (Payment) orderPaymentCombo.getSelectedItem(); // Có thể null
        LocalDate orderDate = getLocalDateFromPicker(orderDatePicker);
        String status = (String) orderStatusCombo.getSelectedItem();
        String shippingAddress = orderShippingAddressField.getText().trim();
        String notes = orderNotesArea.getText().trim();


        if (selectedCustomer == null) {
            showAlert("Input Error", "Please select a customer.", JOptionPane.ERROR_MESSAGE); return;
        }
        if (orderDate == null) {
            showAlert("Input Error", "Please select an order date.", JOptionPane.ERROR_MESSAGE); return;
        }
        if (status == null || status.isEmpty()) {
            showAlert("Input Error", "Please select an order status.", JOptionPane.ERROR_MESSAGE); return;
        }
        if (tempOrderItems.isEmpty()) {
            showAlert("Input Error", "Order must have at least one item. Please add items to the list.", JOptionPane.ERROR_MESSAGE); return;
        }

        try {
            Order newOrder = new Order(); // Dùng constructor rỗng
            newOrder.setCustomerId(selectedCustomer.getCustomerId());
            newOrder.setPaymentId(selectedPayment != null ? selectedPayment.getPaymentId() : 0); // 0 nếu không có payment
            newOrder.setOrderDate(orderDate);
            newOrder.setStatus(status);
            newOrder.setShippingAddress(shippingAddress);
            newOrder.setNotes(notes);

            // Gán danh sách item đã được chuẩn bị
            newOrder.setOrderItems(new ArrayList<>(tempOrderItems)); // Gán bản sao
            // newOrder.calculateAndSetTotals(); // Đã gọi trong OrderDataStore.addOrder

            Order addedOrder = orderDb.addOrder(newOrder);
            if (addedOrder != null) {
                loadOrdersData(); // Load lại để có tên customer, ...
                resetOrderFormAndItems();
                showAlert("Success", "New order created successfully with ID: " + addedOrder.getOrderId(), JOptionPane.INFORMATION_MESSAGE);
            } else {
                showAlert("Error", "Failed to create new order.", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            showAlert("Input Error", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            showAlert("Database Error", "Error creating new order: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void updateSelectedOrderAction() {
        int selectedViewRow = orderTable.getSelectedRow();
        if (selectedViewRow == -1) {
            showAlert("Selection Error", "Please select an order to update.", JOptionPane.WARNING_MESSAGE); return;
        }
        int modelRow = orderTable.convertRowIndexToModel(selectedViewRow);
        Order orderToUpdate = orderTableModel.getOrderAt(modelRow);
        if (orderToUpdate == null) return;

        Customer selectedCustomer = (Customer) orderCustomerCombo.getSelectedItem();
        Payment selectedPayment = (Payment) orderPaymentCombo.getSelectedItem();
        LocalDate orderDate = getLocalDateFromPicker(orderDatePicker);
        String status = (String) orderStatusCombo.getSelectedItem();
        String shippingAddress = orderShippingAddressField.getText().trim();
        String notes = orderNotesArea.getText().trim();

        if (selectedCustomer == null || orderDate == null || status == null || status.isEmpty()) {
            showAlert("Input Error", "Customer, Order Date, and Status are required.", JOptionPane.ERROR_MESSAGE); return;
        }
        // Không cần kiểm tra tempOrderItems.isEmpty() ở đây vì updateOrder của DataStore
        // có thể chỉ cập nhật thông tin chính của Order. Nếu muốn đồng bộ items, logic đó nằm ở DataStore.

        try {
            orderToUpdate.setCustomerId(selectedCustomer.getCustomerId());
            orderToUpdate.setPaymentId(selectedPayment != null ? selectedPayment.getPaymentId() : 0);
            orderToUpdate.setOrderDate(orderDate);
            orderToUpdate.setStatus(status);
            orderToUpdate.setShippingAddress(shippingAddress);
            orderToUpdate.setNotes(notes);

            // QUAN TRỌNG: Gán danh sách item hiện tại từ form (tempOrderItems) vào orderToUpdate.
            // OrderDataStore.updateOrder() PHẢI có logic để xóa các order_details cũ và thêm lại các item mới này.
            // Nếu OrderDataStore.updateOrder() không làm điều này, thì các thay đổi item sẽ không được lưu.
            orderToUpdate.setOrderItems(new ArrayList<>(tempOrderItems)); // Gán bản sao
            // orderToUpdate.calculateAndSetTotals(); // Đã gọi trong OrderDataStore.updateOrder (nếu có)

            boolean success = orderDb.updateOrder(orderToUpdate);
            if (success) {
                loadOrdersData(); // Load lại để thấy tên customer, và có thể cả items nếu DataStore hỗ trợ
                // Sau khi loadOrdersData, orderTable sẽ được cập nhật.
                // Tìm lại dòng đã chọn và chọn lại nó (nếu ID không đổi)
                for(int i=0; i < orderTableModel.getRowCount(); i++){
                    if(orderTableModel.getOrderAt(i).getOrderId() == orderToUpdate.getOrderId()){
                        orderTable.setRowSelectionInterval(orderTable.convertRowIndexToView(i), orderTable.convertRowIndexToView(i));
                        // Kích hoạt lại việc load items cho dòng được chọn
                        Order reSelectedOrder = orderTableModel.getOrderAt(i);
                        if (reSelectedOrder != null) {
                            try {
                                Order orderWithDetails = orderDb.getOrderById(reSelectedOrder.getOrderId());
                                if(orderWithDetails != null) {
                                    orderItemTableModel.setItems(new ArrayList<>(orderWithDetails.getOrderItems()));
                                    updateTempOrderTotalLabel();
                                }
                            } catch (SQLException ex) { /*ignore*/ }
                        }
                        break;
                    }
                }
                showAlert("Success", "Order updated successfully.", JOptionPane.INFORMATION_MESSAGE);
            } else {
                showAlert("Error", "Failed to update order (no changes or order not found).", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            showAlert("Input Error", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            showAlert("Database Error", "Error updating order: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void deleteSelectedOrderAction() {
        int selectedViewRow = orderTable.getSelectedRow();
        if (selectedViewRow == -1) {
            showAlert("Selection Error", "Please select an order to delete.", JOptionPane.WARNING_MESSAGE); return;
        }
        int modelRow = orderTable.convertRowIndexToModel(selectedViewRow);
        Order orderToDelete = orderTableModel.getOrderAt(modelRow);
        if (orderToDelete == null) return;

        int confirmation = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to delete Order ID: " + orderToDelete.getOrderId() + "?\nThis will also delete all associated order items.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                boolean success = orderDb.deleteOrder(orderToDelete.getOrderId());
                if (success) {
                    loadOrdersData();
                    resetOrderFormAndItems();
                    showAlert("Success", "Order deleted successfully.", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    showAlert("Error", "Failed to delete order.", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                showAlert("Database Error", "Error deleting order: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void resetOrderFormFields() {
        orderIdField.setText("");
        if(orderCustomerCombo.getItemCount() > 0) orderCustomerCombo.setSelectedIndex(0);
        if(orderPaymentCombo.getItemCount() > 0) orderPaymentCombo.setSelectedIndex(0);
        orderDatePicker.setDate(null);
        if(orderStatusCombo.getItemCount() > 0) orderStatusCombo.setSelectedIndex(0);
        orderShippingAddressField.setText("");
        orderNotesArea.setText("");
        if(orderItemProductCombo.getItemCount() > 0) orderItemProductCombo.setSelectedIndex(0);
        orderItemQuantityField.setText("");
    }

    private void resetOrderFormAndItems() {
        resetOrderFormFields();
        tempOrderItems.clear();
        orderItemTableModel.clearItems();
        updateTempOrderTotalLabel(); // Reset label tổng tiền
        orderTable.clearSelection();
    }
    // --- KẾT THÚC TAB ORDERS ---

    public void showScreen() {
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }

    public JFrame getFrame() {
        return frame;
    }
}

