# ============================================================
# ETAPA 1: Compilar la aplicacion Angular
# ============================================================
FROM node:20-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build -- --configuration production

# ============================================================
# ETAPA 2: Servir los archivos estaticos con Nginx
# ============================================================
FROM nginx:1.27-alpine

COPY --from=build /app/dist/elpuntazo-frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
