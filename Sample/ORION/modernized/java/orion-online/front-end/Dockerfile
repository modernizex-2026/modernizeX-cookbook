# Frontend build + serve — vite build rồi nginx serve dist, proxy /api sang backend service.
FROM node:20-alpine AS build
WORKDIR /fe
COPY package*.json ./
# npm ci đòi package-lock.json — generator không emit lock (không chạy npm lúc gen).
# Có lock thì ci (reproducible), không thì install.
RUN if [ -f package-lock.json ]; then npm ci --no-audit --no-fund; else npm install --no-audit --no-fund; fi
COPY . .
RUN npm run build
FROM nginx:alpine
COPY --from=build /fe/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
