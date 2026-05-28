const { test, expect } = require("@playwright/test");

test.describe("Authentication & Admin Flow", () => {
  test("User can login and see name", async ({ page }) => {
    await page.goto("http://localhost:3000");

    // Click Sign In
    await page.click("#accountBtn");
    
    // Fill credentials
    await page.fill("#authEmail", "user@laptopstore.com");
    await page.fill("#authPassword", "user123");
    await page.click("#authSubmitBtn");

    // Check if account button changes to User's Name
    await expect(page.locator("#accountBtnText")).toHaveText("Test User");
    
    // Ensure Admin button is NOT visible
    await expect(page.locator("#adminBtn")).not.toBeVisible();
  });

  test("Admin can login, see admin panel, and add product", async ({ page }) => {
    await page.goto("http://localhost:3000");

    // Click Sign In
    await page.click("#accountBtn");
    
    // Fill credentials
    await page.fill("#authEmail", "admin@laptopstore.com");
    await page.fill("#authPassword", "admin123");
    await page.click("#authSubmitBtn");

    // Check if account button changes to Admin's Name
    await expect(page.locator("#accountBtnText")).toHaveText("Admin Super");
    
    // Ensure Admin button IS visible
    const adminBtn = page.locator("#adminBtn");
    await expect(adminBtn).toBeVisible();

    // Click Admin Button (navigate to admin index)
    await adminBtn.click();
    
    // Wait for Admin Dashboard to load
    await expect(page).toHaveURL(/.*\/admin\/index\.html/);

    // Navigate to Products
    await page.click("text=Products");
    await expect(page).toHaveURL(/.*\/admin\/products\.html/);

    // Click Add Product
    await page.click("#addProductBtn");
    await expect(page.locator("#addModal")).toHaveClass(/open/);

    // Fill new product form
    await page.fill("#addName", "Test Laptop X");
    await page.fill("#addSku", `TEST-X-${Date.now()}`);
    await page.fill("#addBrand", "TestBrand");
    await page.selectOption("#addType", "LAPTOP");
    await page.fill("#addPrice", "1299.99");
    await page.fill("#addStock", "10");
    await page.click("#addForm button[type='submit']");

    // Modal should close (or toast appears)
    await expect(page.locator(".toast").filter({ hasText: "Product added!" })).toBeVisible();
  });
});
