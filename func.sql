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
