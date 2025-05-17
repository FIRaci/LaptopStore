package laptopstore.data;

import laptopstore.LaptopStoreApplication;
import laptopstore.model.Order;
import laptopstore.model.OrderItem;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;

public class OrderDataStore {
    public static void saveToJson() {
        JSONObject jsonObject = new JSONObject();

        // Lưu orders
        JSONArray ordersArray = new JSONArray();
        for (Order order : LaptopStoreApplication.orders) {
            JSONObject orderJson = new JSONObject();
            orderJson.put("orderId", order.getOrderId());
            orderJson.put("customerId", order.getCustomerId());
            orderJson.put("paymentId", order.getPaymentId());
            orderJson.put("orderDate", order.getOrderDate().toString());
            orderJson.put("status", order.getStatus());
            orderJson.put("netAmount", order.getNetAmount());
            orderJson.put("tax", order.getTax());
            orderJson.put("totalAmount", order.getTotalAmount());

            // Lưu orderItems
            JSONArray itemsArray = new JSONArray();
            for (OrderItem item : order.getOrderItems()) {
                JSONObject itemJson = new JSONObject();
                itemJson.put("odId", item.getOdId());
                itemJson.put("orderId", item.getOrderId());
                itemJson.put("productId", item.getProductId());
                itemJson.put("quantity", item.getQuantity());
                itemJson.put("unitPrice", item.getUnitPrice());
                itemsArray.put(itemJson);
            }
            orderJson.put("orderItems", itemsArray);
            ordersArray.put(orderJson);
        }
        jsonObject.put("orders", ordersArray);

        try (FileWriter file = new FileWriter("orders.json")) {
            file.write(jsonObject.toString(4));
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addOrder(Order order) {
        LaptopStoreApplication.orders.add(order);
        for (OrderItem item : order.getOrderItems()) {
            item.setOdId(getNextOdId());
            LaptopStoreApplication.orderItems.add(item);
        }
        saveToJson();
    }

    public static void updateOrder(Order order) {
        for (int i = 0; i < LaptopStoreApplication.orders.size(); i++) {
            if (LaptopStoreApplication.orders.get(i).getOrderId() == order.getOrderId()) {
                LaptopStoreApplication.orders.set(i, order);
                break;
            }
        }
        // Cập nhật orderItems
        LaptopStoreApplication.orderItems.removeIf(item -> item.getOrderId() == order.getOrderId());
        for (OrderItem item : order.getOrderItems()) {
            item.setOdId(getNextOdId());
            LaptopStoreApplication.orderItems.add(item);
        }
        saveToJson();
    }

    public static void deleteOrder(int orderId) {
        LaptopStoreApplication.orders.removeIf(o -> o.getOrderId() == orderId);
        LaptopStoreApplication.orderItems.removeIf(item -> item.getOrderId() == orderId);
        saveToJson();
    }

    public static int getNextOrderId() {
        int maxId = 0;
        for (Order order : LaptopStoreApplication.orders) {
            maxId = Math.max(maxId, order.getOrderId());
        }
        return maxId + 1;
    }

    public static int getNextOdId() {
        int maxId = 0;
        for (OrderItem item : LaptopStoreApplication.orderItems) {
            maxId = Math.max(maxId, item.getOdId());
        }
        return maxId + 1;
    }
}