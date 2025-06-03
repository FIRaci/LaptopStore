-- PRODUCTS QUERIES

-- Medium 1: Top 20 sản phẩm bán chạy
WITH ProductSales AS (
    SELECT 
        p.product_name,
        SUM(od.quantity) as quantity_sold,
        SUM(od.quantity * p.price) as revenue,
        c.category_name
    FROM PRODUCTS p
    JOIN ORDER_DETAILS od ON p.product_id = od.product_id
    JOIN ORDERS o ON od.order_id = o.order_id
    JOIN categories c ON p.category_id = c.category_id
    WHERE o.order_date BETWEEN '2024-03-01' AND '2024-07-01'
    GROUP BY p.product_name, c.category_name
    ORDER BY revenue DESC
    LIMIT 20
)
SELECT * FROM ProductSales;

-- Hard 2: Phân tích xu hướng bán hàng
WITH MonthlyRevenue AS (
    SELECT 
        c.category_name,
        DATE_TRUNC('month', o.order_date) as month,
        SUM(od.quantity * p.price) as monthly_revenue
    FROM categories c
    JOIN PRODUCTS p ON c.category_id = p.category_id
    JOIN ORDER_DETAILS od ON p.product_id = od.product_id
    JOIN ORDERS o ON od.order_id = o.order_id
    WHERE o.order_date BETWEEN '2024-07-01' AND '2024-12-31'
    GROUP BY c.category_name, DATE_TRUNC('month', o.order_date)
),
GrowthRateCalc AS (
    SELECT 
        category_name,
        month,
        monthly_revenue - LAG(monthly_revenue) OVER (PARTITION BY category_name ORDER BY month) as growth
    FROM MonthlyRevenue
),
GrowthRate AS (
    SELECT 
        category_name,
        ARRAY_AGG(growth) as monthly_growth
    FROM GrowthRateCalc
    GROUP BY category_name
),
TopProducts AS (
    SELECT 
        category_name,
        ARRAY_AGG(product_name) as top_products
    FROM (
        SELECT 
            c.category_name,
            p.product_name,
            ROW_NUMBER() OVER (PARTITION BY c.category_name ORDER BY SUM(od.quantity * p.price) DESC) as rn
        FROM categories c
        JOIN PRODUCTS p ON c.category_id = p.category_id
        JOIN ORDER_DETAILS od ON p.product_id = od.product_id
        JOIN ORDERS o ON od.order_id = o.order_id
        WHERE o.order_date BETWEEN '2024-07-01' AND '2024-12-31'
        GROUP BY c.category_name, p.product_name
    ) ranked
    WHERE rn <= 3
    GROUP BY category_name
),
TopEmployees AS (
    SELECT 
        category_name,
        ARRAY_AGG(employee_name ORDER BY total_amount DESC) as best_employees
    FROM (
        SELECT 
            c.category_name,
            e.first_name || ' ' || e.last_name as employee_name,
            SUM(o.total_amount) as total_amount,
            ROW_NUMBER() OVER (PARTITION BY c.category_name ORDER BY SUM(o.total_amount) DESC) as rn
        FROM categories c
        JOIN PRODUCTS p ON c.category_id = p.category_id
        JOIN ORDER_DETAILS od ON p.product_id = od.product_id
        JOIN ORDERS o ON od.order_id = o.order_id
        JOIN PAYMENTS py ON o.payment_id = py.payment_id
        JOIN EMPLOYEES e ON py.employee_id = e.employee_id
        WHERE o.order_date BETWEEN '2024-07-01' AND '2024-12-31'
        GROUP BY c.category_name, e.first_name, e.last_name
    ) ranked
    WHERE rn <= 3
    GROUP BY category_name
)
SELECT 
    gr.category_name,
    gr.monthly_growth,
    tp.top_products,
    te.best_employees,
    SUM(mr.monthly_revenue) as total_revenue
FROM GrowthRate gr
JOIN TopProducts tp ON gr.category_name = tp.category_name
JOIN TopEmployees te ON gr.category_name = te.category_name
JOIN MonthlyRevenue mr ON gr.category_name = mr.category_name
GROUP BY gr.category_name, gr.monthly_growth, tp.top_products, te.best_employees;

-- CUSTOMERS QUERIES

-- Medium 1: Khách hàng 18-25 tuổi mua nhiều
SELECT 
    c.first_name || ' ' || c.last_name as customer_name,
    EXTRACT(YEAR FROM AGE(CURRENT_DATE, c.date_of_birth)) as age,
    COUNT(DISTINCT o.order_id) as total_orders,
    SUM(o.total_amount) as total_spent,
    MAX(o.order_date) as last_order_date
FROM CUSTOMERS c
JOIN ORDERS o ON c.customer_id = o.customer_id
WHERE EXTRACT(YEAR FROM AGE(CURRENT_DATE, c.date_of_birth)) BETWEEN 18 AND 30
AND o.order_date BETWEEN '2024-02-01' AND '2024-08-31'
GROUP BY c.customer_id, c.first_name, c.last_name, c.date_of_birth
HAVING COUNT(DISTINCT o.order_id) >= 5;

-- Hard 2: Dự đoán mua hàng
WITH CustomerCategorySpending AS (
    SELECT 
        c.customer_id,
        c.first_name || ' ' || c.last_name as customer_name,
        cat.category_name,
        SUM(od.quantity * p.price) as total_spent,
        ROW_NUMBER() OVER (PARTITION BY cat.category_name ORDER BY SUM(od.quantity * p.price) DESC) as rn
    FROM CUSTOMERS c
    JOIN ORDERS o ON c.customer_id = o.customer_id
    JOIN ORDER_DETAILS od ON o.order_id = od.order_id
    JOIN PRODUCTS p ON od.product_id = p.product_id
    JOIN CATEGORIES cat ON p.category_id = cat.category_id
    WHERE o.order_date BETWEEN '2024-01-01' AND '2024-12-31'
    GROUP BY c.customer_id, c.first_name, c.last_name, cat.category_name
)
SELECT 
    category_name,
    customer_name,
    total_spent
FROM CustomerCategorySpending
WHERE rn = 1
ORDER BY total_spent DESC
LIMIT 15;

-- ORDERS QUERIES

-- Medium 1: Đơn hàng giá trị cao nhất mỗi tháng
WITH MonthlyMaxOrders AS (
    SELECT DISTINCT ON (DATE_TRUNC('month', order_date))
        o.order_id,
        o.order_date,
        o.total_amount,
        c.first_name || ' ' || c.last_name as customer_name,
        e.first_name || ' ' || e.last_name as employee_name,
        p.payment_method
    FROM ORDERS o
    JOIN CUSTOMERS c ON o.customer_id = c.customer_id
    JOIN PAYMENTS p ON o.payment_id = p.payment_id
    JOIN EMPLOYEES e ON p.employee_id = e.employee_id
    WHERE EXTRACT(YEAR FROM o.order_date) = 2024
    ORDER BY DATE_TRUNC('month', order_date), o.total_amount DESC
)
SELECT * FROM MonthlyMaxOrders ORDER BY order_date;

-- Hard 2: Đơn hàng chưa thanh toán
SELECT o.*
FROM ORDERS o
JOIN CUSTOMERS c ON o.customer_id = c.customer_id
WHERE o.payment_id IS NULL 
AND c.gender = 'M'
AND (c.first_name ILIKE '%z%' OR c.last_name ILIKE '%z%');

-- EMPLOYEES QUERIES

-- Medium 1: Nhân viên chưa bán Apple iMac
SELECT DISTINCT e.*
FROM EMPLOYEES e
WHERE e.employee_id NOT IN (
    SELECT DISTINCT p.employee_id
    FROM PAYMENTS p
    JOIN ORDERS o ON p.payment_id = o.payment_id
    JOIN ORDER_DETAILS od ON o.order_id = od.order_id
    JOIN PRODUCTS pr ON od.product_id = pr.product_id
    WHERE pr.product_name = 'Apple iMac 24 M3'
    AND EXTRACT(MONTH FROM o.order_date) = 4
    AND EXTRACT(YEAR FROM o.order_date) = 2024
);

-- Hard 2: Top 10 nhân viên hiệu suất cao
SELECT 
    e.first_name || ' ' || e.last_name as employee_name,
    COUNT(p.payment_id)::FLOAT / NULLIF(('2024-12-31'::DATE - e.hire_day), 0) as efficiency_rate
FROM EMPLOYEES e
LEFT JOIN PAYMENTS p ON e.employee_id = p.employee_id
GROUP BY e.employee_id, e.first_name, e.last_name, e.hire_day
ORDER BY efficiency_rate DESC
LIMIT 10;

-- PAYMENTS QUERIES

-- Medium 1: Top 10 payment nhiều order
SELECT 
    p.payment_id,
    p.payment_method,
    COUNT(o.order_id) as order_count
FROM PAYMENTS p
LEFT JOIN ORDERS o ON p.payment_id = o.payment_id
GROUP BY p.payment_id, p.payment_method
ORDER BY order_count DESC
LIMIT 10;

-- Hard 2: Payment 1 order của nhân viên lương cao
WITH TopSalaryEmployee AS (
    SELECT employee_id
    FROM EMPLOYEES
    ORDER BY salary DESC
    LIMIT 1
)
SELECT p.*
FROM PAYMENTS p
JOIN TopSalaryEmployee tse ON p.employee_id = tse.employee_id
WHERE (
    SELECT COUNT(order_id)
    FROM ORDERS
    WHERE payment_id = p.payment_id
) = 1;
