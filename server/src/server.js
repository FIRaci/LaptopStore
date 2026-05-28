const path = require("path");
const express = require("express");
const cors = require("cors");
const { Prisma } = require("@prisma/client");
require("dotenv").config();

const productsRouter = require("./routes/products");
const ordersRouter = require("./routes/orders");
const paymentsRouter = require("./routes/payments");
const customersRouter = require("./routes/customers");
const employeesRouter = require("./routes/employees");
const authRouter = require("./routes/auth");

const app = express();
const port = Number(process.env.PORT) || 3000;

app.use(cors());
app.use(express.json({ limit: "1mb" }));

app.get("/api/health", (req, res) => {
  res.json({ status: "ok" });
});

app.use("/api/products", productsRouter);
app.use("/api/orders", ordersRouter);
app.use("/api/payments", paymentsRouter);
app.use("/api/customers", customersRouter);
app.use("/api/employees", employeesRouter);
app.use("/api/auth", authRouter);

const staticDir = path.resolve(__dirname, "../../web");
app.use(express.static(staticDir));

app.get("/", (req, res) => {
  res.sendFile(path.join(staticDir, "index.html"));
});

app.use((error, req, res, next) => {
  if (error?.name === "ZodError") {
    return res.status(400).json({
      error: "Invalid request",
      details: error.issues?.map((issue) => issue.message) || []
    });
  }

  if (error instanceof Prisma.PrismaClientKnownRequestError) {
    if (error.code === "P2002") {
      return res.status(409).json({ error: "Duplicate record" });
    }
    if (error.code === "P2025") {
      return res.status(404).json({ error: "Record not found" });
    }
  }

  if (error?.message?.includes("Insufficient stock")) {
    return res.status(409).json({ error: "Insufficient stock" });
  }

  const message = error?.message || "Internal server error";
  const status = error?.status || 500;
  return res.status(status).json({ error: message });
});

app.listen(port, () => {
  console.log(`LaptopStore API listening on :${port}`);
});
