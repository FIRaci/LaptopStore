const { test, expect } = require('@playwright/test');

test.describe('LaptopStore E2E Shopping Flow', () => {
  test('User can browse products, add to cart, and checkout', async ({ page }) => {
    // 1. Visit homepage
    await page.goto('/');
    
    // Ensure the page title and hero are visible
    await expect(page).toHaveTitle(/LaptopStore/);
    await expect(page.locator('h1')).toContainText('Build your dream setup');

    // 2. Verify products loaded from the API
    // The products are dynamically loaded into #productGrid
    const productCards = page.locator('.product-card');
    await expect(productCards.first()).toBeVisible();

    // 3. Add first product to the cart
    const firstProductAddToCart = productCards.nth(0).locator('button', { hasText: 'Add' });
    await firstProductAddToCart.click();

    // Verify cart count increments
    const cartCount = page.locator('#cartCount');
    await expect(cartCount).toHaveText('1');

    // Add second product to the cart
    const secondProductAddToCart = productCards.nth(1).locator('button', { hasText: 'Add' });
    await secondProductAddToCart.click();
    await expect(cartCount).toHaveText('2');

    // 4. Open cart and verify total
    const cartToggle = page.locator('#cartToggle');
    await cartToggle.click();

    const cartPanel = page.locator('#cartPanel');
    await expect(cartPanel).toHaveClass(/open/);

    // Verify total is populated
    const cartTotal = page.locator('#cartTotal');
    await expect(cartTotal).not.toContainText('$0.00');

    // 5. Fill out checkout form
    await page.locator('input[name="firstName"]').fill('Jane');
    await page.locator('input[name="lastName"]').fill('Doe');
    await page.locator('input[name="email"]').fill('jane@example.com');
    await page.locator('input[name="phone"]').fill('1234567890');
    await page.locator('input[name="address"]').fill('123 Test St');
    
    // Select payment method
    await page.locator('select[name="method"]').selectOption('CARD');

    // 6. Submit Order
    const placeOrderBtn = page.locator('#checkoutForm button[type="submit"]');
    await placeOrderBtn.click();

    // 7. Verify Success
    await expect(page.locator('.toast').filter({ hasText: /Success! Order #\d+ placed./ })).toBeVisible();

    // Verify cart is empty afterwards
    await expect(cartCount).toHaveText('0');
  });
});
