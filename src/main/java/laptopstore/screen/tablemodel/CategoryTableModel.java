package laptopstore.screen.tablemodel;

import laptopstore.model.Category;
import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.ArrayList;

public class CategoryTableModel extends AbstractTableModel {
    private List<Category> categories;
    private final String[] columnNames = {"ID", "Category Name"};

    public CategoryTableModel(List<Category> categories) {
        // Khởi tạo với một bản sao của danh sách để tránh thay đổi từ bên ngoài
        this.categories = new ArrayList<>(categories != null ? categories : new ArrayList<>());
    }

    public CategoryTableModel() {
        this.categories = new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        return categories.size();
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
        Category category = categories.get(rowIndex);
        switch (columnIndex) {
            case 0: return category.getCategoryId();
            case 1: return category.getCategoryName();
            default: return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) { // ID
            return Integer.class;
        }
        return String.class;
    }

    public void setCategories(List<Category> newCategories) {
        this.categories.clear();
        if (newCategories != null) {
            this.categories.addAll(newCategories);
        }
        fireTableDataChanged(); // Thông báo cho JTable rằng toàn bộ dữ liệu đã thay đổi
    }

    public Category getCategoryAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < categories.size()) {
            return categories.get(rowIndex);
        }
        return null;
    }

    // Các phương thức tiện ích để quản lý dữ liệu trong model (nếu cần cập nhật UI ngay)
    public void addCategoryRow(Category category) {
        if (category != null) {
            categories.add(category);
            fireTableRowsInserted(categories.size() - 1, categories.size() - 1);
        }
    }

    public void removeCategoryRow(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < categories.size()) {
            categories.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }

    public void removeCategoryRow(Category category) {
        int rowIndex = categories.indexOf(category);
        if (rowIndex != -1) {
            removeCategoryRow(rowIndex);
        }
    }

    public void updateCategoryRow(int rowIndex, Category category) {
        if (rowIndex >= 0 && rowIndex < categories.size() && category != null) {
            categories.set(rowIndex, category);
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }
}
