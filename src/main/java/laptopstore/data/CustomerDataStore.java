package laptopstore.data;

import laptopstore.LaptopStoreApplication;
import laptopstore.model.Customer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CustomerDataStore {
    public static void saveToJson() {
        JSONArray jsonArray = new JSONArray();
        for (Customer customer : LaptopStoreApplication.customers) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("customerId", customer.getCustomerId());
            jsonObject.put("username", customer.getUsername());
            jsonObject.put("email", customer.getEmail());
            jsonObject.put("firstName", customer.getFirstName());
            jsonObject.put("lastName", customer.getLastName());
            jsonObject.put("createdAt", customer.getCreatedAt().toString());
            jsonObject.put("gender", String.valueOf(customer.getGender()));
            jsonObject.put("address", customer.getAddress());
            jsonObject.put("dateOfBirth", customer.getDateOfBirth() != null ? customer.getDateOfBirth().toString() : "");
            jsonObject.put("phone", customer.getPhone());
            jsonArray.put(jsonObject);
        }

        try (FileWriter file = new FileWriter("customers.json")) {
            file.write(jsonArray.toString(4));
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addCustomer(Customer customer) {
        LaptopStoreApplication.customers.add(customer);
        saveToJson();
    }

    public static void updateCustomer(Customer customer) {
        for (int i = 0; i < LaptopStoreApplication.customers.size(); i++) {
            if (LaptopStoreApplication.customers.get(i).getCustomerId() == customer.getCustomerId()) {
                LaptopStoreApplication.customers.set(i, customer);
                break;
            }
        }
        saveToJson();
    }

    public static void deleteCustomer(int customerId) {
        LaptopStoreApplication.customers.removeIf(c -> c.getCustomerId() == customerId);
        saveToJson();
    }

    public static int getNextCustomerId() {
        int maxId = 0;
        for (Customer customer : LaptopStoreApplication.customers) {
            maxId = Math.max(maxId, customer.getCustomerId());
        }
        return maxId + 1;
    }
}