SET client_encoding = 'UTF8';

-- Xóa các bảng cũ nếu tồn tại (theo thứ tự ngược lại để tránh lỗi khóa ngoại)
DROP TABLE IF EXISTS ORDER_DETAILS CASCADE;
DROP TABLE IF EXISTS ORDERS CASCADE;
DROP TABLE IF EXISTS PAYMENTS CASCADE;
DROP TABLE IF EXISTS PRODUCTS CASCADE;
DROP TABLE IF EXISTS EMPLOYEES CASCADE;
DROP TABLE IF EXISTS CUSTOMERS CASCADE;
DROP TABLE IF EXISTS categories CASCADE; -- 'categories' viết thường như DDL gốc

-- Bảng CATEGORIES
CREATE TABLE categories ( -- Viết thường 'categories' để khớp với DDL gốc của bạn nếu CSDL phân biệt chữ hoa/thường
    category_id SERIAL PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

-- Bảng CUSTOMERS
CREATE TABLE CUSTOMERS (
    customer_id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    gender CHAR(1) CHECK (gender IN ('F', 'M', 'O')),
    address VARCHAR(255),
    date_of_birth DATE,
    phone VARCHAR(20)
);

-- Bảng EMPLOYEES
CREATE TABLE EMPLOYEES (
    employee_id SERIAL PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone VARCHAR(20) UNIQUE,
    address VARCHAR(255),
    gender CHAR(1) CHECK (gender IN ('F', 'M', 'O')),
    bank_number VARCHAR(20),
    role VARCHAR(50) NOT NULL,
    salary NUMERIC(19, 2) NOT NULL,
    work_day VARCHAR(200),
    hire_day DATE NOT NULL,
    email VARCHAR(255) UNIQUE -- Đã thêm cột email
);

-- Bảng PRODUCTS
CREATE TABLE PRODUCTS (
    product_id SERIAL PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    model VARCHAR(100) NOT NULL,
    brand VARCHAR(100) NOT NULL,
    description TEXT,
    price NUMERIC(19, 2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    year_publish TIMESTAMP,
    category_id INTEGER,                -- Đã thêm cột category_id
    CONSTRAINT fk_product_category FOREIGN KEY (category_id)
        REFERENCES categories (category_id) -- Tham chiếu đến 'categories' viết thường
        ON DELETE SET NULL ON UPDATE CASCADE
);

-- Bảng PAYMENTS
CREATE TABLE PAYMENTS (
    payment_id SERIAL PRIMARY KEY,
    employee_id INTEGER,
    payment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payment_method VARCHAR(50) NOT NULL,
    total_amount NUMERIC(19, 2) NOT NULL,
    notes TEXT,                         -- Đã thêm cột notes
    CONSTRAINT fk_employee_payment FOREIGN KEY (employee_id)
        REFERENCES EMPLOYEES (employee_id)
        ON DELETE SET NULL ON UPDATE CASCADE
);

-- Bảng ORDERS
CREATE TABLE ORDERS (
    order_id SERIAL PRIMARY KEY,
    customer_id INTEGER NOT NULL,
    payment_id INTEGER,
    order_date DATE NOT NULL DEFAULT CURRENT_DATE,
    status VARCHAR(50) NOT NULL CHECK (status IN ('Pending', 'Processing', 'Shipped', 'Delivered', 'Cancelled', 'Returned')),
    net_amount NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    tax NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    total_amount NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    shipping_address VARCHAR(255),      -- Đã thêm cột shipping_address
    notes TEXT,                         -- Đã thêm cột notes
    CONSTRAINT fk_customer_order FOREIGN KEY (customer_id)
        REFERENCES CUSTOMERS (customer_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_payment_order FOREIGN KEY (payment_id)
        REFERENCES PAYMENTS (payment_id)
        ON DELETE SET NULL ON UPDATE CASCADE
);

-- Bảng ORDER_DETAILS
CREATE TABLE ORDER_DETAILS (
    od_id SERIAL PRIMARY KEY,
    order_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(19, 2) NOT NULL, -- Đã thêm cột unit_price
    CONSTRAINT fk_order_details_order FOREIGN KEY (order_id)
        REFERENCES ORDERS (order_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_order_details_product FOREIGN KEY (product_id)
        REFERENCES PRODUCTS (product_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

-- (Tùy chọn) Tạo INDEX để tăng tốc độ truy vấn thường xuyên
CREATE INDEX IF NOT EXISTS idx_products_category_id ON PRODUCTS(category_id);
CREATE INDEX IF NOT EXISTS idx_products_product_type ON PRODUCTS(product_type);
CREATE INDEX IF NOT EXISTS idx_orders_customer_id ON ORDERS(customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_payment_id ON ORDERS(payment_id);
CREATE INDEX IF NOT EXISTS idx_order_details_order_id ON ORDER_DETAILS(order_id);
CREATE INDEX IF NOT EXISTS idx_order_details_product_id ON ORDER_DETAILS(product_id);
CREATE INDEX IF NOT EXISTS idx_payments_employee_id ON PAYMENTS(employee_id);
CREATE INDEX IF NOT EXISTS idx_employees_email ON EMPLOYEES(email);
CREATE INDEX IF NOT EXISTS idx_customers_email ON CUSTOMERS(email);
CREATE INDEX IF NOT EXISTS idx_customers_username ON CUSTOMERS(username);
