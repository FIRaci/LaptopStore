FROM node:20-alpine

WORKDIR /app

COPY server/package.json server/package-lock.json* ./server/
RUN cd server && npm install

COPY server ./server
COPY web ./web

WORKDIR /app/server

ENV NODE_ENV=production
EXPOSE 3000

CMD ["node", "src/server.js"]
