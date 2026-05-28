const express = require("express");
const { Prisma } = require("@prisma/client");
const { z } = require("zod");
const prisma = require("../db/prisma");
const { verifyToken, isAdmin } = require("../middleware/auth");

const router = express.Router();

const productSchema = z.object({
  sku: z.string().min(1),
  name: z.string().min(1),
  brand: z.string().min(1),
  description: z.string().optional(),
  imageUrl: z.string().optional(),
  type: z.enum(["LAPTOP", "GEAR", "COMPONENT"]),
  price: z.coerce.number().positive(),
  stock: z.coerce.number().int().nonnegative()
});

router.get("/", async (req, res, next) => {
  try {
    const { q, type, minPrice, maxPrice, sort } = req.query;
    const where = {};
    const allowedTypes = new Set(["LAPTOP", "GEAR", "COMPONENT"]);

    if (q) {
      where.OR = [
        { name: { contains: q, mode: "insensitive" } },
        { brand: { contains: q, mode: "insensitive" } }
      ];
    }

    if (type) {
      const normalized = String(type).toUpperCase();
      if (!allowedTypes.has(normalized)) {
        return res.status(400).json({ error: "Invalid product type" });
      }
      where.type = normalized;
    }

    if (minPrice || maxPrice) {
      where.price = {};
      if (minPrice) {
        const parsed = Number(minPrice);
        if (Number.isNaN(parsed)) {
          return res.status(400).json({ error: "Invalid minPrice" });
        }
        where.price.gte = new Prisma.Decimal(parsed);
      }
      if (maxPrice) {
        const parsed = Number(maxPrice);
        if (Number.isNaN(parsed)) {
          return res.status(400).json({ error: "Invalid maxPrice" });
        }
        where.price.lte = new Prisma.Decimal(parsed);
      }
    }

    let orderBy;
    if (sort === "price_asc") {
      orderBy = { price: "asc" };
    } else if (sort === "price_desc") {
      orderBy = { price: "desc" };
    } else if (sort === "newest") {
      orderBy = { createdAt: "desc" };
    }

    const products = await prisma.product.findMany({
      where,
      orderBy
    });

    res.json(products);
  } catch (error) {
    next(error);
  }
});

router.get("/:id", async (req, res, next) => {
  try {
    const id = Number(req.params.id);
    if (Number.isNaN(id)) {
      return res.status(400).json({ error: "Invalid product id" });
    }

    const product = await prisma.product.findUnique({
      where: { id }
    });

    if (!product) {
      return res.status(404).json({ error: "Product not found" });
    }

    res.json(product);
  } catch (error) {
    next(error);
  }
});

router.post("/", verifyToken, isAdmin, async (req, res, next) => {
  try {
    const payload = productSchema.parse(req.body);
    const product = await prisma.product.create({ data: payload });
    res.status(201).json(product);
  } catch (error) {
    next(error);
  }
});

router.put("/:id", verifyToken, isAdmin, async (req, res, next) => {
  try {
    const id = Number(req.params.id);
    if (Number.isNaN(id)) return res.status(400).json({ error: "Invalid product id" });
    
    const payload = productSchema.partial().parse(req.body);
    const product = await prisma.product.update({
      where: { id },
      data: payload
    });
    res.json(product);
  } catch (error) {
    next(error);
  }
});

router.delete("/:id", verifyToken, isAdmin, async (req, res, next) => {
  try {
    const id = Number(req.params.id);
    if (Number.isNaN(id)) return res.status(400).json({ error: "Invalid product id" });
    
    await prisma.product.delete({ where: { id } });
    res.status(204).end();
  } catch (error) {
    next(error);
  }
});

module.exports = router;
