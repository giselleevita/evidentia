#!/bin/bash

# Evidentia - Stop Script
# Stops all backend services and Docker containers

echo "🛑 Stopping Evidentia services..."
echo ""

# Kill backend service processes
echo "Stopping backend services..."
pkill -f "rating-service:bootRun" 2>/dev/null && echo "  ✅ Rating Service stopped" || echo "  ⚠️  Rating Service not running"
pkill -f "evidence-service:bootRun" 2>/dev/null && echo "  ✅ Evidence Service stopped" || echo "  ⚠️  Evidence Service not running"
pkill -f "audit-log-service:bootRun" 2>/dev/null && echo "  ✅ Audit Service stopped" || echo "  ⚠️  Audit Service not running"
pkill -f "incident-service:bootRun" 2>/dev/null && echo "  ✅ Incident Service stopped" || echo "  ⚠️  Incident Service not running"

echo ""
echo "Stopping Docker containers..."
cd infra/docker
docker-compose down
echo "  ✅ Docker containers stopped"

echo ""
echo "✅ All services stopped!"
