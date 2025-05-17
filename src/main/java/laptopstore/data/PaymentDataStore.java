package laptopstore.data;

import laptopstore.LaptopStoreApplication;
import laptopstore.model.Payment;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;

public class PaymentDataStore {
    public static void saveToJson() {
        JSONArray jsonArray = new JSONArray();
        for (Payment payment : LaptopStoreApplication.payments) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("paymentId", payment.getPaymentId());
            jsonObject.put("employeeId", payment.getEmployeeId());
            jsonObject.put("paymentDate", payment.getPaymentDate().toString());
            jsonObject.put("totalAmount", payment.getTotalAmount());
            jsonObject.put("paymentMethod", payment.getPaymentMethod());
            jsonObject.put("status", payment.getStatus());
            jsonArray.put(jsonObject);
        }

        try (FileWriter file = new FileWriter("payments.json")) {
            file.write(jsonArray.toString(4));
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addPayment(Payment payment) {
        LaptopStoreApplication.payments.add(payment);
        saveToJson();
    }

    public static void updatePayment(Payment payment) {
        for (int i = 0; i < LaptopStoreApplication.payments.size(); i++) {
            if (LaptopStoreApplication.payments.get(i).getPaymentId() == payment.getPaymentId()) {
                LaptopStoreApplication.payments.set(i, payment);
                break;
            }
        }
        saveToJson();
    }

    public static void deletePayment(int paymentId) {
        LaptopStoreApplication.payments.removeIf(p -> p.getPaymentId() == paymentId);
        saveToJson();
    }

    public static int getNextPaymentId() {
        int maxId = 0;
        for (Payment payment : LaptopStoreApplication.payments) {
            maxId = Math.max(maxId, payment.getPaymentId());
        }
        return maxId + 1;
    }
}