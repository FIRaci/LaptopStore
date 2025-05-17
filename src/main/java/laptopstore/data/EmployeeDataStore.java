package laptopstore.data;

import laptopstore.LaptopStoreApplication;
import laptopstore.model.Employee;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;

public class EmployeeDataStore {
    public static void saveToJson() {
        JSONArray jsonArray = new JSONArray();
        for (Employee employee : LaptopStoreApplication.employees) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("employeeId", employee.getEmployeeId());
            jsonObject.put("firstName", employee.getFirstName());
            jsonObject.put("lastName", employee.getLastName());
            jsonObject.put("phone", employee.getPhone());
            jsonObject.put("address", employee.getAddress());
            jsonObject.put("gender", String.valueOf(employee.getGender()));
            jsonObject.put("bankNumber", employee.getBankNumber());
            jsonObject.put("role", employee.getRole());
            jsonObject.put("salary", employee.getSalary());
            jsonObject.put("workDay", employee.getWorkDay());
            jsonObject.put("hireDay", employee.getHireDay().toString());
            jsonArray.put(jsonObject);
        }

        try (FileWriter file = new FileWriter("employees.json")) {
            file.write(jsonArray.toString(4));
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addEmployee(Employee employee) {
        LaptopStoreApplication.employees.add(employee);
        saveToJson();
    }

    public static void updateEmployee(Employee employee) {
        for (int i = 0; i < LaptopStoreApplication.employees.size(); i++) {
            if (LaptopStoreApplication.employees.get(i).getEmployeeId() == employee.getEmployeeId()) {
                LaptopStoreApplication.employees.set(i, employee);
                break;
            }
        }
        saveToJson();
    }

    public static void deleteEmployee(int employeeId) {
        LaptopStoreApplication.employees.removeIf(e -> e.getEmployeeId() == employeeId);
        saveToJson();
    }

    public static int getNextEmployeeId() {
        int maxId = 0;
        for (Employee employee : LaptopStoreApplication.employees) {
            maxId = Math.max(maxId, employee.getEmployeeId());
        }
        return maxId + 1;
    }
}