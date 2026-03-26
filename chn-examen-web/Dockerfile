FROM node:22-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM nginx:1.27-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf

# Limpiar los archivos por defecto de Nginx (para quitar el "Welcome to nginx")
RUN rm -rf /usr/share/nginx/html/*

COPY --from=builder /app/dist/chn-examen-web/browser /usr/share/nginx/html

# Renombrar el entrypoint del cliente (CSR) para que Nginx lo sirva
RUN mv /usr/share/nginx/html/index.csr.html /usr/share/nginx/html/index.html || true
