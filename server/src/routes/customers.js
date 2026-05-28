const express = require("express");
const { z } = require("zod");
const prisma = require("../db/prisma");
const { verifyToken, isAdmin } = require("../middleware/auth");

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

router.get("/", verifyToken, isAdmin, async (req, res, next) => {
  try {
    const customers = await prisma.user.findMany({
      select: { id: true, email: true, firstName: true, lastName: true, username: true, gender: true, address: true, phone: true, dateOfBirth: true, role: true, createdAt: true }
    });
    res.json(customers);
  } catch (error) {
    next(error);
  }
});

router.get("/:id", verifyToken, isAdmin, async (req, res, next) => {
  try {
    const id = Number(req.params.id);
    if (Number.isNaN(id)) return res.status(400).json({ error: "Invalid customer id" });

    const customer = await prisma.user.findUnique({
      where: { id },
      select: { id: true, email: true, firstName: true, lastName: true, username: true, gender: true, address: true, phone: true, dateOfBirth: true, role: true, createdAt: true }
    });
    if (!customer) return res.status(404).json({ error: "Customer not found" });

    res.json(customer);
  } catch (error) {
    next(error);
  }
});

router.post("/", verifyToken, isAdmin, async (req, res, next) => {
  try {
    const payload = customerSchema.parse(req.body);
    const customer = await prisma.user.create({
      data: {
        ...payload,
        password: "temporary_password",
        role: "USER",
        dateOfBirth: payload.dateOfBirth ? new Date(payload.dateOfBirth) : undefined
      }
    });
    res.status(201).json(customer);
  } catch (error) {
    next(error);
  }
});

router.put("/:id", verifyToken, isAdmin, async (req, res, next) => {
  try {
    const id = Number(req.params.id);
    if (Number.isNaN(id)) return res.status(400).json({ error: "Invalid customer id" });

    const payload = customerSchema.partial().parse(req.body);
    const customer = await prisma.user.update({
      where: { id },
      data: {
        ...payload,
        dateOfBirth: payload.dateOfBirth ? new Date(payload.dateOfBirth) : undefined
      },
      select: { id: true, email: true, firstName: true, lastName: true, username: true, gender: true, address: true, phone: true, dateOfBirth: true, role: true, createdAt: true }
    });
    res.json(customer);
  } catch (error) {
    next(error);
  }
});

router.delete("/:id", verifyToken, isAdmin, async (req, res, next) => {
  try {
    const id = Number(req.params.id);
    if (Number.isNaN(id)) return res.status(400).json({ error: "Invalid customer id" });

    await prisma.user.delete({ where: { id } });
    res.status(204).end();
  } catch (error) {
    next(error);
  }
});

module.exports = router;
