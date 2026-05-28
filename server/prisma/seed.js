const { PrismaClient } = require("@prisma/client");
const bcrypt = require("bcryptjs");
const prisma = new PrismaClient();

async function main() {
  // Reset data
  await prisma.orderItem.deleteMany({});
  await prisma.order.deleteMany({});
  await prisma.user.deleteMany({});
  await prisma.product.deleteMany({});
  console.log("Deleted old data.");

  // Create Users
  const adminPassword = await bcrypt.hash("admin123", 10);
  await prisma.user.create({
    data: {
      email: "admin@laptopstore.com",
      password: adminPassword,
      role: "ADMIN",
      firstName: "Admin",
      lastName: "Super"
    }
  });

  const userPassword = await bcrypt.hash("user123", 10);
  await prisma.user.create({
    data: {
      email: "user@laptopstore.com",
      password: userPassword,
      role: "USER",
      firstName: "Test",
      lastName: "User"
    }
  });
  console.log("Created Admin and User accounts.");


  const products = [
    { name: "Zephyr Pro 14", brand: "ASUS", description: "OLED + RTX Studio, made for motion and 4K timelines.", price: 1499.0, stock: 50, imageUrl: "/images/creator_laptop.png", type: "LAPTOP" },
    { name: "Titanium X17", brand: "MSI", description: "Ultimate desktop replacement. RTX 4090, 240Hz Mini-LED display.", price: 3299.0, stock: 12, imageUrl: "/images/gaming_laptop.png", type: "LAPTOP" },
    { name: "Aero Stealth 15", brand: "GIGABYTE", description: "Ultra-thin gaming machine with subtle aesthetics. RTX 4070.", price: 1899.0, stock: 25, imageUrl: "/images/gaming_laptop.png", type: "LAPTOP" },
    { name: "Creator Studio Pro", brand: "Lenovo", description: "Color-accurate 4K OLED, dual Thunderbolt 4, 64GB RAM.", price: 2499.0, stock: 18, imageUrl: "/images/creator_laptop.png", type: "LAPTOP" },
    { name: "NovaBook Air", brand: "LG", description: "Fanless, silent, all-day battery life for daily tasks.", price: 999.0, stock: 100, imageUrl: "/images/creator_laptop.png", type: "LAPTOP" },
    { name: "CyberType Mech 80", brand: "Razer", description: "TKL mechanical keyboard with hot-swappable switches and PBT keycaps.", price: 149.0, stock: 80, imageUrl: "/images/mech_keyboard.png", type: "GEAR" },
    { name: "Apex Wireless Mouse", brand: "Logitech", description: "Ultralight 55g gaming mouse. 30K DPI optical sensor.", price: 129.0, stock: 60, imageUrl: "/images/gaming_mouse.png", type: "GEAR" },
    { name: "Nova Studio Headphones", brand: "Sony", description: "ANC over-ear headphones with studio-grade drivers and 40h battery.", price: 299.0, stock: 45, imageUrl: "/images/creator_laptop.png", type: "GEAR" },
    { name: "RGB Desk Mat Pro", brand: "SteelSeries", description: "900x400mm waterproof desk mat with 360-degree edge lighting.", price: 49.0, stock: 150, imageUrl: "/images/mech_keyboard.png", type: "GEAR" },
    { name: "Streamer Mic X", brand: "Blue", description: "Cardioid condenser microphone with built-in pop filter and RGB.", price: 159.0, stock: 35, imageUrl: "/images/gaming_mouse.png", type: "GEAR" },
    { name: "RTX 4080 Super", brand: "NVIDIA", description: "Next-gen graphics card for 4K gaming and 3D rendering.", price: 999.0, stock: 20, imageUrl: "/images/gaming_laptop.png", type: "COMPONENT" },
    { name: "Core i9-14900K", brand: "Intel", description: "24-core processor reaching up to 6.0 GHz.", price: 589.0, stock: 40, imageUrl: "/images/creator_laptop.png", type: "COMPONENT" },
    { name: "2TB Gen4 NVMe SSD", brand: "Samsung", description: "7400MB/s read speeds for instant load times.", price: 149.0, stock: 120, imageUrl: "/images/gaming_mouse.png", type: "COMPONENT" },
    { name: "32GB DDR5 RAM Kit", brand: "Corsair", description: "6000MHz CL30 RAM for optimal gaming performance.", price: 119.0, stock: 75, imageUrl: "/images/mech_keyboard.png", type: "COMPONENT" },
    { name: "OLED Gaming Monitor 27\"", brand: "LG", description: "1440p, 240Hz OLED panel with 0.03ms response time.", price: 899.0, stock: 15, imageUrl: "/images/creator_laptop.png", type: "COMPONENT" },
    { name: "850W Gold PSU", brand: "Corsair", description: "Fully modular power supply with quiet fan curve.", price: 129.0, stock: 50, imageUrl: "/images/gaming_laptop.png", type: "COMPONENT" }
  ];

  for (let i = 0; i < products.length; i++) {
    const p = products[i];
    await prisma.product.create({
      data: {
        ...p,
        sku: `SKU-${Date.now()}-${i}`
      }
    });
  }

  console.log(`Seeded ${products.length} products successfully.`);
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
