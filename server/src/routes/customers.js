const express = require("express");
const { z } = require("zod");
const prisma = require("../db/prisma");

const router = express.Router();

const customerSchema = z.object({
  email: z.string().email(),
  firstName: z.string().min(1),
  lastName: z.string().min(1),
  username: z.string().optional(),
  gender: z.string().optional(),
  address: z.string().optional(),
  phone: z.string().optional(),
  dateOfBirth: z.string().datetime().optional()
});

router.get("/", async (req, res, next) => {
  try {
    const customers = await prisma.customer.findMany();
    res.json(customers);
  } catch (error) {
    next(error);
  }
});

router.get("/:id", async (req, res, next) => {
  try {
    const id = Number(req.params.id);
    if (Number.isNaN(id)) return res.status(400).json({ error: "Invalid customer id" });

    const customer = await prisma.customer.findUnique({ where: { id } });
    if (!customer) return res.status(404).json({ error: "Customer not found" });

    res.json(customer);
  } catch (error) {
    next(error);
  }
});

router.post("/", async (req, res, next) => {
  try {
    const payload = customerSchema.parse(req.body);
    const customer = await prisma.customer.create({
      data: {
        ...payload,
        dateOfBirth: payload.dateOfBirth ? new Date(payload.dateOfBirth) : undefined
      }
    });
    res.status(201).json(customer);
  } catch (error) {
    next(error);
  }
});

router.put("/:id", async (req, res, next) => {
  try {
    const id = Number(req.params.id);
    if (Number.isNaN(id)) return res.status(400).json({ error: "Invalid customer id" });

    const payload = customerSchema.partial().parse(req.body);
    const customer = await prisma.customer.update({
      where: { id },
      data: {
        ...payload,
        dateOfBirth: payload.dateOfBirth ? new Date(payload.dateOfBirth) : undefined
      }
    });
    res.json(customer);
  } catch (error) {
    next(error);
  }
});

router.delete("/:id", async (req, res, next) => {
  try {
    const id = Number(req.params.id);
    if (Number.isNaN(id)) return res.status(400).json({ error: "Invalid customer id" });

    await prisma.customer.delete({ where: { id } });
    res.status(204).end();
  } catch (error) {
    next(error);
  }
});

module.exports = router;
