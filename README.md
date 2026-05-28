# LaptopStore 🖥️

Full-stack laptop & gear store with **Java Swing** desktop admin + **Node.js** REST API + **PostgreSQL** database.

Built by **FIRaci**.

## Architecture

```
├── src/              # Java Swing desktop app (Maven)
│   └── laptopstore/  # Models, DataStores, Screens, TableModels
├── server/           # Node.js REST API (Express + Prisma)
│   ├── prisma/       # Schema, migrations, seed data
│   ├── src/
│   │   ├── routes/   # auth, products, orders, payments, customers, employees
│   │   ├── middleware/# JWT auth, role-based access
│   │   └── db/       # Prisma client
│   └── tests/        # Playwright E2E tests
├── web/              # Frontend (HTML/CSS/JS + Vite)
│   ├── admin/        # Admin dashboard (products, orders, stats)
│   ├── app.js        # Store frontend logic
│   └── styles.css    # Dark-themed UI
├── docker-compose.yml # PostgreSQL + API containers
└── pom.xml           # Maven build for Swing app
```

## Quick Start

```bash
# 1. Start PostgreSQL
docker-compose up -d db

# 2. Set env
$env:DATABASE_URL = "postgresql://postgres:postgres@localhost:5439/laptop_store?schema=public"
$env:JWT_SECRET = "your-secret-key"

# 3. Install & migrate
cd server
npm install
npx prisma migrate deploy
npm run prisma:seed

# 4. Start API
npm start

# 5. Open in browser
# http://localhost:3000
```

Or use `run.bat` for one-click setup.

## Test Accounts

| Role  | Email                   | Password  |
|-------|-------------------------|-----------|
| Admin | admin@laptopstore.com   | admin123  |
| User  | user@laptopstore.com    | user123   |

## API Endpoints

| Endpoint             | Method | Auth   | Description            |
|----------------------|--------|--------|------------------------|
| `/api/products`      | GET    | —      | List products (filter/sort) |
| `/api/products/:id`  | GET    | —      | Product detail         |
| `/api/products`      | POST   | Admin  | Create product         |
| `/api/orders`        | POST   | —      | Place order (guest)    |
| `/api/orders/my-orders` | GET | User | User's order history   |
| `/api/auth/register` | POST   | —      | Create account         |
| `/api/auth/login`    | POST   | —      | Sign in                |
| `/api/auth/me`       | GET    | User   | Current user profile   |

## Tech Stack

- **Desktop**: Java 17 + Swing + FlatLaf + JFreeChart
- **Backend**: Node.js + Express + Prisma ORM
- **Database**: PostgreSQL 15
- **Frontend**: Vanilla JS + Vite + CSS (dark theme)
- **Testing**: Playwright
