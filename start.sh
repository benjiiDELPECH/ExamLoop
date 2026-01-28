#!/bin/bash
set -e

echo "Building ExamLoop API..."
cd services/api
mvn clean package -DskipTests
cd ../..

echo "Starting Docker Compose..."
docker compose up --build
