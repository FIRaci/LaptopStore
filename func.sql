-- Drop các function nếu đã tồn tại
DROP FUNCTION IF EXISTS update_order_total(INTEGER);
DROP FUNCTION IF EXISTS update_all_orders_totals();

DROP FUNCTION IF EXISTS update_payment_total(INTEGER);
DROP FUNCTION IF EXISTS update_all_payments_totals();

-- Function cập nhật total cho một order
CREATE OR REPLACE FUNCTION update_order_total(p_order_id INTEGER)
RETURNS BOOLEAN AS $$
DECLARE
    v_net_amount NUMERIC(19,2);
    v_tax NUMERIC(19,2);
    v_total_amount NUMERIC(19,2);
BEGIN
    -- Tính net_amount từ order_details và products
    SELECT COALESCE(SUM(od.quantity * p.price), 0)
    INTO v_net_amount
    FROM ORDER_DETAILS od
    JOIN PRODUCTS p ON od.product_id = p.product_id
    WHERE od.order_id = p_order_id;

    -- Tính tax (giả sử 10% của net_amount)
    v_tax := v_net_amount * 0.1;
    
    -- Tính total_amount
    v_total_amount := v_net_amount + v_tax;

    -- Cập nhật order
    UPDATE ORDERS
    SET net_amount = v_net_amount,
        tax = v_tax,
        total_amount = v_total_amount
    WHERE order_id = p_order_id;

    RETURN FOUND;
END;
$$ LANGUAGE plpgsql;

-- Function cập nhật total cho tất cả orders
CREATE OR REPLACE FUNCTION update_all_orders_totals()
RETURNS INTEGER AS $$
DECLARE
    v_order RECORD;
    updated_count INTEGER := 0;
BEGIN
    FOR v_order IN SELECT order_id FROM ORDERS
    LOOP
        IF update_order_total(v_order.order_id) THEN
            updated_count := updated_count + 1;
        END IF;
    END LOOP;
    
    RETURN updated_count;
END;
$$ LANGUAGE plpgsql;

-- Tạo function để cập nhật total_amount cho một payment cụ thể
CREATE OR REPLACE FUNCTION update_payment_total(p_payment_id INTEGER)
RETURNS BOOLEAN AS $$
BEGIN
    -- Cập nhật total_amount dựa trên tổng của các orders
    UPDATE PAYMENTS p 
    SET total_amount = COALESCE((
        SELECT SUM(o.total_amount)
        FROM ORDERS o 
        WHERE o.payment_id = p.payment_id
        GROUP BY o.payment_id
    ), 0)
    WHERE p.payment_id = p_payment_id;

    RETURN FOUND;
END;
$$ LANGUAGE plpgsql;

-- Tạo function để cập nhật total_amount cho tất cả payments
CREATE OR REPLACE FUNCTION update_all_payments_totals()
RETURNS INTEGER AS $$
DECLARE 
    updated_count INTEGER := 0;
BEGIN
    -- Cập nhật toàn bộ payments với tổng từ orders tương ứng
    WITH payment_totals AS (
        SELECT p.payment_id, COALESCE(SUM(o.total_amount), 0) as total
        FROM PAYMENTS p
        LEFT JOIN ORDERS o ON p.payment_id = o.payment_id
        GROUP BY p.payment_id
    )
    UPDATE PAYMENTS p
    SET total_amount = pt.total
    FROM payment_totals pt
    WHERE p.payment_id = pt.payment_id;

    GET DIAGNOSTICS updated_count = ROW_COUNT;
    RETURN updated_count;
END;
$$ LANGUAGE plpgsql;

-- 1. Trigger cập nhật stock_quantity khi thêm hoặc xoá ORDER_DETAILS

-- Giảm stock khi thêm ORDER_DETAILS
CREATE OR REPLACE FUNCTION trg_decrease_stock_on_insert()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE PRODUCTS
    SET stock_quantity = stock_quantity - NEW.quantity
    WHERE product_id = NEW.product_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_decrease_stock_on_insert
AFTER INSERT ON ORDER_DETAILS
FOR EACH ROW
EXECUTE FUNCTION trg_decrease_stock_on_insert();

-- Tăng stock khi xoá ORDER_DETAILS
CREATE OR REPLACE FUNCTION trg_increase_stock_on_delete()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE PRODUCTS
    SET stock_quantity = stock_quantity + OLD.quantity
    WHERE product_id = OLD.product_id;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_increase_stock_on_delete
AFTER DELETE ON ORDER_DETAILS
FOR EACH ROW
EXECUTE FUNCTION trg_increase_stock_on_delete();

-- 2. Trigger cập nhật total_amount của ORDER khi thêm, sửa, xoá ORDER_DETAILS

CREATE OR REPLACE FUNCTION trg_update_order_total()
RETURNS TRIGGER AS $$
DECLARE
    v_net NUMERIC(19,2);
    v_tax NUMERIC(19,2);
    v_total NUMERIC(19,2);
BEGIN
    SELECT COALESCE(SUM(od.quantity * p.price), 0)
    INTO v_net
    FROM ORDER_DETAILS od
    JOIN PRODUCTS p ON od.product_id = p.product_id
    WHERE od.order_id = COALESCE(NEW.order_id, OLD.order_id);

    v_tax := v_net * 0.1;
    v_total := v_net + v_tax;

    UPDATE ORDERS
    SET net_amount = v_net,
        tax = v_tax,
        total_amount = v_total
    WHERE order_id = COALESCE(NEW.order_id, OLD.order_id);

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Gọi khi INSERT, UPDATE, DELETE trên ORDER_DETAILS
CREATE TRIGGER trg_update_order_total_insert
AFTER INSERT ON ORDER_DETAILS
FOR EACH ROW
EXECUTE FUNCTION trg_update_order_total();

CREATE TRIGGER trg_update_order_total_update
AFTER UPDATE ON ORDER_DETAILS
FOR EACH ROW
EXECUTE FUNCTION trg_update_order_total();

CREATE TRIGGER trg_update_order_total_delete
AFTER DELETE ON ORDER_DETAILS
FOR EACH ROW
EXECUTE FUNCTION trg_update_order_total();

-- 3. Trigger cập nhật total_amount của PAYMENT khi có thay đổi ở ORDERS

CREATE OR REPLACE FUNCTION trg_update_payment_total()
RETURNS TRIGGER AS $$
DECLARE
    v_total NUMERIC(19,2);
BEGIN
    IF (TG_OP = 'UPDATE' OR TG_OP = 'INSERT') AND NEW.payment_id IS NOT NULL THEN
        SELECT COALESCE(SUM(total_amount), 0)
        INTO v_total
        FROM ORDERS
        WHERE payment_id = NEW.payment_id;

        UPDATE PAYMENTS
        SET total_amount = v_total
        WHERE payment_id = NEW.payment_id;
    END IF;

    IF TG_OP = 'UPDATE' AND OLD.payment_id IS NOT NULL AND OLD.payment_id <> NEW.payment_id THEN
        -- Nếu payment_id thay đổi, cập nhật payment cũ
        SELECT COALESCE(SUM(total_amount), 0)
        INTO v_total
        FROM ORDERS
        WHERE payment_id = OLD.payment_id;

        UPDATE PAYMENTS
        SET total_amount = v_total
        WHERE payment_id = OLD.payment_id;
    END IF;

    IF TG_OP = 'DELETE' AND OLD.payment_id IS NOT NULL THEN
        SELECT COALESCE(SUM(total_amount), 0)
        INTO v_total
        FROM ORDERS
        WHERE payment_id = OLD.payment_id;

        UPDATE PAYMENTS
        SET total_amount = v_total
        WHERE payment_id = OLD.payment_id;
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_payment_total_insert
AFTER INSERT ON ORDERS
FOR EACH ROW
EXECUTE FUNCTION trg_update_payment_total();

CREATE TRIGGER trg_update_payment_total_update
AFTER UPDATE ON ORDERS
FOR EACH ROW
EXECUTE FUNCTION trg_update_payment_total();

CREATE TRIGGER trg_update_payment_total_delete
AFTER DELETE ON ORDERS
FOR EACH ROW
EXECUTE FUNCTION trg_update_payment_total();


-- Thực hiện cập nhật theo thứ tự: orders trước, payments sau
SELECT update_all_orders_totals();
SELECT update_all_payments_totals();
