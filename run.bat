@echo off
title LaptopStore Local Server
echo ===================================================
echo        LaptopStore - Local Environment Setup
echo ===================================================
echo.

echo [1/3] Starting PostgreSQL Database via Docker...
docker-compose up -d db
echo.

echo [2/3] Installing Dependencies...
echo - Installing Server Dependencies...
cd server
call npm install
cd ..

echo - Installing Web Dependencies...
cd web
call npm install
cd ..
echo.

echo [3/3] Starting API and Vite Frontend...
echo Backend API: http://localhost:3000
echo Vite Web: http://localhost:5173
echo (Press CTRL+C to stop both servers)
echo.
call npx concurrently -k -n "API,WEB" -c "blue,green" "cd server && npm run start" "cd web && npm run dev"
pause
