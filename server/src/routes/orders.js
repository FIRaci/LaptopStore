const express = require("express");
const { z } = require("zod");
const prisma = require("../db/prisma");

const { verifyToken, isAdmin } = require("../middleware/auth");

const router = express.Router();

const orderSchema = z.object({
  customer: z.object({
    email: z.string().email(),
    firstName: z.string().min(1),
    lastName: z.string().min(1),
    phone: z.string().optional(),
    address: z.string().optional()
  }),
  items: z
    .array(
      z.object({
        productId: z.coerce.number().int().positive(),
        quantity: z.coerce.number().int().positive()
      })
    )
    .min(1)
});

router.post("/", async (req, res, next) => {
  try {
    const payload = orderSchema.parse(req.body);
    const productIds = payload.items.map((item) => item.productId);
    const uniqueProductIds = [...new Set(productIds)];

    const products = await prisma.product.findMany({
      where: { id: { in: uniqueProductIds } }
    });

    if (products.length !== uniqueProductIds.length) {
      return res.status(400).json({ error: "One or more products not found" });
    }

    const productMap = new Map(products.map((p) => [p.id, p]));

    const order = await prisma.$transaction(async (tx) => {
      const customer = await tx.user.upsert({
        where: { email: payload.customer.email },
        update: {
          firstName: payload.customer.firstName,
          lastName: payload.customer.lastName,
          phone: payload.customer.phone || null,
          address: payload.customer.address || null
        },
        create: {
          email: payload.customer.email,
          password: "guest_password", // Guest checkout default
          firstName: payload.customer.firstName,
          lastName: payload.customer.lastName,
          phone: payload.customer.phone || null,
          address: payload.customer.address || null
        }
      });

      const orderItems = payload.items.map((item) => {
        const product = productMap.get(item.productId);
        return {
          productId: item.productId,
          quantity: item.quantity,
          unitPrice: product.price
        };
      });

      const netAmount = orderItems.reduce((sum, item) => sum + (item.unitPrice * item.quantity), 0);
      const tax = 0; // Simplified for this example
      const totalAmount = netAmount + tax;

      const created = await tx.order.create({
        data: {
          userId: customer.id,
          netAmount,
          tax,
          totalAmount,
          orderItems: {
            create: orderItems
          }
        }
      });

      return tx.order.findUnique({
        where: { id: created.id },
        include: {
          orderItems: true,
          user: true,
          payment: true
        }
      });
    });

    res.status(201).json(order);
  } catch (error) {
    next(error);
  }
});

router.get("/my-orders", verifyToken, async (req, res, next) => {
  try {
    const orders = await prisma.order.findMany({
      where: { userId: req.user.id },
      include: {
        orderItems: {
          include: {
            product: true
          }
        },
        payment: true
      },
      orderBy: { createdAt: "desc" }
    });
    res.json(orders);
  } catch (error) {
    next(error);
  }
});

router.get("/:id", async (req, res, next) => {
  try {
    const id = Number(req.params.id);
    if (Number.isNaN(id)) {
      return res.status(400).json({ error: "Invalid order id" });
    }

    const order = await prisma.order.findUnique({
      where: { id },
      include: {
        orderItems: true,
        user: true,
        payment: true
      }
    });

    if (!order) {
      return res.status(404).json({ error: "Order not found" });
    }

    res.json(order);
  } catch (error) {
    next(error);
  }
});

router.get("/", verifyToken, isAdmin, async (req, res, next) => {
  try {
    const orders = await prisma.order.findMany({
      include: {
        orderItems: true,
        user: true,
        payment: true
      },
      orderBy: { createdAt: "desc" }
    });
    res.json(orders);
  } catch (error) {
    next(error);
  }
});

router.put("/:id/status", verifyToken, isAdmin, async (req, res, next) => {
  try {
    const id = Number(req.params.id);
    if (Number.isNaN(id)) return res.status(400).json({ error: "Invalid order id" });

    const statusSchema = z.object({
      status: z.enum(["PENDING", "PAID", "SHIPPED", "DELIVERED", "CANCELED"])
    });
    
    const { status } = statusSchema.parse(req.body);

    const order = await prisma.order.update({
      where: { id },
      data: { status },
      include: {
        orderItems: true,
        user: true,
        payment: true
      }
    });

    res.json(order);
  } catch (error) {
    next(error);
  }
});

module.exports = router;
