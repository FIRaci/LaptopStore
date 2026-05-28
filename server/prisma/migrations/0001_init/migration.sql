-- Enums
CREATE TYPE "ProductType" AS ENUM ('LAPTOP', 'GEAR', 'COMPONENT');
CREATE TYPE "OrderStatus" AS ENUM ('PENDING', 'PAID', 'SHIPPED', 'DELIVERED', 'CANCELED');
CREATE TYPE "PaymentStatus" AS ENUM ('PENDING', 'PAID', 'CANCELED', 'REFUNDED');
CREATE TYPE "PaymentMethod" AS ENUM ('CASH', 'CARD', 'BANK_TRANSFER', 'COD');

-- User (replaces Customer — matches Prisma schema)
CREATE TABLE "User" (
  "id" SERIAL PRIMARY KEY,
  "username" TEXT,
  "email" TEXT NOT NULL UNIQUE,
  "password" TEXT NOT NULL,
  "role" TEXT NOT NULL DEFAULT 'USER',
  "firstName" TEXT NOT NULL,
  "lastName" TEXT NOT NULL,
  "gender" TEXT,
  "address" TEXT,
  "dateOfBirth" TIMESTAMP,
  "phone" TEXT,
  "createdAt" TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE "Employee" (
  "id" SERIAL PRIMARY KEY,
  "firstName" TEXT NOT NULL,
  "lastName" TEXT NOT NULL,
  "phone" TEXT,
  "address" TEXT,
  "role" TEXT,
  "salary" NUMERIC(12,2),
  "hireDate" TIMESTAMP
);

CREATE TABLE "Product" (
  "id" SERIAL PRIMARY KEY,
  "sku" TEXT NOT NULL UNIQUE,
  "name" TEXT NOT NULL,
  "brand" TEXT NOT NULL,
  "description" TEXT,
  "imageUrl" TEXT,
  "type" "ProductType" NOT NULL,
  "price" NUMERIC(12,2) NOT NULL,
  "stock" INTEGER NOT NULL,
  "createdAt" TIMESTAMP NOT NULL DEFAULT NOW(),
  "updatedAt" TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE "Order" (
  "id" SERIAL PRIMARY KEY,
  "userId" INTEGER NOT NULL REFERENCES "User"("id") ON DELETE RESTRICT,
  "status" "OrderStatus" NOT NULL DEFAULT 'PENDING',
  "orderDate" TIMESTAMP NOT NULL DEFAULT NOW(),
  "netAmount" NUMERIC(12,2) NOT NULL DEFAULT 0,
  "tax" NUMERIC(12,2) NOT NULL DEFAULT 0,
  "totalAmount" NUMERIC(12,2) NOT NULL DEFAULT 0,
  "createdAt" TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE "OrderItem" (
  "id" SERIAL PRIMARY KEY,
  "orderId" INTEGER NOT NULL REFERENCES "Order"("id") ON DELETE CASCADE,
  "productId" INTEGER NOT NULL REFERENCES "Product"("id") ON DELETE RESTRICT,
  "quantity" INTEGER NOT NULL,
  "unitPrice" NUMERIC(12,2) NOT NULL
);

CREATE TABLE "Payment" (
  "id" SERIAL PRIMARY KEY,
  "orderId" INTEGER NOT NULL UNIQUE REFERENCES "Order"("id") ON DELETE CASCADE,
  "employeeId" INTEGER REFERENCES "Employee"("id") ON DELETE SET NULL,
  "amount" NUMERIC(12,2) NOT NULL,
  "method" "PaymentMethod" NOT NULL,
  "status" "PaymentStatus" NOT NULL,
  "paidAt" TIMESTAMP,
  "createdAt" TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX "Product_type_idx" ON "Product"("type");
CREATE INDEX "Product_brand_idx" ON "Product"("brand");
CREATE INDEX "Order_user_idx" ON "Order"("userId");
CREATE INDEX "Order_status_idx" ON "Order"("status");
CREATE INDEX "OrderItem_order_idx" ON "OrderItem"("orderId");
CREATE INDEX "OrderItem_product_idx" ON "OrderItem"("productId");
CREATE INDEX "Payment_status_idx" ON "Payment"("status");

-- Functions
CREATE OR REPLACE FUNCTION calc_order_totals(p_order_id INTEGER) RETURNS VOID AS $$
DECLARE
  v_net NUMERIC(12,2);
  v_tax NUMERIC(12,2);
BEGIN
  SELECT COALESCE(SUM(oi."quantity" * oi."unitPrice"), 0)
    INTO v_net
    FROM "OrderItem" oi
   WHERE oi."orderId" = p_order_id;

  v_tax := ROUND(v_net * 0.10, 2);

  UPDATE "Order"
     SET "netAmount" = v_net,
         "tax" = v_tax,
         "totalAmount" = v_net + v_tax
   WHERE "id" = p_order_id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION apply_stock_delta(p_product_id INTEGER, p_delta INTEGER) RETURNS VOID AS $$
DECLARE
  v_stock INTEGER;
BEGIN
  SELECT "stock" INTO v_stock
    FROM "Product"
   WHERE "id" = p_product_id
   FOR UPDATE;

  IF v_stock IS NULL THEN
    RAISE EXCEPTION 'Product % not found', p_product_id;
  END IF;

  IF (v_stock - p_delta) < 0 THEN
    RAISE EXCEPTION 'Insufficient stock for product %', p_product_id;
  END IF;

  UPDATE "Product"
     SET "stock" = "stock" - p_delta,
         "updatedAt" = NOW()
   WHERE "id" = p_product_id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION trg_order_items_stock_before() RETURNS TRIGGER AS $$
DECLARE
  v_delta INTEGER;
BEGIN
  IF TG_OP = 'INSERT' THEN
    v_delta := NEW."quantity";
    PERFORM apply_stock_delta(NEW."productId", v_delta);
    RETURN NEW;
  ELSIF TG_OP = 'UPDATE' THEN
    IF NEW."productId" = OLD."productId" THEN
      v_delta := NEW."quantity" - OLD."quantity";
      IF v_delta <> 0 THEN
        PERFORM apply_stock_delta(NEW."productId", v_delta);
      END IF;
    ELSE
      PERFORM apply_stock_delta(OLD."productId", -OLD."quantity");
      PERFORM apply_stock_delta(NEW."productId", NEW."quantity");
    END IF;
    RETURN NEW;
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION trg_order_items_stock_before_delete() RETURNS TRIGGER AS $$
BEGIN
  UPDATE "Product"
     SET "stock" = "stock" + OLD."quantity",
         "updatedAt" = NOW()
   WHERE "id" = OLD."productId";
  RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION trg_order_items_totals_after() RETURNS TRIGGER AS $$
BEGIN
  IF TG_OP = 'DELETE' THEN
    PERFORM calc_order_totals(OLD."orderId");
  ELSE
    PERFORM calc_order_totals(NEW."orderId");
  END IF;
  RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION trg_payment_status_after() RETURNS TRIGGER AS $$
BEGIN
  IF NEW."status" = 'PAID' THEN
    UPDATE "Order"
       SET "status" = 'PAID'
     WHERE "id" = NEW."orderId"
       AND "status" IN ('PENDING');
  ELSIF NEW."status" IN ('CANCELED', 'REFUNDED') THEN
    UPDATE "Order"
       SET "status" = 'CANCELED'
     WHERE "id" = NEW."orderId"
       AND "status" IN ('PENDING', 'PAID');
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers
CREATE TRIGGER "order_items_stock_before"
BEFORE INSERT OR UPDATE ON "OrderItem"
FOR EACH ROW EXECUTE FUNCTION trg_order_items_stock_before();

CREATE TRIGGER "order_items_stock_before_delete"
BEFORE DELETE ON "OrderItem"
FOR EACH ROW EXECUTE FUNCTION trg_order_items_stock_before_delete();

CREATE TRIGGER "order_items_totals_after"
AFTER INSERT OR UPDATE OR DELETE ON "OrderItem"
FOR EACH ROW EXECUTE FUNCTION trg_order_items_totals_after();

CREATE TRIGGER "payment_status_after"
AFTER INSERT OR UPDATE ON "Payment"
FOR EACH ROW EXECUTE FUNCTION trg_payment_status_after();
