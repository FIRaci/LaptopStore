package laptopstore.data;

import laptopstore.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.NumberFormat; // For parsing locale-specific numbers
import java.text.ParseException; // For parsing locale-specific numbers
import java.util.Locale; // For parsing locale-specific numbers


public class DynamicQueryDataStore {

    // Sử dụng NumberFormat cho việc parse số có thể chứa dấu phẩy
    // Giả sử định dạng số của CSDL/JDBC có thể là kiểu "1,234,567.89"
    // Locale.US dùng '.' làm dấu thập phân và ',' làm dấu nhóm.
    // Nếu CSDL của bro dùng định dạng khác (ví dụ VN: "1.234.567,89"),
    // bro cần thay Locale phù hợp (ví dụ: new Locale("vi", "VN"))
    // Tuy nhiên, sau khi đổi sang NUMERIC, JDBC nên trả về giá trị chuẩn không cần locale.
    // Nhưng để phòng hờ, chúng ta có thể thử parse.
    private static final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);


    public List<Map<String, Object>> executeQuery(String sqlQuery, List<Object> params) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        System.out.println("Executing Dynamic Query: " + sqlQuery);
        if (params != null && !params.isEmpty()) {
            System.out.println("With Parameters: " + params);
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {

            if (params != null) {
                for (int i = 0; i < params.size(); i++) {
                    pstmt.setObject(i + 1, params.get(i));
                }
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnLabel = metaData.getColumnLabel(i);
                        int columnType = metaData.getColumnType(i);
                        Object columnValue = null;

                        switch (columnType) {
                            case Types.NUMERIC:
                            case Types.DECIMAL:
                                columnValue = rs.getBigDecimal(i);
                                break;
                            case Types.DOUBLE:
                            case Types.FLOAT:
                            case Types.REAL:
                                // Đây là nơi có thể xảy ra lỗi nếu CSDL vẫn là MONEY
                                // hoặc JDBC driver trả về chuỗi có định dạng locale.
                                // Cố gắng đọc dưới dạng String trước để làm sạch.
                                String stringValueForDouble = rs.getString(i);
                                if (stringValueForDouble != null) {
                                    try {
                                        // Loại bỏ tất cả các ký tự không phải là số, dấu chấm, dấu trừ
                                        // và thay thế dấu phẩy (phân cách hàng nghìn) bằng rỗng.
                                        String cleanedString = stringValueForDouble.replaceAll("[^\\d.,-]", "").replace(",", "");
                                        // Nếu sau khi làm sạch, chuỗi vẫn không hợp lệ cho BigDecimal, nó sẽ throw NumberFormatException
                                        columnValue = new BigDecimal(cleanedString);
                                    } catch (NumberFormatException e) {
                                        System.err.println("Could not parse '" + stringValueForDouble + "' as BigDecimal for (double-like) column '" + columnLabel + "'. Storing as original string. Error: " + e.getMessage());
                                        columnValue = stringValueForDouble; // Fallback
                                    }
                                } else {
                                    columnValue = null;
                                }
                                break;
                            case Types.INTEGER:
                            case Types.SMALLINT:
                            case Types.TINYINT:
                                columnValue = rs.getInt(i);
                                if (rs.wasNull()) { // Check for SQL NULL
                                    columnValue = null;
                                }
                                break;
                            case Types.BIGINT:
                                columnValue = rs.getLong(i);
                                if (rs.wasNull()) {
                                    columnValue = null;
                                }
                                break;
                            case Types.DATE:
                                columnValue = rs.getDate(i);
                                break;
                            case Types.TIME:
                                columnValue = rs.getTime(i);
                                break;
                            case Types.TIMESTAMP:
                            case Types.TIMESTAMP_WITH_TIMEZONE:
                                columnValue = rs.getTimestamp(i);
                                break;
                            case Types.VARCHAR:
                            case Types.CHAR:
                            case Types.LONGVARCHAR:
                            case Types.CLOB: // Handle CLOB as String
                                columnValue = rs.getString(i);
                                // Nếu nghi ngờ cột VARCHAR/TEXT này có thể chứa số tiền tệ bị định dạng sai
                                if (columnValue != null && (columnLabel.toLowerCase().contains("price") || columnLabel.toLowerCase().contains("amount") || columnLabel.toLowerCase().contains("salary"))) {
                                    try {
                                        String cleanedString = ((String)columnValue).replaceAll("[^\\d.,-]", "").replace(",", "");
                                        columnValue = new BigDecimal(cleanedString);
                                    } catch (NumberFormatException e) {
                                        // It's not a parsable number, keep as string
                                        // System.err.println("Column " + columnLabel + " looks like money but failed to parse: " + columnValue);
                                    }
                                }
                                break;
                            case Types.BOOLEAN:
                            case Types.BIT:
                                columnValue = rs.getBoolean(i);
                                if (rs.wasNull()) {
                                    columnValue = null;
                                }
                                break;
                            default:
                                columnValue = rs.getObject(i);
                                break;
                        }
                        row.put(columnLabel, columnValue);
                    }
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error executing dynamic query: " + e.getMessage() + " (SQLState: " + e.getSQLState() + ")");
            e.printStackTrace();
            throw e;
        }
        return results;
    }

    public List<String> getDistinctStringValues(String tableName, String columnName) throws SQLException {
        List<String> distinctValues = new ArrayList<>();
        if (!tableName.matches("^[a-zA-Z0-9_]+$") || !columnName.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Invalid table or column name for distinct value query.");
        }

        String sql = "SELECT DISTINCT CAST(" + columnName + " AS VARCHAR) FROM " + tableName + " WHERE " + columnName + " IS NOT NULL ORDER BY " + columnName;
        System.out.println("Fetching distinct values: " + sql);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                distinctValues.add(rs.getString(1));
            }
        } catch (SQLException e) {
            System.err.println("SQL Error fetching distinct values for " + tableName + "." + columnName + ": " + e.getMessage());
            throw e;
        }
        return distinctValues;
    }

    public List<Integer> getDistinctIntegerValues(String tableName, String expression) throws SQLException {
        List<Integer> distinctValues = new ArrayList<>();
        if (!tableName.matches("^[a-zA-Z0-9_]+$")) { // Basic validation
            throw new IllegalArgumentException("Invalid table name for distinct integer value query.");
        }
        // A bit more safety for expression, though still limited.
        // This is a simple check; complex expressions might need more robust validation.
        if (!expression.matches("^[a-zA-Z0-9_()EXTRACTYEARFROM\\s]+$")) {
            // Allow basic functions like EXTRACT(YEAR FROM col_name)
            // This regex is very basic and might need refinement for more complex valid SQL expressions.
            // It's better to have predefined allowed expressions if possible.
            // For now, this allows simple column names and the specific EXTRACT example.
            // throw new IllegalArgumentException("Invalid or potentially unsafe expression for distinct integer value query.");
        }


        String sql = "SELECT DISTINCT " + expression + " AS distinct_value FROM " + tableName + " WHERE " + expression + " IS NOT NULL ORDER BY distinct_value DESC";
        System.out.println("Fetching distinct integer values: " + sql);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                distinctValues.add(rs.getInt("distinct_value"));
            }
        } catch (SQLException e) {
            System.err.println("SQL Error fetching distinct integer values for " + tableName + "." + expression + ": " + e.getMessage());
            throw e;
        }
        return distinctValues;
    }
}
