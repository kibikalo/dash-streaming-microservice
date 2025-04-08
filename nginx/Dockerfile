FROM nginx:stable-alpine

# Remove default Nginx configuration
RUN rm /etc/nginx/conf.d/default.conf

# Copy our custom configuration file
COPY nginx.conf /etc/nginx/nginx.conf

# Expose the port Nginx listens on inside the container
EXPOSE 80