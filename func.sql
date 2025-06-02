-- Drop các function nếu đã tồn tại
DROP FUNCTION IF EXISTS update_payment_total(INTEGER);
DROP FUNCTION IF EXISTS update_all_payments_totals();

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

-- Thực hiện cập nhật cho tất cả payments
SELECT update_all_payments_totals();
