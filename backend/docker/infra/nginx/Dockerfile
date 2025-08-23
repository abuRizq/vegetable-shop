# syntax=docker/dockerfile:1.7
#
# NGINX reverse proxy for Vegetable Shop Backend
# - Copies a provided nginx config (server block) into the image.
# - Runs on Alpine, adds curl for simple healthcheck/debugging.
#
FROM nginx:1.27-alpine

# Optional: install curl for healthchecks / debugging
RUN apk add --no-cache curl

# Copy your nginx server config into the default conf.d location.
# Adjust the source path if your repo stores it elsewhere.
# Example source in this project: backend/config/local/nginx/default.conf
ARG NGINX_CONF=backend/config/local/nginx/default.conf
COPY ${NGINX_CONF} /etc/nginx/conf.d/default.conf

# Expose standard HTTP port
EXPOSE 80

# Healthcheck (assumes your nginx serves a simple 200 at / or /healthz)
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s   CMD curl -fsS http://127.0.0.1/ || exit 1

STOPSIGNAL SIGQUIT
CMD ["nginx","-g","daemon off;"]
