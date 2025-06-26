NQ21: Khách hàng có sinh nhật trong tháng hiện tại
SELECT customer_id, first_name, last_name, date_of_birth
FROM CUSTOMERS
WHERE EXTRACT(MONTH FROM date_of_birth) = EXTRACT(MONTH FROM CURRENT_DATE)
ORDER BY date_of_birth ASC;

NQ22: Tháng có doanh thu cao nhất 2024
-- Tham số (year = 2024)
SELECT EXTRACT(MONTH FROM order_date) AS month, SUM(total_amount) AS total_revenue
FROM ORDERS
WHERE EXTRACT(YEAR FROM order_date) = 2024 -- year
GROUP BY EXTRACT(MONTH FROM order_date)
ORDER BY total_revenue DESC
LIMIT 1;

NQ23: Sản phẩm có số lượng tồn kho thấp dưới 10
-- Tham số (stock_threshold = 10)
SELECT product_id, model, stock_quantity, price
FROM PRODUCTS
WHERE stock < 10 -- stock_threshold
ORDER BY price ASC;

NQ24: Nhân viên xử lý nhiều giao dịch thanh toán nhất
SELECT e.employee_id, e.first_name, e.last_name, COUNT(p.payment_id) AS payment_count
FROM EMPLOYEES e
JOIN PAYMENTS p ON e.employee_id = p.employee_id
GROUP BY e.employee_id, e.first_name, e.last_name
ORDER BY payment_count DESC
LIMIT 1;

NQ25: Khách hàng mua sản phẩm từ ít nhất 3 thương hiệu khác nhau
-- Tham số (min_brands = 3)
SELECT c.customer_id, c.first_name, c.last_name, COUNT(DISTINCT p.brand) AS brand_count
FROM CUSTOMERS c
JOIN ORDERS o ON c.customer_id = o.customer_id
JOIN ORDER_DETAILS od ON o.order_id = od.order_id
JOIN PRODUCTS p ON od.product_id = p.product_id
GROUP BY c.customer_id, c.first_name, c.last_name
HAVING COUNT(DISTINCT p.brand) >= 3 -- min_brands
ORDER BY brand_count DESC
LIMIT 5;

NQ26: Số lượng khách hàng mua theo từng thương hiệu
SELECT p.brand, COUNT(DISTINCT o.customer_id) AS customer_count
FROM ORDERS o
JOIN ORDER_DETAILS od ON o.order_id = od.order_id
JOIN PRODUCTS p ON od.product_id = p.product_id
GROUP BY p.brand
ORDER BY customer_count DESC;

NQ27: Sản phẩm chưa bao giờ được đặt
SELECT p.product_id, p.model
FROM PRODUCTS p
LEFT JOIN ORDER_DETAILS od ON p.product_id = od.product_id
WHERE od.order_id IS NULL
ORDER BY p.product_id;

NQ28: Top 3 khách hàng có tổng số tiền chi cao nhất
-- Tham số (top_n = 3)
SELECT c.customer_id, c.first_name, c.last_name, SUM(o.total_amount) AS total_spent
FROM CUSTOMERS c
JOIN ORDERS o ON c.customer_id = o.customer_id
GROUP BY c.customer_id, c.first_name, c.last_name
ORDER BY total_spent DESC
LIMIT 3 -- top_n
;

NQ29: Sản phẩm bán chạy nhất
SELECT p.product_id, p.model, SUM(od.quantity) AS total_sold
FROM PRODUCTS p
JOIN ORDER_DETAILS od ON p.product_id = od.product_id
GROUP BY p.product_id, p.model
ORDER BY total_sold DESC
LIMIT 1;

NQ30: Khách hàng chưa mua sản phẩm thuộc danh mục 'Office'
-- Tham số (category_name = 'Office')
SELECT c.customer_id, c.first_name, c.last_name
FROM CUSTOMERS c
WHERE NOT EXISTS (
    SELECT 1
    FROM ORDERS o
    JOIN ORDER_DETAILS od ON o.order_id = od.order_id
    JOIN PRODUCTS p ON od.product_id = p.product_id
    JOIN CATEGORIES cat ON p.category_id = cat.category_id
    WHERE o.customer_id = c.customer_id
    AND cat.category_name = 'Office' -- category_name
)
ORDER BY c.customer_id;