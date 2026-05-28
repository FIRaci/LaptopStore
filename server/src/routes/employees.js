const express = require("express");
const { z } = require("zod");
const prisma = require("../db/prisma");

const router = express.Router();

const employeeSchema = z.object({
  firstName: z.string().min(1),
  lastName: z.string().min(1),
  phone: z.string().optional(),
  address: z.string().optional(),
  role: z.string().optional(),
  salary: z.coerce.number().positive().optional(),
  hireDate: z.string().datetime().optional()
});

router.get("/", async (req, res, next) => {
  try {
    const employees = await prisma.employee.findMany();
    res.json(employees);
  } catch (error) {
    next(error);
  }
});

router.get("/:id", async (req, res, next) => {
  try {
    const id = Number(req.params.id);
    if (Number.isNaN(id)) return res.status(400).json({ error: "Invalid employee id" });

    const employee = await prisma.employee.findUnique({ where: { id } });
    if (!employee) return res.status(404).json({ error: "Employee not found" });

    res.json(employee);
  } catch (error) {
    next(error);
  }
});

router.post("/", async (req, res, next) => {
  try {
    const payload = employeeSchema.parse(req.body);
    const employee = await prisma.employee.create({
      data: {
        ...payload,
        hireDate: payload.hireDate ? new Date(payload.hireDate) : undefined
      }
    });
    res.status(201).json(employee);
  } catch (error) {
    next(error);
  }
});

router.put("/:id", async (req, res, next) => {
  try {
    const id = Number(req.params.id);
    if (Number.isNaN(id)) return res.status(400).json({ error: "Invalid employee id" });

    const payload = employeeSchema.partial().parse(req.body);
    const employee = await prisma.employee.update({
      where: { id },
      data: {
        ...payload,
        hireDate: payload.hireDate ? new Date(payload.hireDate) : undefined
      }
    });
    res.json(employee);
  } catch (error) {
    next(error);
  }
});

router.delete("/:id", async (req, res, next) => {
  try {
    const id = Number(req.params.id);
    if (Number.isNaN(id)) return res.status(400).json({ error: "Invalid employee id" });

    await prisma.employee.delete({ where: { id } });
    res.status(204).end();
  } catch (error) {
    next(error);
  }
});

module.exports = router;
