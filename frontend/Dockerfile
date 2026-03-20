# Build the React/Vite application
FROM node:22-alpine AS builder

WORKDIR /app

# Copy package files and install dependencies first (caches this step)
COPY package*.json ./
RUN npm ci

# Copy the rest of the source code
COPY . .

# Build the project (Vite outputs to the /dist folder by default)
RUN npm run build

# Serve the application with Nginx
FROM nginx:alpine

# Remove the default Nginx index.html
RUN rm -rf /usr/share/nginx/html/*

# Copy our custom routing configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy the compiled static files from the builder stage
COPY --from=builder /app/dist /usr/share/nginx/html

# Expose port 80 to the outside world
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]