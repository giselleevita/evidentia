#!/bin/bash

# Evidentia - Easy Start Script
# This script starts the local infrastructure, backend services, and frontend.

set -e

echo "🚀 Evidentia - Easy Start"
echo "========================"
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker Desktop and try again."
    exit 1
fi

# Step 1: Start Docker containers
echo -e "${BLUE}Step 1: Starting databases and infrastructure...${NC}"
cd infra/docker

docker compose up -d
echo -e "${GREEN}✅ Infrastructure started${NC}"
echo ""

# Step 2: Wait for databases to be ready
echo -e "${BLUE}Step 2: Waiting for databases to be ready...${NC}"
sleep 5
echo -e "${GREEN}✅ Databases ready${NC}"
echo ""

# Step 3: Start backend services in background
cd ../..
echo -e "${BLUE}Step 3: Starting backend services...${NC}"

# Start Rating Service
echo "   Starting Rating Service (port 8082)..."
DATABASE_URL=jdbc:postgresql://localhost:5435/evidentia_rating gradle :backend:rating-service:bootRun > /tmp/rating-service.log 2>&1 &
RATING_PID=$!

# Start Evidence Service
echo "   Starting Evidence Service (port 8080)..."
DATABASE_URL=jdbc:postgresql://localhost:15432/evidentia_evidence gradle :backend:evidence-service:bootRun > /tmp/evidence-service.log 2>&1 &
EVIDENCE_PID=$!

# Start Audit Log Service
echo "   Starting Audit Log Service (port 8081)..."
DATABASE_URL=jdbc:postgresql://localhost:5433/evidentia_audit gradle :backend:audit-log-service:bootRun > /tmp/audit-service.log 2>&1 &
AUDIT_PID=$!

echo "   Starting Incident Service (port 8083)..."
DATABASE_URL=jdbc:postgresql://localhost:5434/evidentia_incident gradle :backend:incident-service:bootRun > /tmp/incident-service.log 2>&1 &
INCIDENT_PID=$!

echo "   Starting Integration Service (port 8084)..."
INTEGRATION_DB_URL=jdbc:postgresql://localhost:5436/evidentia_integration \
INTEGRATION_DB_USER=evidentia \
INTEGRATION_DB_PASS=evidentia \
gradle :backend:integration-service:bootRun > /tmp/integration-service.log 2>&1 &
INTEGRATION_PID=$!

echo -e "${GREEN}✅ Backend services starting (running in background)${NC}"
echo ""

# Step 4: Start frontend
echo -e "${BLUE}Step 4: Starting frontend...${NC}"
cd frontend/compliance-portal

# Install dependencies if needed
if [ ! -d "node_modules" ]; then
    echo "   Installing dependencies (first time setup)..."
    npm install --silent
fi

echo ""
echo "========================"
echo -e "${GREEN}✅ Everything is starting!${NC}"
echo "========================"
echo ""
echo "📍 Frontend: http://localhost:5173"
echo "📍 Rating Service: http://localhost:8082"
echo "📍 Evidence Service: http://localhost:8080"
echo "📍 Audit Log Service: http://localhost:8081"
echo "📍 Incident Service: http://localhost:8083"
echo "📍 Integration Service: http://localhost:8084"
echo ""
echo -e "${YELLOW}📝 Backend service logs:${NC}"
echo "   - Rating Service: tail -f /tmp/rating-service.log"
echo "   - Evidence Service: tail -f /tmp/evidence-service.log"
echo "   - Audit Service: tail -f /tmp/audit-service.log"
echo "   - Incident Service: tail -f /tmp/incident-service.log"
echo "   - Integration Service: tail -f /tmp/integration-service.log"
echo ""
echo "✨ Features:"
echo "   - Exit button in header (top-right)"
echo "   - Floating exit button (bottom-right)"
echo "   - Press Ctrl+Q or Alt+X to logout"
echo ""
echo -e "${YELLOW}⚠️  To stop everything:${NC}"
echo "   - Press Ctrl+C to stop frontend"
echo "   - Run: ./stop.sh (to stop backend services and Docker)"
echo ""
echo "Starting frontend server..."
echo ""

# Start frontend (this runs in foreground so user can see it)
npm run dev
