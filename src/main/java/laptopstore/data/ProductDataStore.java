package laptopstore.data;

import laptopstore.LaptopStoreApplication;
import laptopstore.model.Components;
import laptopstore.model.Gear;
import laptopstore.model.Laptop;
import laptopstore.model.Product;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductDataStore {
    // Lưu tất cả danh sách vào JSON
    public static void saveToJson() {
        JSONObject jsonObject = new JSONObject();

        // Lưu products
        JSONArray productsArray = new JSONArray();
        for (Product product : LaptopStoreApplication.products) {
            JSONObject productJson = new JSONObject();
            productJson.put("productId", product.getProductId());
            productJson.put("model", product.getModel());
            productJson.put("brand", product.getBrand());
            productJson.put("description", product.getDescription());
            productJson.put("price", product.getPrice());
            productJson.put("stockQuantity", product.getStockQuantity());
            productJson.put("yearPublish", product.getYearPublish().toString());
            productsArray.put(productJson);
        }
        jsonObject.put("products", productsArray);

        // Lưu laptops
        JSONArray laptopsArray = new JSONArray();
        for (Laptop laptop : LaptopStoreApplication.laptops) {
            JSONObject laptopJson = new JSONObject();
            laptopJson.put("laptopId", laptop.getLaptopId());
            laptopJson.put("productId", laptop.getProductId());
            laptopJson.put("laptopName", laptop.getLaptopName());
            laptopsArray.put(laptopJson);
        }
        jsonObject.put("laptops", laptopsArray);

        // Lưu gears
        JSONArray gearsArray = new JSONArray();
        for (Gear gear : LaptopStoreApplication.gears) {
            JSONObject gearJson = new JSONObject();
            gearJson.put("gearId", gear.getGearId());
            gearJson.put("productId", gear.getProductId());
            gearJson.put("gearName", gear.getGearName());
            gearsArray.put(gearJson);
        }
        jsonObject.put("gears", gearsArray);

        // Lưu components
        JSONArray componentsArray = new JSONArray();
        for (Components component : LaptopStoreApplication.components) {
            JSONObject componentJson = new JSONObject();
            componentJson.put("componentId", component.getComponentId());
            componentJson.put("productId", component.getProductId());
            componentJson.put("componentName", component.getComponentName());
            componentsArray.put(componentJson);
        }
        jsonObject.put("components", componentsArray);

        try (FileWriter file = new FileWriter("products.json")) {
            file.write(jsonObject.toString(4));
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Thêm sản phẩm mới
    public static void addProduct(Product product, String type, String typeName) {
        LaptopStoreApplication.products.add(product);
        int nextId = getNextTypeId(type);
        if (type.equals("Laptop")) {
            LaptopStoreApplication.laptops.add(new Laptop(nextId, product.getProductId(), typeName));
        } else if (type.equals("Gear")) {
            LaptopStoreApplication.gears.add(new Gear(nextId, product.getProductId(), typeName));
        } else if (type.equals("Components")) {
            LaptopStoreApplication.components.add(new Components(nextId, product.getProductId(), typeName));
        }
        saveToJson();
    }

    // Sửa sản phẩm
    public static void updateProduct(Product product, String type, String typeName) {
        // Cập nhật Product
        for (int i = 0; i < LaptopStoreApplication.products.size(); i++) {
            if (LaptopStoreApplication.products.get(i).getProductId() == product.getProductId()) {
                LaptopStoreApplication.products.set(i, product);
                break;
            }
        }
        // Cập nhật type-specific
        if (type.equals("Laptop")) {
            for (Laptop laptop : LaptopStoreApplication.laptops) {
                if (laptop.getProductId() == product.getProductId()) {
                    laptop.setLaptopName(typeName);
                    break;
                }
            }
        } else if (type.equals("Gear")) {
            for (Gear gear : LaptopStoreApplication.gears) {
                if (gear.getProductId() == product.getProductId()) {
                    gear.setGearName(typeName);
                    break;
                }
            }
        } else if (type.equals("Components")) {
            for (Components component : LaptopStoreApplication.components) {
                if (component.getProductId() == product.getProductId()) {
                    component.setComponentName(typeName);
                    break;
                }
            }
        }
        saveToJson();
    }

    // Xóa sản phẩm
    public static void deleteProduct(int productId) {
        // Xóa Product
        LaptopStoreApplication.products.removeIf(p -> p.getProductId() == productId);
        // Xóa type-specific
        LaptopStoreApplication.laptops.removeIf(l -> l.getProductId() == productId);
        LaptopStoreApplication.gears.removeIf(g -> g.getProductId() == productId);
        LaptopStoreApplication.components.removeIf(c -> c.getProductId() == productId);
        saveToJson();
    }

    // Lấy ID tiếp theo cho type
    private static int getNextTypeId(String type) {
        int maxId = 0;
        if (type.equals("Laptop")) {
            for (Laptop laptop : LaptopStoreApplication.laptops) {
                maxId = Math.max(maxId, laptop.getLaptopId());
            }
        } else if (type.equals("Gear")) {
            for (Gear gear : LaptopStoreApplication.gears) {
                maxId = Math.max(maxId, gear.getGearId());
            }
        } else if (type.equals("Components")) {
            for (Components component : LaptopStoreApplication.components) {
                maxId = Math.max(maxId, component.getComponentId());
            }
        }
        return maxId + 1;
    }

    // Lấy ID sản phẩm tiếp theo
    public static int getNextProductId() {
        int maxId = 0;
        for (Product product : LaptopStoreApplication.products) {
            maxId = Math.max(maxId, product.getProductId());
        }
        return maxId + 1;
    }
}