# Base image
FROM node:20-alpine

# Set working directory
WORKDIR /app

# Copy package.json and install dependencies
COPY package.json package-lock.json* ./
RUN npm install

# Copy the rest of the project
COPY . .

# Build the app
RUN npm run build

# Start the Next.js app
EXPOSE 3000
CMD ["npm", "start"]
