package laptopstore.util;

import laptopstore.LaptopStoreApplication;
import laptopstore.model.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

public class DataLoader {

    public static void loadDataFromDatabase() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            loadProducts(conn);
            loadCustomers(conn);
            loadEmployees(conn);
            loadOrdersAndItems(conn);
            loadPayments(conn);
            System.out.println("Data loaded successfully from database!");
        } catch (SQLException e) {
            System.err.println("Failed to load data from database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void loadProducts(Connection conn) throws SQLException {
        LaptopStoreApplication.products.clear();
        String query = "SELECT id, name, brand, description, price, stock, \"createdAt\", type FROM \"Product\"";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String brand = rs.getString("brand");
                String description = rs.getString("description");
                BigDecimal price = rs.getBigDecimal("price");
                int stock = rs.getInt("stock");
                Timestamp createdAt = rs.getTimestamp("createdAt");
                LocalDateTime yearPublish = createdAt != null ? createdAt.toLocalDateTime() : LocalDateTime.now();
                
                Product p = new Product(id, name, brand, description, price, stock, yearPublish);
                LaptopStoreApplication.products.add(p);
                
                String type = rs.getString("type");
                if ("LAPTOP".equals(type)) {
                    LaptopStoreApplication.laptops.add(new Laptop(id, id, name));
                } else if ("GEAR".equals(type)) {
                    LaptopStoreApplication.gears.add(new Gear(id, id, name));
                } else if ("COMPONENT".equals(type)) {
                    LaptopStoreApplication.components.add(new Components(id, id, name));
                }
            }
        }
    }

    private static void loadCustomers(Connection conn) throws SQLException {
        LaptopStoreApplication.customers.clear();
        String query = "SELECT id, username, email, \"firstName\", \"lastName\", gender, address, \"dateOfBirth\", phone, \"createdAt\" FROM \"Customer\"";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String email = rs.getString("email");
                String firstName = rs.getString("firstName");
                String lastName = rs.getString("lastName");
                String genderStr = rs.getString("gender");
                char gender = (genderStr != null && !genderStr.isEmpty()) ? genderStr.charAt(0) : 'U';
                String address = rs.getString("address");
                Date dobDate = rs.getDate("dateOfBirth");
                LocalDate dateOfBirth = dobDate != null ? dobDate.toLocalDate() : null;
                String phone = rs.getString("phone");
                Timestamp createdAtTs = rs.getTimestamp("createdAt");
                LocalDateTime createdAt = createdAtTs != null ? createdAtTs.toLocalDateTime() : LocalDateTime.now();
                
                LaptopStoreApplication.customers.add(new Customer(id, username, email, firstName, lastName, createdAt, gender, address, dateOfBirth, phone));
            }
        }
    }

    private static void loadEmployees(Connection conn) throws SQLException {
        LaptopStoreApplication.employees.clear();
        String query = "SELECT id, \"firstName\", \"lastName\", phone, address, role, salary, \"hireDate\" FROM \"Employee\"";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String firstName = rs.getString("firstName");
                String lastName = rs.getString("lastName");
                String phone = rs.getString("phone");
                String address = rs.getString("address");
                String role = rs.getString("role");
                BigDecimal salary = rs.getBigDecimal("salary");
                double salaryVal = salary != null ? salary.doubleValue() : 0.0;
                Date hireDateDb = rs.getDate("hireDate");
                LocalDate hireDate = hireDateDb != null ? hireDateDb.toLocalDate() : null;
                
                // Using dummy values for gender, bankNumber, workDay as they might not be in DB yet
                LaptopStoreApplication.employees.add(new Employee(id, firstName, lastName, phone, address, 'U', "N/A", role, salaryVal, "Mon-Fri", hireDate));
            }
        }
    }

    private static void loadOrdersAndItems(Connection conn) throws SQLException {
        LaptopStoreApplication.orders.clear();
        LaptopStoreApplication.orderItems.clear();
        String orderQuery = "SELECT id, \"customerId\", status, \"orderDate\", \"netAmount\", tax, \"totalAmount\" FROM \"Order\"";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(orderQuery)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                int customerId = rs.getInt("customerId");
                String status = rs.getString("status");
                Timestamp orderDateTs = rs.getTimestamp("orderDate");
                LocalDate orderDate = orderDateTs != null ? orderDateTs.toLocalDateTime().toLocalDate() : LocalDate.now();
                double netAmount = rs.getBigDecimal("netAmount") != null ? rs.getBigDecimal("netAmount").doubleValue() : 0.0;
                double tax = rs.getBigDecimal("tax") != null ? rs.getBigDecimal("tax").doubleValue() : 0.0;
                double totalAmount = rs.getBigDecimal("totalAmount") != null ? rs.getBigDecimal("totalAmount").doubleValue() : 0.0;
                
                Order order = new Order(id, customerId, 1, orderDate, status, netAmount, tax, totalAmount);
                LaptopStoreApplication.orders.add(order);
            }
        }

        String itemQuery = "SELECT id, \"orderId\", \"productId\", quantity, \"unitPrice\" FROM \"OrderItem\"";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(itemQuery)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                int orderId = rs.getInt("orderId");
                int productId = rs.getInt("productId");
                int quantity = rs.getInt("quantity");
                double unitPrice = rs.getBigDecimal("unitPrice") != null ? rs.getBigDecimal("unitPrice").doubleValue() : 0.0;
                
                OrderItem item = new OrderItem(id, orderId, productId, quantity, unitPrice);
                LaptopStoreApplication.orderItems.add(item);
                
                for (Order o : LaptopStoreApplication.orders) {
                    if (o.getOrderId() == orderId) {
                        o.addOrderItem(item);
                        break;
                    }
                }
            }
        }
    }

    private static void loadPayments(Connection conn) throws SQLException {
        LaptopStoreApplication.payments.clear();
        String query = "SELECT id, \"orderId\", \"employeeId\", amount, method, status, \"paidAt\" FROM \"Payment\"";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                int orderId = rs.getInt("orderId");
                // Int employeeId might be null
                int employeeId = rs.getInt("employeeId");
                double amount = rs.getBigDecimal("amount") != null ? rs.getBigDecimal("amount").doubleValue() : 0.0;
                String method = rs.getString("method");
                String status = rs.getString("status");
                Timestamp paidAtTs = rs.getTimestamp("paidAt");
                LocalDateTime paidAt = paidAtTs != null ? paidAtTs.toLocalDateTime() : null;
                
                LaptopStoreApplication.payments.add(new Payment(id, orderId, paidAt, amount, method, status));
            }
        }
    }
}
