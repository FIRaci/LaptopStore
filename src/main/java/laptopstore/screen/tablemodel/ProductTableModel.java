package laptopstore.screen.tablemodel; // Tạo package mới nếu cần

import laptopstore.model.Product;
import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.ArrayList;

public class ProductTableModel extends AbstractTableModel {
    private final List<Product> products;
    private final String[] columnNames = {"ID", "Model", "Brand", "Description", "Price", "Stock"};

    public ProductTableModel(List<Product> products) {
        this.products = new ArrayList<>(products); // Tạo bản sao để tránh thay đổi trực tiếp từ bên ngoài
    }

    @Override
    public int getRowCount() {
        return products.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Product product = products.get(rowIndex);
        switch (columnIndex) {
            case 0: return product.getProductId();
            case 1: return product.getModel();
            case 2: return product.getBrand();
            case 3: return product.getDescription();
            case 4: return product.getPrice();
            case 5: return product.getStockQuantity();
            default: return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: // ID
            case 5: // Stock
                return Integer.class;
            case 4: // Price
                return Double.class;
            default:
                return String.class;
        }
    }

    // Các phương thức để cập nhật dữ liệu trong table model nếu cần
    public void addProduct(Product product) {
        products.add(product);
        fireTableRowsInserted(products.size() - 1, products.size() - 1);
    }

    public void removeProduct(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < products.size()) {
            products.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }

    public void removeProduct(Product product) {
        int rowIndex = products.indexOf(product);
        if (rowIndex != -1) {
            removeProduct(rowIndex);
        }
    }

    public void updateProduct(int rowIndex, Product product) {
        if (rowIndex >= 0 && rowIndex < products.size()) {
            products.set(rowIndex, product);
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }

    public Product getProductAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < products.size()) {
            return products.get(rowIndex);
        }
        return null;
    }

    // Phương thức để cập nhật toàn bộ danh sách sản phẩm
    public void setProducts(List<Product> newProducts) {
        this.products.clear();
        this.products.addAll(newProducts);
        fireTableDataChanged(); // Thông báo cho JTable rằng toàn bộ dữ liệu đã thay đổi
    }
}