#!/bin/bash

echo "Starting Varta Chat Server..."
echo "Building project..."
mvn clean compile

echo "Starting Spring Boot application..."
mvn spring-boot:run