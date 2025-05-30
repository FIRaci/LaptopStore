package laptopstore.model; // Hoặc package phù hợp của bro

public record PredefinedQuery(String displayName, String sqlQuery) {
    @Override
    public String toString() {
        // Điều này quan trọng để JComboBox hiển thị displayName thay vì thông tin object
        return displayName;
    }
}
