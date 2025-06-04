package laptopstore.screen.tablemodel;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class DynamicTableModel extends AbstractTableModel {
    private List<Object[]> data;
    private String[] columnNames;

    public DynamicTableModel() {
        this.data = new ArrayList<>();
        this.columnNames = new String[0];
    }

    public DynamicTableModel(List<Object[]> data, String[] columnNames) {
        this.data = new ArrayList<>(data);
        this.columnNames = Arrays.copyOf(columnNames, columnNames.length);
    }

    /**
     * Sets new data and column structure for the table.
     * This will fire a table structure changed event.
     * @param newData The list of data rows. Each element in the list is an Object array representing a row.
     * @param newColumnNames The array of column names.
     */
    public void setData(List<Object[]> newData, String[] newColumnNames) {
        if (newData == null) {
            this.data = new ArrayList<>();
        } else {
            this.data = new ArrayList<>(newData);
        }

        if (newColumnNames == null) {
            this.columnNames = new String[0];
        } else {
            this.columnNames = Arrays.copyOf(newColumnNames, newColumnNames.length);
        }
        // Quan trọng: Thông báo cho JTable rằng cấu trúc (số cột, tên cột) và dữ liệu đã thay đổi hoàn toàn.
        fireTableStructureChanged();
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex >= 0 && columnIndex < columnNames.length) {
            return columnNames[columnIndex];
        }
        return super.getColumnName(columnIndex);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= 0 && rowIndex < data.size()) {
            Object[] row = data.get(rowIndex);
            if (columnIndex >= 0 && columnIndex < row.length) {
                return row[columnIndex];
            }
        }
        return null;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        // Cố gắng xác định kiểu dữ liệu của cột từ dòng dữ liệu đầu tiên
        // Điều này giúp JTable render và sort tốt hơn.
        if (data.isEmpty() || getColumnCount() == 0 || columnIndex >= getColumnCount()) {
            return Object.class;
        }
        Object value = getValueAt(0, columnIndex);
        if (value != null) {
            return value.getClass();
        }
        return Object.class; // Mặc định nếu không có dữ liệu hoặc giá trị null
    }

    /**
     * Clears all data from the table.
     */
    public void clearData() {
        this.data.clear();
        this.columnNames = new String[0];
        fireTableStructureChanged(); // Hoặc fireTableDataChanged() nếu chỉ xóa dữ liệu giữ cột
    }
}
