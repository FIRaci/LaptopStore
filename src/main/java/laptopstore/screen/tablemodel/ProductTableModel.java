package laptopstore.screen.tablemodel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import laptopstore.model.Product;

public class ProductTableModel extends AbstractTableModel {
    private List<Product> products;
    private final String[] columnNames = {"ID", "Name", "Model", "Brand", "Category Name", "Price", "Stock", "Published"};
    private final DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yyyy");

    public ProductTableModel(List<Product> products) {
        this.products = new ArrayList<>(products != null ? products : new ArrayList<>());
    }

    public ProductTableModel() {
        this.products = new ArrayList<>();
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
            case 1: return product.getSpecificProductName();
            case 2: return product.getModel();
            case 3: return product.getBrand();
            case 4: return product.getCategoryName() != null ? product.getCategoryName() : "N/A"; // Show category name
            case 5:
                BigDecimal price = product.getPrice();
                return price != null ? price.setScale(2, RoundingMode.HALF_UP) : null;
            case 6: return product.getStockQuantity();
            case 7:
                if (product.getYearPublish() != null) {
                    return product.getYearPublish().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                }
                return "";
            default: return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: // ID
            case 6: // Stock
                return Integer.class;
            case 5: // Price
                return BigDecimal.class;
            default:
                return String.class;
        }
    }

    public void setProducts(List<Product> newProducts) {
        this.products.clear();
        if (newProducts != null) {
            this.products.addAll(newProducts);
        }
        fireTableDataChanged();
    }

    public Product getProductAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < products.size()) {
            return products.get(rowIndex);
        }
        return null;
    }

    public void addProductRow(Product product) {
        if (product != null) {
            products.add(product);
            fireTableRowsInserted(products.size() - 1, products.size() - 1);
        }
    }

    public void removeProductRow(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < products.size()) {
            products.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }

    public void removeProductRow(Product product) {
        int rowIndex = products.indexOf(product);
        if (rowIndex != -1) {
            removeProductRow(rowIndex);
        }
    }

    public void updateProductRow(int rowIndex, Product product) {
        if (rowIndex >= 0 && rowIndex < products.size() && product != null) {
            products.set(rowIndex, product);
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }
}
