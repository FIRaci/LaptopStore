const express = require("express");
const { z } = require("zod");
const prisma = require("../db/prisma");

const router = express.Router();

const paymentSchema = z.object({
  orderId: z.coerce.number().int().positive(),
  amount: z.coerce.number().positive(),
  method: z.enum(["CASH", "CARD", "BANK_TRANSFER", "COD"]),
  status: z.enum(["PENDING", "PAID", "CANCELED", "REFUNDED"]).default("PAID")
});

router.post("/", async (req, res, next) => {
  try {
    const payload = paymentSchema.parse(req.body);

    const order = await prisma.order.findUnique({
      where: { id: payload.orderId }
    });

    if (!order) {
      return res.status(404).json({ error: "Order not found" });
    }

    if (["CANCELED", "DELIVERED"].includes(order.status)) {
      return res.status(409).json({ error: "Order is not payable" });
    }

    const orderAmount = Number(order.totalAmount);
    if (Math.abs(orderAmount - payload.amount) > 0.01) {
      return res.status(409).json({ error: "Payment amount mismatch" });
    }

    const existing = await prisma.payment.findUnique({
      where: { orderId: payload.orderId }
    });

    if (existing && existing.status === "PAID") {
      return res.status(409).json({ error: "Payment already completed" });
    }

    const payment = existing
      ? await prisma.payment.update({
          where: { id: existing.id },
          data: {
            amount: order.totalAmount,
            method: payload.method,
            status: payload.status,
            paidAt: payload.status === "PAID" ? new Date() : null
          }
        })
      : await prisma.payment.create({
          data: {
            orderId: payload.orderId,
            amount: order.totalAmount,
            method: payload.method,
            status: payload.status,
            paidAt: payload.status === "PAID" ? new Date() : null
          }
        });

    res.status(201).json(payment);
  } catch (error) {
    next(error);
  }
});

router.get("/", async (req, res, next) => {
  try {
    const payments = await prisma.payment.findMany({
      include: {
        order: true,
        employee: true
      },
      orderBy: { createdAt: "desc" }
    });
    res.json(payments);
  } catch (error) {
    next(error);
  }
});

router.put("/:id/status", async (req, res, next) => {
  try {
    const id = Number(req.params.id);
    if (Number.isNaN(id)) return res.status(400).json({ error: "Invalid payment id" });

    const statusSchema = z.object({
      status: z.enum(["PENDING", "PAID", "CANCELED", "REFUNDED"])
    });
    
    const { status } = statusSchema.parse(req.body);

    const payment = await prisma.payment.update({
      where: { id },
      data: { 
        status,
        paidAt: status === "PAID" ? new Date() : null
      },
      include: {
        order: true,
        employee: true
      }
    });

    res.json(payment);
  } catch (error) {
    next(error);
  }
});

module.exports = router;
