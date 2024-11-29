# Step 1: Use Node.js as the base image for building the React app
FROM node:18-alpine AS build

# Step 2: Set the working directory inside the container
WORKDIR /app

# Step 3: Copy package.json and package-lock.json to install dependencies
COPY package*.json ./

# Step 4: Install the app dependencies
RUN npm install --legacy-peer-deps

# Step 5: Copy the rest of the app's code into the container
COPY . .

# Step 6: Build the React app
RUN npm run build

# Step 7: Use Nginx as the base image to serve the React app
FROM nginx:stable-alpine

# Step 8: Copy the build output to Nginx's HTML directory
COPY --from=build /app/build /usr/share/nginx/html

# Step 9: Expose the port Nginx will run on
EXPOSE 80

# Step 10: Start Nginx server
CMD ["nginx", "-g", "daemon off;"]

