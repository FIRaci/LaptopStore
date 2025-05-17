package laptopstore.screen;

import laptopstore.LaptopStoreApplication;
import laptopstore.data.*;
import laptopstore.model.*;
import laptopstore.screen.tablemodel.*;

import org.jdesktop.swingx.JXDatePicker;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
// import java.text.ParseException; // Có vẻ không được sử dụng trực tiếp
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdminDashboardScreen {

    private JFrame frame;
    private JTabbedPane tabPane;

    // --- Components chung ---
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    // private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // Có vẻ không được sử dụng trực tiếp trong UI này


    // --- Tab Products ---
    private JTable productTable;
    private ProductTableModel productTableModel;
    private JTextField modelField, brandField, descriptionFieldProd, priceField, stockField, typeNameField;
    private JComboBox<String> typeCombo;

    // --- Tab Customers ---
    private JTable customerTable;
    private CustomerTableModel customerTableModel;
    private JTextField custUsernameField, custEmailField, custFirstNameField, custLastNameField, custAddressField, custPhoneField;
    private JComboBox<Character> custGenderCombo;
    private JXDatePicker custDateOfBirthPicker;

    // --- Tab Employees ---
    private JTable employeeTable;
    private EmployeeTableModel employeeTableModel;
    private JTextField empFirstNameField, empLastNameField, empPhoneField, empAddressField, empBankNumberField, empRoleField, empSalaryField, empWorkDayField;
    private JComboBox<Character> empGenderCombo;
    private JXDatePicker empHireDatePicker;

    // --- Tab Payments ---
    private JTable paymentTable;
    private PaymentTableModel paymentTableModel;
    private JComboBox<Employee> paymentEmployeeCombo;
    private JTextField paymentAmountField;
    private JXDatePicker paymentDatePicker;
    private JComboBox<String> paymentMethodCombo, paymentStatusCombo;

    // --- Tab Orders ---
    private JTable orderTable;
    private OrderTableModel orderTableModel;
    private JTable orderItemTable;
    private OrderItemTableModel orderItemTableModel;
    private JComboBox<Customer> orderCustomerCombo;
    private JComboBox<Payment> orderPaymentCombo;
    private JXDatePicker orderDatePicker;
    private JComboBox<String> orderStatusCombo;
    private JComboBox<Product> orderItemProductCombo;
    private JTextField orderItemQuantityField;
    private List<OrderItem> tempOrderItems = new ArrayList<>();


    public AdminDashboardScreen() {
        frame = new JFrame("Laptop Store Admin Dashboard (Swing)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1600, 900);
        frame.setLocationRelativeTo(null);

        tabPane = new JTabbedPane();

        createManageProductsTab();
        createManageCustomersTab();
        createManageEmployeesTab();
        createPaymentHistoryTab();
        createManageOrdersTab();

        frame.add(tabPane);
    }

    // Helper để style JTable
    private void styleTable(JTable table) {
        table.setRowHeight(25);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setShowGrid(true);
        table.setGridColor(Color.LIGHT_GRAY);

        JTableHeader header = table.getTableHeader();
        header.setFont(header.getFont().deriveFont(Font.BOLD));
        header.setPreferredSize(new Dimension(header.getWidth(), 30));
        ((DefaultTableCellRenderer)header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        table.setAutoCreateRowSorter(true);
    }

    // Helper để căn lề trái cho tất cả các ô trong bảng
    private void alignTableCellsLeft(JTable table) {
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(leftRenderer);
        }
    }


    // Helper để thêm label và component vào GridBagLayout
    private void addFormField(JPanel panel, GridBagConstraints gbc, JLabel label, JComponent component, int yPos) {
        gbc.gridx = 0;
        gbc.gridy = yPos;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.1;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.gridy = yPos;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.9;
        panel.add(component, gbc);
    }

    // Helper để style button cho BoxLayout dọc
    private void styleButtonForVerticalLayout(JButton button, int preferredWidth) {
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        Dimension buttonSize = new Dimension(preferredWidth, button.getPreferredSize().height + 5);
        button.setPreferredSize(buttonSize);
        button.setMinimumSize(buttonSize);
        button.setMaximumSize(new Dimension(preferredWidth, Short.MAX_VALUE));
    }

    // --- TAB MANAGE PRODUCTS ---
    private void createManageProductsTab() {
        JPanel productsPanel = new JPanel(new BorderLayout(10, 10));
        productsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        productTableModel = new ProductTableModel(LaptopStoreApplication.products);
        productTable = new JTable(productTableModel);
        styleTable(productTable);
        alignTableCellsLeft(productTable); // Căn lề trái
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && productTable.getSelectedRow() != -1) {
                populateProductForm(productTableModel.getProductAt(productTable.getSelectedRow()));
            }
        });
        JScrollPane productTableScrollPane = new JScrollPane(productTable);

        JPanel productFormPanel = new JPanel(new GridBagLayout());
        productFormPanel.setBorder(BorderFactory.createTitledBorder("Product Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);

        modelField = new JTextField(20);
        brandField = new JTextField(20);
        descriptionFieldProd = new JTextField(20);
        priceField = new JTextField(20);
        stockField = new JTextField(20);
        typeCombo = new JComboBox<>(new String[]{"", "Laptop", "Gear", "Components"});
        typeNameField = new JTextField(20);

        int y = 0;
        addFormField(productFormPanel, gbc, new JLabel("Model:"), modelField, y++);
        addFormField(productFormPanel, gbc, new JLabel("Brand:"), brandField, y++);
        addFormField(productFormPanel, gbc, new JLabel("Description:"), descriptionFieldProd, y++);
        addFormField(productFormPanel, gbc, new JLabel("Price:"), priceField, y++);
        addFormField(productFormPanel, gbc, new JLabel("Stock Quantity:"), stockField, y++);
        addFormField(productFormPanel, gbc, new JLabel("Select Type:"), typeCombo, y++);
        addFormField(productFormPanel, gbc, new JLabel("Type Name:"), typeNameField, y++);

        JPanel buttonPanelVertical = new JPanel();
        buttonPanelVertical.setLayout(new BoxLayout(buttonPanelVertical, BoxLayout.Y_AXIS));
        JButton addButton = new JButton("Add Product");
        JButton updateButton = new JButton("Update Product");
        JButton deleteButton = new JButton("Delete Selected");
        JButton cancelButton = new JButton("Cancel");

        int buttonPreferredWidth = 150;
        styleButtonForVerticalLayout(addButton, buttonPreferredWidth);
        styleButtonForVerticalLayout(updateButton, buttonPreferredWidth);
        styleButtonForVerticalLayout(deleteButton, buttonPreferredWidth);
        styleButtonForVerticalLayout(cancelButton, buttonPreferredWidth);

        buttonPanelVertical.add(addButton);
        buttonPanelVertical.add(Box.createRigidArea(new Dimension(0, 5)));
        buttonPanelVertical.add(updateButton);
        buttonPanelVertical.add(Box.createRigidArea(new Dimension(0, 5)));
        buttonPanelVertical.add(deleteButton);
        buttonPanelVertical.add(Box.createRigidArea(new Dimension(0, 5)));
        buttonPanelVertical.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 1.0;
        productFormPanel.add(buttonPanelVertical, gbc);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, productTableScrollPane, productFormPanel);
        splitPane.setResizeWeight(0.65);
        productsPanel.add(splitPane, BorderLayout.CENTER);

        addButton.addActionListener(e -> addProductAction());
        updateButton.addActionListener(e -> updateProductAction());
        deleteButton.addActionListener(e -> deleteProductAction());
        cancelButton.addActionListener(e -> cancelProductAction());

        tabPane.addTab("Manage Products", productsPanel);
    }

    private void populateProductForm(Product product) {
        if (product != null) {
            modelField.setText(product.getModel());
            brandField.setText(product.getBrand());
            descriptionFieldProd.setText(product.getDescription());
            priceField.setText(String.valueOf(product.getPrice()));
            stockField.setText(String.valueOf(product.getStockQuantity()));

            String type = "";
            String typeName = "";
            for (Laptop laptop : LaptopStoreApplication.laptops) {
                if (laptop.getProductId() == product.getProductId()) {
                    type = "Laptop";
                    typeName = laptop.getLaptopName();
                    break;
                }
            }
            if (type.isEmpty()) {
                for (Gear gear : LaptopStoreApplication.gears) {
                    if (gear.getProductId() == product.getProductId()) {
                        type = "Gear";
                        typeName = gear.getGearName();
                        break;
                    }
                }
            }
            if (type.isEmpty()) {
                for (Components component : LaptopStoreApplication.components) {
                    if (component.getProductId() == product.getProductId()) {
                        type = "Components";
                        typeName = component.getComponentName();
                        break;
                    }
                }
            }
            typeCombo.setSelectedItem(type);
            typeNameField.setText(typeName);
        }
    }
    private void addProductAction() {
        try {
            String model = modelField.getText();
            String brand = brandField.getText();
            String description = descriptionFieldProd.getText();
            String type = (String) typeCombo.getSelectedItem();
            String typeName = typeNameField.getText();

            if (model.isEmpty() || brand.isEmpty() || priceField.getText().isEmpty() || stockField.getText().isEmpty() || type == null || type.isEmpty() || typeName.isEmpty()) {
                showAlert("Error", "Please fill all required fields.", JOptionPane.ERROR_MESSAGE);
                return;
            }
            double price = Double.parseDouble(priceField.getText());
            int stock = Integer.parseInt(stockField.getText());


            int productId = ProductDataStore.getNextProductId();
            Product product = new Product(productId, model, brand, description, BigDecimal.valueOf(price), stock, LocalDateTime.now());
            ProductDataStore.addProduct(product, type, typeName);
            productTableModel.addProduct(product);

            resetProductForm();
            showAlert("Success", "Product added successfully.", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            showAlert("Error", "Price and Stock must be valid numbers.", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            showAlert("Error", "An error occurred: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    private void updateProductAction() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            showAlert("Error", "Please select a product to update.", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Product selectedProduct = productTableModel.getProductAt(selectedRow);

        try {
            String model = modelField.getText();
            String brand = brandField.getText();
            String description = descriptionFieldProd.getText();
            String type = (String) typeCombo.getSelectedItem();
            String typeName = typeNameField.getText();
            if (model.isEmpty() || brand.isEmpty() || priceField.getText().isEmpty() || stockField.getText().isEmpty() || type == null || type.isEmpty() || typeName.isEmpty()) {
                showAlert("Error", "Please fill all required fields.", JOptionPane.ERROR_MESSAGE);
                return;
            }
            double price = Double.parseDouble(priceField.getText());
            int stock = Integer.parseInt(stockField.getText());


            Product updatedProduct = new Product(selectedProduct.getProductId(), model, brand, description, BigDecimal.valueOf(price), stock, selectedProduct.getYearPublish());
            ProductDataStore.updateProduct(updatedProduct, type, typeName);
            productTableModel.updateProduct(selectedRow, updatedProduct);

            resetProductForm();
            showAlert("Success", "Product updated successfully.", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            showAlert("Error", "Price and Stock must be valid numbers.", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            showAlert("Error", "An error occurred: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    private void deleteProductAction() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            showAlert("Error", "Please select a product to delete.", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Product productToDelete = productTableModel.getProductAt(selectedRow);
        int confirmation = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to delete product: " + productToDelete.getModel() + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirmation == JOptionPane.YES_OPTION) {
            ProductDataStore.deleteProduct(productToDelete.getProductId());
            productTableModel.removeProduct(selectedRow);
            resetProductForm();
            showAlert("Success", "Product deleted successfully.", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    private void cancelProductAction() {
        resetProductForm();
        productTable.clearSelection();
    }
    private void resetProductForm() {
        modelField.setText("");
        brandField.setText("");
        descriptionFieldProd.setText("");
        priceField.setText("");
        stockField.setText("");
        typeCombo.setSelectedIndex(0);
        typeNameField.setText("");
    }


    // --- TAB MANAGE CUSTOMERS ---
    private void createManageCustomersTab() {
        JPanel customersPanel = new JPanel(new BorderLayout(10, 10));
        customersPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        customerTableModel = new CustomerTableModel(LaptopStoreApplication.customers);
        customerTable = new JTable(customerTableModel);
        styleTable(customerTable);
        alignTableCellsLeft(customerTable); // Căn lề trái
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && customerTable.getSelectedRow() != -1) {
                populateCustomerForm(customerTableModel.getCustomerAt(customerTable.getSelectedRow()));
            }
        });
        JScrollPane customerTableScrollPane = new JScrollPane(customerTable);

        JPanel customerFormPanel = new JPanel(new GridBagLayout());
        customerFormPanel.setBorder(BorderFactory.createTitledBorder("Customer Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);

        custUsernameField = new JTextField(15);
        custEmailField = new JTextField(15);
        custFirstNameField = new JTextField(15);
        custLastNameField = new JTextField(15);
        custGenderCombo = new JComboBox<>(new Character[]{'M', 'F'});
        custGenderCombo.insertItemAt(null, 0);
        custGenderCombo.setSelectedIndex(0);
        custAddressField = new JTextField(15);
        custDateOfBirthPicker = new JXDatePicker();
        custDateOfBirthPicker.setFormats(dateFormat);
        custDateOfBirthPicker.setPreferredSize(new Dimension(180, custDateOfBirthPicker.getPreferredSize().height));
        custPhoneField = new JTextField(15);

        int yCust = 0;
        addFormField(customerFormPanel, gbc, new JLabel("Username:"), custUsernameField, yCust++);
        addFormField(customerFormPanel, gbc, new JLabel("Email:"), custEmailField, yCust++);
        addFormField(customerFormPanel, gbc, new JLabel("First Name:"), custFirstNameField, yCust++);
        addFormField(customerFormPanel, gbc, new JLabel("Last Name:"), custLastNameField, yCust++);
        addFormField(customerFormPanel, gbc, new JLabel("Gender:"), custGenderCombo, yCust++);
        addFormField(customerFormPanel, gbc, new JLabel("Address:"), custAddressField, yCust++);
        addFormField(customerFormPanel, gbc, new JLabel("Date of Birth:"), custDateOfBirthPicker, yCust++);
        addFormField(customerFormPanel, gbc, new JLabel("Phone:"), custPhoneField, yCust++);

        JPanel custButtonPanelVertical = new JPanel();
        custButtonPanelVertical.setLayout(new BoxLayout(custButtonPanelVertical, BoxLayout.Y_AXIS));
        JButton custAddButton = new JButton("Add Customer");
        JButton custUpdateButton = new JButton("Update Customer");
        JButton custDeleteButton = new JButton("Delete Selected");
        JButton custCancelButton = new JButton("Cancel");

        int custButtonPreferredWidth = 150;
        styleButtonForVerticalLayout(custAddButton, custButtonPreferredWidth);
        styleButtonForVerticalLayout(custUpdateButton, custButtonPreferredWidth);
        styleButtonForVerticalLayout(custDeleteButton, custButtonPreferredWidth);
        styleButtonForVerticalLayout(custCancelButton, custButtonPreferredWidth);

        custButtonPanelVertical.add(custAddButton);
        custButtonPanelVertical.add(Box.createRigidArea(new Dimension(0, 5)));
        custButtonPanelVertical.add(custUpdateButton);
        custButtonPanelVertical.add(Box.createRigidArea(new Dimension(0, 5)));
        custButtonPanelVertical.add(custDeleteButton);
        custButtonPanelVertical.add(Box.createRigidArea(new Dimension(0, 5)));
        custButtonPanelVertical.add(custCancelButton);

        gbc.gridx = 0; gbc.gridy = yCust; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 1.0;
        customerFormPanel.add(custButtonPanelVertical, gbc);

        JSplitPane custSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, customerTableScrollPane, customerFormPanel);
        custSplitPane.setResizeWeight(0.65);
        customersPanel.add(custSplitPane, BorderLayout.CENTER);

        custAddButton.addActionListener(e -> addCustomerAction());
        custUpdateButton.addActionListener(e -> updateCustomerAction());
        custDeleteButton.addActionListener(e -> deleteCustomerAction());
        custCancelButton.addActionListener(e -> cancelCustomerAction());

        tabPane.addTab("Manage Customers", customersPanel);
    }

    private void populateCustomerForm(Customer customer) {
        if (customer != null) {
            custUsernameField.setText(customer.getUsername());
            custEmailField.setText(customer.getEmail());
            custFirstNameField.setText(customer.getFirstName());
            custLastNameField.setText(customer.getLastName());
            if (customer.getGender() != ' ' && customer.getGender() != '\0') {
                custGenderCombo.setSelectedItem(customer.getGender());
            } else {
                custGenderCombo.setSelectedIndex(0);
            }
            custAddressField.setText(customer.getAddress());
            if (customer.getDateOfBirth() != null) {
                custDateOfBirthPicker.setDate(Date.from(customer.getDateOfBirth().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            } else {
                custDateOfBirthPicker.setDate(null);
            }
            custPhoneField.setText(customer.getPhone());
        }
    }
    private void addCustomerAction() {
        try {
            String firstName = custFirstNameField.getText();
            String lastName = custLastNameField.getText();
            if (firstName.isEmpty() || lastName.isEmpty()) {
                showAlert("Error", "First name and last name are required.", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String username = custUsernameField.getText();
            String email = custEmailField.getText();
            Character gender = (Character) custGenderCombo.getSelectedItem();
            if (gender == null) gender = ' ';
            String address = custAddressField.getText();
            LocalDate dateOfBirth = null;
            if (custDateOfBirthPicker.getDate() != null) {
                dateOfBirth = custDateOfBirthPicker.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
            String phone = custPhoneField.getText();

            int customerId = CustomerDataStore.getNextCustomerId();
            Customer customer = new Customer(customerId, username, email, firstName, lastName, LocalDateTime.now(), gender, address, dateOfBirth, phone);
            CustomerDataStore.addCustomer(customer);
            customerTableModel.addCustomer(customer);

            resetCustomerForm();
            showAlert("Success", "Customer added successfully.", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            showAlert("Error", "Error adding customer: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    private void updateCustomerAction() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            showAlert("Error", "Please select a customer to update.", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Customer selectedCustomer = customerTableModel.getCustomerAt(selectedRow);

        try {
            String firstName = custFirstNameField.getText();
            String lastName = custLastNameField.getText();
            if (firstName.isEmpty() || lastName.isEmpty()) {
                showAlert("Error", "First name and last name are required.", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String username = custUsernameField.getText();
            String email = custEmailField.getText();
            Character genderInput = (Character) custGenderCombo.getSelectedItem();
            char gender = (genderInput != null) ? genderInput : selectedCustomer.getGender();


            String address = custAddressField.getText();
            LocalDate dateOfBirth = selectedCustomer.getDateOfBirth();
            if (custDateOfBirthPicker.getDate() != null) {
                dateOfBirth = custDateOfBirthPicker.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
            String phone = custPhoneField.getText();

            Customer updatedCustomer = new Customer(selectedCustomer.getCustomerId(), username, email, firstName, lastName, selectedCustomer.getCreatedAt(), gender, address, dateOfBirth, phone);
            CustomerDataStore.updateCustomer(updatedCustomer);
            customerTableModel.updateCustomer(selectedRow, updatedCustomer);

            resetCustomerForm();
            showAlert("Success", "Customer updated successfully.", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            showAlert("Error", "Error updating customer: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    private void deleteCustomerAction() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            showAlert("Error", "Please select a customer to delete.", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Customer customerToDelete = customerTableModel.getCustomerAt(selectedRow);
        int confirmation = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to delete customer: " + customerToDelete.getFirstName() + " " + customerToDelete.getLastName() + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirmation == JOptionPane.YES_OPTION) {
            CustomerDataStore.deleteCustomer(customerToDelete.getCustomerId());
            customerTableModel.removeCustomer(selectedRow);
            resetCustomerForm();
            showAlert("Success", "Customer deleted successfully.", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    private void cancelCustomerAction() {
        resetCustomerForm();
        customerTable.clearSelection();
    }
    private void resetCustomerForm() {
        custUsernameField.setText("");
        custEmailField.setText("");
        custFirstNameField.setText("");
        custLastNameField.setText("");
        custGenderCombo.setSelectedIndex(0);
        custAddressField.setText("");
        custDateOfBirthPicker.setDate(null);
        custPhoneField.setText("");
    }

    // --- TAB MANAGE EMPLOYEES ---
    private void createManageEmployeesTab() {
        JPanel employeesPanel = new JPanel(new BorderLayout(10, 10));
        employeesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        employeeTableModel = new EmployeeTableModel(LaptopStoreApplication.employees);
        employeeTable = new JTable(employeeTableModel);
        styleTable(employeeTable);
        alignTableCellsLeft(employeeTable); // Căn lề trái
        employeeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        employeeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && employeeTable.getSelectedRow() != -1) {
                populateEmployeeForm(employeeTableModel.getEmployeeAt(employeeTable.getSelectedRow()));
            }
        });
        JScrollPane employeeTableScrollPane = new JScrollPane(employeeTable);

        JPanel employeeFormPanel = new JPanel(new GridBagLayout());
        employeeFormPanel.setBorder(BorderFactory.createTitledBorder("Employee Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);

        empFirstNameField = new JTextField(15);
        empLastNameField = new JTextField(15);
        empPhoneField = new JTextField(15);
        empAddressField = new JTextField(15);
        empGenderCombo = new JComboBox<>(new Character[]{'M', 'F'});
        empGenderCombo.insertItemAt(null, 0);
        empGenderCombo.setSelectedIndex(0);
        empBankNumberField = new JTextField(15);
        empRoleField = new JTextField(15);
        empSalaryField = new JTextField(15);
        empWorkDayField = new JTextField(15);
        empHireDatePicker = new JXDatePicker();
        empHireDatePicker.setFormats(dateFormat);
        empHireDatePicker.setPreferredSize(new Dimension(180, empHireDatePicker.getPreferredSize().height));

        int yEmp = 0;
        addFormField(employeeFormPanel, gbc, new JLabel("First Name:"), empFirstNameField, yEmp++);
        addFormField(employeeFormPanel, gbc, new JLabel("Last Name:"), empLastNameField, yEmp++);
        addFormField(employeeFormPanel, gbc, new JLabel("Phone:"), empPhoneField, yEmp++);
        addFormField(employeeFormPanel, gbc, new JLabel("Address:"), empAddressField, yEmp++);
        addFormField(employeeFormPanel, gbc, new JLabel("Gender:"), empGenderCombo, yEmp++);
        addFormField(employeeFormPanel, gbc, new JLabel("Bank Number:"), empBankNumberField, yEmp++);
        addFormField(employeeFormPanel, gbc, new JLabel("Role:"), empRoleField, yEmp++);
        addFormField(employeeFormPanel, gbc, new JLabel("Salary:"), empSalaryField, yEmp++);
        addFormField(employeeFormPanel, gbc, new JLabel("Work Days:"), empWorkDayField, yEmp++);
        addFormField(employeeFormPanel, gbc, new JLabel("Hire Date:"), empHireDatePicker, yEmp++);

        JPanel empButtonPanelVertical = new JPanel();
        empButtonPanelVertical.setLayout(new BoxLayout(empButtonPanelVertical, BoxLayout.Y_AXIS));
        JButton empAddButton = new JButton("Add Employee");
        JButton empUpdateButton = new JButton("Update Employee");
        JButton empDeleteButton = new JButton("Delete Selected");
        JButton empCancelButton = new JButton("Cancel");

        int empButtonPreferredWidth = 150;
        styleButtonForVerticalLayout(empAddButton, empButtonPreferredWidth);
        styleButtonForVerticalLayout(empUpdateButton, empButtonPreferredWidth);
        styleButtonForVerticalLayout(empDeleteButton, empButtonPreferredWidth);
        styleButtonForVerticalLayout(empCancelButton, empButtonPreferredWidth);

        empButtonPanelVertical.add(empAddButton);
        empButtonPanelVertical.add(Box.createRigidArea(new Dimension(0, 5)));
        empButtonPanelVertical.add(empUpdateButton);
        empButtonPanelVertical.add(Box.createRigidArea(new Dimension(0, 5)));
        empButtonPanelVertical.add(empDeleteButton);
        empButtonPanelVertical.add(Box.createRigidArea(new Dimension(0, 5)));
        empButtonPanelVertical.add(empCancelButton);

        gbc.gridx = 0; gbc.gridy = yEmp; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 1.0;
        employeeFormPanel.add(empButtonPanelVertical, gbc);

        JSplitPane empSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, employeeTableScrollPane, employeeFormPanel);
        empSplitPane.setResizeWeight(0.70);
        employeesPanel.add(empSplitPane, BorderLayout.CENTER);

        empAddButton.addActionListener(e -> addEmployeeAction());
        empUpdateButton.addActionListener(e -> updateEmployeeAction());
        empDeleteButton.addActionListener(e -> deleteEmployeeAction());
        empCancelButton.addActionListener(e -> cancelEmployeeAction());

        tabPane.addTab("Manage Employees", employeesPanel);
    }

    private void populateEmployeeForm(Employee employee) {
        if (employee != null) {
            empFirstNameField.setText(employee.getFirstName());
            empLastNameField.setText(employee.getLastName());
            empPhoneField.setText(employee.getPhone());
            empAddressField.setText(employee.getAddress());
            if (employee.getGender() != ' ' && employee.getGender() != '\0') {
                empGenderCombo.setSelectedItem(employee.getGender());
            } else {
                empGenderCombo.setSelectedIndex(0);
            }
            empBankNumberField.setText(employee.getBankNumber());
            empRoleField.setText(employee.getRole());
            empSalaryField.setText(String.valueOf(employee.getSalary()));
            empWorkDayField.setText(employee.getWorkDay());
            if (employee.getHireDate() != null) {
                empHireDatePicker.setDate(Date.from(employee.getHireDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            } else {
                empHireDatePicker.setDate(null);
            }
        }
    }
    private void addEmployeeAction() {
        try {
            String firstName = empFirstNameField.getText();
            String lastName = empLastNameField.getText();
            String role = empRoleField.getText();
            if (firstName.isEmpty() || lastName.isEmpty() || role.isEmpty()) {
                showAlert("Error", "First name, last name, and role are required.", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String phone = empPhoneField.getText();
            String address = empAddressField.getText();
            Character gender = (Character) empGenderCombo.getSelectedItem();
            if (gender == null) gender = ' ';
            String bankNumber = empBankNumberField.getText();
            double salary = Double.parseDouble(empSalaryField.getText());
            String workDay = empWorkDayField.getText();
            LocalDate hireDate = LocalDate.now();
            if (empHireDatePicker.getDate() != null) {
                hireDate = empHireDatePicker.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }

            int employeeId = EmployeeDataStore.getNextEmployeeId();
            Employee employee = new Employee(employeeId, firstName, lastName, phone, address, gender, bankNumber, role, salary, workDay, hireDate);
            EmployeeDataStore.addEmployee(employee);
            employeeTableModel.addEmployee(employee);

            resetEmployeeForm();
            showAlert("Success", "Employee added successfully.", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            showAlert("Error", "Salary must be a valid number.", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            showAlert("Error", "Error adding employee: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    private void updateEmployeeAction() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            showAlert("Error", "Please select an employee to update.", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Employee selectedEmployee = employeeTableModel.getEmployeeAt(selectedRow);

        try {
            String firstName = empFirstNameField.getText();
            String lastName = empLastNameField.getText();
            String role = empRoleField.getText();
            if (firstName.isEmpty() || lastName.isEmpty() || role.isEmpty()) {
                showAlert("Error", "First name, last name, and role are required.", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String phone = empPhoneField.getText();
            String address = empAddressField.getText();
            Character genderInput = (Character) empGenderCombo.getSelectedItem();
            char gender = (genderInput != null) ? genderInput : selectedEmployee.getGender();
            String bankNumber = empBankNumberField.getText();
            double salary = Double.parseDouble(empSalaryField.getText());
            String workDay = empWorkDayField.getText();
            LocalDate hireDate = selectedEmployee.getHireDate();
            if (empHireDatePicker.getDate() != null) {
                hireDate = empHireDatePicker.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }

            Employee updatedEmployee = new Employee(selectedEmployee.getEmployeeId(), firstName, lastName, phone, address, gender, bankNumber, role, salary, workDay, hireDate);
            EmployeeDataStore.updateEmployee(updatedEmployee);
            employeeTableModel.updateEmployee(selectedRow, updatedEmployee);

            resetEmployeeForm();
            showAlert("Success", "Employee updated successfully.", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            showAlert("Error", "Salary must be a valid number.", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            showAlert("Error", "Error updating employee: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    private void deleteEmployeeAction() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            showAlert("Error", "Please select an employee to delete.", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Employee employeeToDelete = employeeTableModel.getEmployeeAt(selectedRow);
        int confirmation = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to delete employee: " + employeeToDelete.getFirstName() + " " + employeeToDelete.getLastName() + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirmation == JOptionPane.YES_OPTION) {
            EmployeeDataStore.deleteEmployee(employeeToDelete.getEmployeeId());
            employeeTableModel.removeEmployee(selectedRow);
            resetEmployeeForm();
            showAlert("Success", "Employee deleted successfully.", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    private void cancelEmployeeAction() {
        resetEmployeeForm();
        employeeTable.clearSelection();
    }
    private void resetEmployeeForm() {
        empFirstNameField.setText("");
        empLastNameField.setText("");
        empPhoneField.setText("");
        empAddressField.setText("");
        empGenderCombo.setSelectedIndex(0);
        empBankNumberField.setText("");
        empRoleField.setText("");
        empSalaryField.setText("");
        empWorkDayField.setText("");
        empHireDatePicker.setDate(null);
    }


    // --- TAB PAYMENT HISTORY ---
    private void createPaymentHistoryTab() {
        JPanel paymentsPanel = new JPanel(new BorderLayout(10, 10));
        paymentsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        paymentTableModel = new PaymentTableModel(LaptopStoreApplication.payments);
        paymentTable = new JTable(paymentTableModel);
        styleTable(paymentTable);
        alignTableCellsLeft(paymentTable); // Căn lề trái
        paymentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        paymentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && paymentTable.getSelectedRow() != -1) {
                populatePaymentForm(paymentTableModel.getPaymentAt(paymentTable.getSelectedRow()));
            }
        });
        JScrollPane paymentTableScrollPane = new JScrollPane(paymentTable);

        JPanel paymentFormPanel = new JPanel(new GridBagLayout());
        paymentFormPanel.setBorder(BorderFactory.createTitledBorder("Payment Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);

        paymentEmployeeCombo = new JComboBox<>();
        LaptopStoreApplication.employees.forEach(emp -> paymentEmployeeCombo.addItem(emp));
        paymentEmployeeCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Employee) {
                    Employee emp = (Employee) value;
                    setText(emp.getFirstName() + " " + emp.getLastName());
                } else if (value == null) {
                    setText("Select Employee");
                }
                return this;
            }
        });
        paymentEmployeeCombo.insertItemAt(null,0);
        paymentEmployeeCombo.setSelectedIndex(0);

        paymentAmountField = new JTextField(15);
        paymentDatePicker = new JXDatePicker();
        paymentDatePicker.setFormats(dateFormat);
        paymentDatePicker.setPreferredSize(new Dimension(180, paymentDatePicker.getPreferredSize().height));
        paymentMethodCombo = new JComboBox<>(new String[]{"", "Cash", "Credit Card", "Bank Transfer"});
        paymentStatusCombo = new JComboBox<>(new String[]{"", "Paid", "Pending", "Canceled", "Refunded"});

        int yPay = 0;
        addFormField(paymentFormPanel, gbc, new JLabel("Employee:"), paymentEmployeeCombo, yPay++);
        addFormField(paymentFormPanel, gbc, new JLabel("Total Amount:"), paymentAmountField, yPay++);
        addFormField(paymentFormPanel, gbc, new JLabel("Payment Date:"), paymentDatePicker, yPay++);
        addFormField(paymentFormPanel, gbc, new JLabel("Method:"), paymentMethodCombo, yPay++);
        addFormField(paymentFormPanel, gbc, new JLabel("Status:"), paymentStatusCombo, yPay++);

        JPanel payButtonPanelVertical = new JPanel();
        payButtonPanelVertical.setLayout(new BoxLayout(payButtonPanelVertical, BoxLayout.Y_AXIS));
        JButton paymentAddButton = new JButton("Add Payment");
        JButton paymentUpdateButton = new JButton("Update Payment");
        JButton paymentDeleteButton = new JButton("Delete Selected");
        JButton paymentCancelButton = new JButton("Cancel");

        int payButtonPreferredWidth = 150;
        styleButtonForVerticalLayout(paymentAddButton, payButtonPreferredWidth);
        styleButtonForVerticalLayout(paymentUpdateButton, payButtonPreferredWidth);
        styleButtonForVerticalLayout(paymentDeleteButton, payButtonPreferredWidth);
        styleButtonForVerticalLayout(paymentCancelButton, payButtonPreferredWidth);

        payButtonPanelVertical.add(paymentAddButton);
        payButtonPanelVertical.add(Box.createRigidArea(new Dimension(0, 5)));
        payButtonPanelVertical.add(paymentUpdateButton);
        payButtonPanelVertical.add(Box.createRigidArea(new Dimension(0, 5)));
        payButtonPanelVertical.add(paymentDeleteButton);
        payButtonPanelVertical.add(Box.createRigidArea(new Dimension(0, 5)));
        payButtonPanelVertical.add(paymentCancelButton);

        gbc.gridx = 0; gbc.gridy = yPay; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 1.0;
        paymentFormPanel.add(payButtonPanelVertical, gbc);

        JSplitPane paySplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, paymentTableScrollPane, paymentFormPanel);
        paySplitPane.setResizeWeight(0.70);
        paymentsPanel.add(paySplitPane, BorderLayout.CENTER);

        paymentAddButton.addActionListener(e -> addPaymentAction());
        paymentUpdateButton.addActionListener(e -> updatePaymentAction());
        paymentDeleteButton.addActionListener(e -> deletePaymentAction());
        paymentCancelButton.addActionListener(e -> cancelPaymentAction());

        tabPane.addTab("Payment History", paymentsPanel);
    }

    private void populatePaymentForm(Payment payment) {
        if (payment != null) {
            Employee selectedEmp = LaptopStoreApplication.employees.stream()
                    .filter(emp -> emp.getEmployeeId() == payment.getEmployeeId())
                    .findFirst().orElse(null);
            paymentEmployeeCombo.setSelectedItem(selectedEmp);
            paymentAmountField.setText(String.valueOf(payment.getTotalAmount()));
            if (payment.getPaymentDate() != null) {
                paymentDatePicker.setDate(Date.from(payment.getPaymentDate().atZone(ZoneId.systemDefault()).toInstant()));
            } else {
                paymentDatePicker.setDate(null);
            }
            paymentMethodCombo.setSelectedItem(payment.getPaymentMethod());
            paymentStatusCombo.setSelectedItem(payment.getStatus());
        }
    }
    private void addPaymentAction() {
        try {
            Employee selectedEmployee = (Employee) paymentEmployeeCombo.getSelectedItem();
            String amountText = paymentAmountField.getText();
            String method = (String) paymentMethodCombo.getSelectedItem();
            String status = (String) paymentStatusCombo.getSelectedItem();

            if (selectedEmployee == null || amountText.isEmpty() || paymentDatePicker.getDate() == null || method == null || method.isEmpty() || status == null || status.isEmpty()) {
                showAlert("Error", "Please fill all required fields.", JOptionPane.ERROR_MESSAGE);
                return;
            }
            double amount = Double.parseDouble(amountText);
            LocalDateTime paymentDateTime = paymentDatePicker.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();


            int paymentId = PaymentDataStore.getNextPaymentId();
            Payment payment = new Payment(paymentId, selectedEmployee.getEmployeeId(), paymentDateTime, amount, method, status);
            PaymentDataStore.addPayment(payment);
            paymentTableModel.addPayment(payment);

            resetPaymentForm();
            showAlert("Success", "Payment added successfully.", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            showAlert("Error", "Amount must be a valid number.", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            showAlert("Error", "Error adding payment: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    private void updatePaymentAction() {
        int selectedRow = paymentTable.getSelectedRow();
        if (selectedRow == -1) {
            showAlert("Error", "Please select a payment to update.", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Payment selectedPayment = paymentTableModel.getPaymentAt(selectedRow);
        try {
            Employee selectedEmployee = (Employee) paymentEmployeeCombo.getSelectedItem();
            String amountText = paymentAmountField.getText();
            String method = (String) paymentMethodCombo.getSelectedItem();
            String status = (String) paymentStatusCombo.getSelectedItem();

            if (selectedEmployee == null || amountText.isEmpty() || paymentDatePicker.getDate() == null || method == null || method.isEmpty() || status == null || status.isEmpty()) {
                showAlert("Error", "Please fill all required fields.", JOptionPane.ERROR_MESSAGE);
                return;
            }
            double amount = Double.parseDouble(amountText);
            LocalDateTime paymentDateTime = paymentDatePicker.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

            Payment updatedPayment = new Payment(selectedPayment.getPaymentId(), selectedEmployee.getEmployeeId(), paymentDateTime, amount, method, status);
            PaymentDataStore.updatePayment(updatedPayment);
            paymentTableModel.updatePayment(selectedRow, updatedPayment);

            resetPaymentForm();
            showAlert("Success", "Payment updated successfully.", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            showAlert("Error", "Amount must be a valid number.", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            showAlert("Error", "Error updating payment: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    private void deletePaymentAction() {
        int selectedRow = paymentTable.getSelectedRow();
        if (selectedRow == -1) {
            showAlert("Error", "Please select a payment to delete.", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Payment paymentToDelete = paymentTableModel.getPaymentAt(selectedRow);
        int confirmation = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to delete payment ID: " + paymentToDelete.getPaymentId() + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirmation == JOptionPane.YES_OPTION) {
            PaymentDataStore.deletePayment(paymentToDelete.getPaymentId());
            paymentTableModel.removePayment(selectedRow);
            resetPaymentForm();
            showAlert("Success", "Payment deleted successfully.", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    private void cancelPaymentAction() {
        resetPaymentForm();
        paymentTable.clearSelection();
    }
    private void resetPaymentForm() {
        paymentEmployeeCombo.setSelectedIndex(0);
        paymentAmountField.setText("");
        paymentDatePicker.setDate(null);
        paymentMethodCombo.setSelectedIndex(0);
        paymentStatusCombo.setSelectedIndex(0);
    }


    // --- TAB MANAGE ORDERS ---
    private void createManageOrdersTab() {
        JPanel ordersPanel = new JPanel(new BorderLayout(10, 10));
        ordersPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Phần Bảng Orders và OrderItems (Bên trái, xếp dọc) ---
        orderTableModel = new OrderTableModel(LaptopStoreApplication.orders);
        orderTable = new JTable(orderTableModel);
        styleTable(orderTable);
        alignTableCellsLeft(orderTable); // Căn lề trái
        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        orderItemTableModel = new OrderItemTableModel();
        orderItemTable = new JTable(orderItemTableModel);
        styleTable(orderItemTable);
        alignTableCellsLeft(orderItemTable); // Căn lề trái
        orderItemTable.setPreferredScrollableViewportSize(new Dimension(orderItemTable.getPreferredScrollableViewportSize().width, 150));

        orderTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && orderTable.getSelectedRow() != -1) {
                Order selectedOrder = orderTableModel.getOrderAt(orderTable.getSelectedRow());
                populateOrderForm(selectedOrder);
                if (selectedOrder != null && selectedOrder.getOrderItems() != null) {
                    orderItemTableModel.setItems(selectedOrder.getOrderItems());
                } else {
                    orderItemTableModel.clearItems();
                }
                tempOrderItems.clear();
                if(selectedOrder != null && selectedOrder.getOrderItems() != null) {
                    // Tạo bản sao của các item để tránh sửa đổi trực tiếp danh sách gốc của order
                    for(OrderItem item : selectedOrder.getOrderItems()){
                        tempOrderItems.add(new OrderItem(item.getOdId(), item.getOrderId(), item.getProductId(), item.getQuantity(), item.getUnitPrice()));
                    }
                }
            } else {
                resetOrderFormFields();
                orderItemTableModel.clearItems();
                tempOrderItems.clear();
            }
        });

        JScrollPane orderTableScrollPane = new JScrollPane(orderTable);
        JScrollPane orderItemTableScrollPane = new JScrollPane(orderItemTable);

        JPanel tablesPanel = new JPanel();
        tablesPanel.setLayout(new BoxLayout(tablesPanel, BoxLayout.Y_AXIS));
        tablesPanel.add(orderTableScrollPane);
        tablesPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        tablesPanel.add(new JLabel("Order Items (for selected order):"));
        tablesPanel.add(orderItemTableScrollPane);


        // --- Phần Form Orders (Bên phải) ---
        JPanel orderFormPanel = new JPanel(new GridBagLayout());
        orderFormPanel.setBorder(BorderFactory.createTitledBorder("Order Details & New Item"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        orderCustomerCombo = new JComboBox<>();
        LaptopStoreApplication.customers.forEach(cust -> orderCustomerCombo.addItem(cust));
        orderCustomerCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Customer) {
                    Customer cust = (Customer) value;
                    setText(cust.getFirstName() + " " + cust.getLastName() + " (ID: " + cust.getCustomerId() + ")");
                } else if (value == null) {
                    setText("Select Customer");
                }
                return this;
            }
        });
        orderCustomerCombo.insertItemAt(null, 0);
        orderCustomerCombo.setSelectedIndex(0);

        orderPaymentCombo = new JComboBox<>();
        LaptopStoreApplication.payments.forEach(pay -> orderPaymentCombo.addItem(pay));
        orderPaymentCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Payment) {
                    Payment pay = (Payment) value;
                    setText("ID: " + pay.getPaymentId() + " - $" + pay.getTotalAmount() + " - " + pay.getStatus());
                } else if (value == null) {
                    setText("Select Payment");
                }
                return this;
            }
        });
        orderPaymentCombo.insertItemAt(null,0);
        orderPaymentCombo.setSelectedIndex(0);


        orderDatePicker = new JXDatePicker();
        orderDatePicker.setFormats(dateFormat);
        orderDatePicker.setPreferredSize(new Dimension(180, orderDatePicker.getPreferredSize().height));
        orderStatusCombo = new JComboBox<>(new String[]{"", "Pending", "Shipped", "Delivered", "Canceled"});

        orderItemProductCombo = new JComboBox<>();
        LaptopStoreApplication.products.forEach(prod -> orderItemProductCombo.addItem(prod));
        orderItemProductCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Product) {
                    setText(((Product) value).getModel() + " (ID: " + ((Product) value).getProductId() + ")");
                } else if (value == null) {
                    setText("Select Product");
                }
                return this;
            }
        });
        orderItemProductCombo.insertItemAt(null, 0);
        orderItemProductCombo.setSelectedIndex(0);

        orderItemQuantityField = new JTextField(5);
        JButton addOrderItemButton = new JButton("Add/Update Item in List");

        int yOrder = 0;
        addFormField(orderFormPanel, gbc, new JLabel("Customer:"), orderCustomerCombo, yOrder++);
        addFormField(orderFormPanel, gbc, new JLabel("Payment:"), orderPaymentCombo, yOrder++);
        addFormField(orderFormPanel, gbc, new JLabel("Order Date:"), orderDatePicker, yOrder++);
        addFormField(orderFormPanel, gbc, new JLabel("Status:"), orderStatusCombo, yOrder++);

        gbc.gridy = yOrder++; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.CENTER;
        orderFormPanel.add(new JSeparator(SwingConstants.HORIZONTAL), gbc);
        gbc.gridwidth = 1; // Reset

        addFormField(orderFormPanel, gbc, new JLabel("Item Product:"), orderItemProductCombo, yOrder++);
        addFormField(orderFormPanel, gbc, new JLabel("Item Quantity:"), orderItemQuantityField, yOrder++);

        gbc.gridx = 0; gbc.gridy = yOrder++; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE;
        orderFormPanel.add(addOrderItemButton, gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL; // Reset


        JPanel orderButtonPanelVertical = new JPanel();
        orderButtonPanelVertical.setLayout(new BoxLayout(orderButtonPanelVertical, BoxLayout.Y_AXIS));
        JButton orderAddButton = new JButton("Create New Order");
        JButton orderUpdateButton = new JButton("Save Changes to Selected Order");
        JButton orderDeleteButton = new JButton("Delete Selected Order");
        JButton orderCancelButton = new JButton("Clear Form / Cancel");

        int orderButtonPreferredWidth = 220;
        styleButtonForVerticalLayout(orderAddButton, orderButtonPreferredWidth);
        styleButtonForVerticalLayout(orderUpdateButton, orderButtonPreferredWidth);
        styleButtonForVerticalLayout(orderDeleteButton, orderButtonPreferredWidth);
        styleButtonForVerticalLayout(orderCancelButton, orderButtonPreferredWidth);

        orderButtonPanelVertical.add(orderAddButton);
        orderButtonPanelVertical.add(Box.createRigidArea(new Dimension(0, 5)));
        orderButtonPanelVertical.add(orderUpdateButton);
        orderButtonPanelVertical.add(Box.createRigidArea(new Dimension(0, 5)));
        orderButtonPanelVertical.add(orderDeleteButton);
        orderButtonPanelVertical.add(Box.createRigidArea(new Dimension(0, 5)));
        orderButtonPanelVertical.add(orderCancelButton);

        gbc.gridx = 0; gbc.gridy = yOrder; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 1.0;
        orderFormPanel.add(orderButtonPanelVertical, gbc);


        JSplitPane orderSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablesPanel, orderFormPanel);
        orderSplitPane.setResizeWeight(0.60);
        ordersPanel.add(orderSplitPane, BorderLayout.CENTER);

        addOrderItemButton.addActionListener(e -> addOrUpdateOrderItemInTempListAction());
        orderAddButton.addActionListener(e -> addNewOrderAction());
        orderUpdateButton.addActionListener(e -> updateSelectedOrderAction());
        orderDeleteButton.addActionListener(e -> deleteSelectedOrderAction());
        orderCancelButton.addActionListener(e -> cancelOrderAction());

        tabPane.addTab("Manage Orders", ordersPanel);
    }

    private void populateOrderForm(Order order) {
        if (order != null) {
            Customer selectedCust = LaptopStoreApplication.customers.stream()
                    .filter(c -> c.getCustomerId() == order.getCustomerId())
                    .findFirst().orElse(null);
            orderCustomerCombo.setSelectedItem(selectedCust);

            Payment selectedPay = LaptopStoreApplication.payments.stream()
                    .filter(p -> p.getPaymentId() == order.getPaymentId())
                    .findFirst().orElse(null);
            orderPaymentCombo.setSelectedItem(selectedPay);

            if (order.getOrderDate() != null) {
                orderDatePicker.setDate(Date.from(order.getOrderDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            } else {
                orderDatePicker.setDate(null);
            }
            orderStatusCombo.setSelectedItem(order.getStatus());
        }
    }

    private void addOrUpdateOrderItemInTempListAction() {
        Product selectedProduct = (Product) orderItemProductCombo.getSelectedItem();
        String quantityText = orderItemQuantityField.getText();

        if (selectedProduct == null || quantityText.isEmpty()) {
            showAlert("Error", "Please select a product and enter quantity for the item.", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            int quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) {
                showAlert("Error", "Item quantity must be greater than 0.", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Kiểm tra xem sản phẩm đã có trong tempOrderItems chưa để cập nhật hoặc thêm mới
            boolean itemUpdated = false;
            for(OrderItem item : tempOrderItems) {
                if(item.getProductId() == selectedProduct.getProductId()) {
                    item.setQuantity(quantity); // Cập nhật số lượng
                    item.setUnitPrice(selectedProduct.getPrice()); // Cập nhật giá nếu cần
                    itemUpdated = true;
                    break;
                }
            }
            if (!itemUpdated) {
                OrderItem newItem = new OrderItem(0, 0, selectedProduct.getProductId(), quantity, selectedProduct.getPrice());
                tempOrderItems.add(newItem);
            }

            orderItemTableModel.setItems(new ArrayList<>(tempOrderItems));

            orderItemProductCombo.setSelectedIndex(0);
            orderItemQuantityField.setText("");

        } catch (NumberFormatException ex) {
            showAlert("Error", "Item quantity must be a valid number.", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addNewOrderAction() {
        Customer selectedCustomer = (Customer) orderCustomerCombo.getSelectedItem();
        Payment selectedPayment = (Payment) orderPaymentCombo.getSelectedItem();
        Date selectedDate = orderDatePicker.getDate();
        String status = (String) orderStatusCombo.getSelectedItem();

        if (selectedCustomer == null || selectedPayment == null || selectedDate == null || status == null || status.isEmpty() || tempOrderItems.isEmpty()) {
            showAlert("Error", "Please fill all order details and add at least one item.", JOptionPane.ERROR_MESSAGE);
            return;
        }
        LocalDate orderDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        try {
            int orderId = OrderDataStore.getNextOrderId();
            Order newOrder = new Order(orderId, selectedCustomer.getCustomerId(), selectedPayment.getPaymentId(), orderDate, status, 0, 0, 0);

            for (OrderItem item : tempOrderItems) {
                item.setOrderId(orderId);
                newOrder.addOrderItem(item);
            }

            OrderDataStore.addOrder(newOrder);
            orderTableModel.addOrder(newOrder);

            resetOrderFormFieldsAndTempItems();
            showAlert("Success", "New order added successfully.", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            showAlert("Error", "Error adding new order: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void updateSelectedOrderAction() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            showAlert("Error", "Please select an order to update.", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Order selectedOrderInTable = orderTableModel.getOrderAt(selectedRow);

        Customer selectedCustomer = (Customer) orderCustomerCombo.getSelectedItem();
        Payment selectedPayment = (Payment) orderPaymentCombo.getSelectedItem();
        Date selectedDate = orderDatePicker.getDate();
        String status = (String) orderStatusCombo.getSelectedItem();

        if (selectedCustomer == null || selectedPayment == null || selectedDate == null || status == null || status.isEmpty() || tempOrderItems.isEmpty()) {
            showAlert("Error", "Please fill all order details and add at least one item for update.", JOptionPane.ERROR_MESSAGE);
            return;
        }
        LocalDate orderDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        try {
            // Tạo một đối tượng Order mới với các thông tin đã cập nhật
            // Các giá trị tính toán như netAmount, tax, totalAmount sẽ được tính lại trong addOrderItem
            Order updatedOrder = new Order(selectedOrderInTable.getOrderId(), selectedCustomer.getCustomerId(), selectedPayment.getPaymentId(), orderDate, status, 0,0,0);

            // Xóa các item cũ khỏi đối tượng updatedOrder và thêm các item từ tempOrderItems
            // Quan trọng: Cần đảm bảo odId của các item được giữ lại nếu chúng đã tồn tại,
            // hoặc gán odId mới nếu là item mới hoàn toàn trong tempOrderItems.
            // OrderDataStore.updateOrder sẽ cần xử lý logic này phức tạp hơn.
            // Hiện tại, chúng ta chỉ đơn giản là gán lại danh sách items.
            updatedOrder.getOrderItems().clear();
            for (OrderItem tempItem : tempOrderItems) {
                // Nếu tempItem chưa có odId (item mới thêm vào temp), nó sẽ được gán khi OrderDataStore.updateOrder xử lý
                // Nếu tempItem đã có odId (item cũ được sửa), nó sẽ được giữ nguyên
                updatedOrder.addOrderItem(new OrderItem(tempItem.getOdId(), updatedOrder.getOrderId(), tempItem.getProductId(), tempItem.getQuantity(), tempItem.getUnitPrice()));
            }

            OrderDataStore.updateOrder(updatedOrder); // DataStore cần logic để cập nhật hoặc thêm/xóa OrderItems

            // Cập nhật lại đối tượng trong TableModel và làm mới bảng
            orderTableModel.updateOrder(selectedRow, updatedOrder);
            orderTable.setRowSelectionInterval(selectedRow, selectedRow); // Giữ lựa chọn
            orderItemTableModel.setItems(new ArrayList<>(updatedOrder.getOrderItems()));


            showAlert("Success", "Order updated successfully.", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            showAlert("Error", "Error updating order: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void deleteSelectedOrderAction() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            showAlert("Error", "Please select an order to delete.", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Order orderToDelete = orderTableModel.getOrderAt(selectedRow);
        int confirmation = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to delete Order ID: " + orderToDelete.getOrderId() + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirmation == JOptionPane.YES_OPTION) {
            OrderDataStore.deleteOrder(orderToDelete.getOrderId());
            orderTableModel.removeOrder(selectedRow);
            resetOrderFormFieldsAndTempItems();
            showAlert("Success", "Order deleted successfully.", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void cancelOrderAction() {
        resetOrderFormFieldsAndTempItems();
        orderTable.clearSelection();
    }

    private void resetOrderFormFields() {
        orderCustomerCombo.setSelectedIndex(0);
        orderPaymentCombo.setSelectedIndex(0);
        orderDatePicker.setDate(null);
        orderStatusCombo.setSelectedIndex(0);
        orderItemProductCombo.setSelectedIndex(0);
        orderItemQuantityField.setText("");
    }

    private void resetOrderFormFieldsAndTempItems() {
        resetOrderFormFields();
        tempOrderItems.clear();
        orderItemTableModel.clearItems();
    }


    // --- Show Screen and Alert ---
    public void showScreen() {
        frame.setVisible(true);
    }

    private void showAlert(String title, String message, int messageType) {
        JOptionPane.showMessageDialog(frame, message, title, messageType);
    }

    public JFrame getFrame() {
        return frame;
    }
}