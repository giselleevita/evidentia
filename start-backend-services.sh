#!/bin/bash

# Start all backend services

echo "🚀 Starting Backend Services..."
echo ""

cd "$(dirname "$0")"

# Start Evidence Service
echo "1. Starting Evidence Service (port 8080)..."
./gradlew :backend:evidence-service:bootRun > /tmp/evidence-service.log 2>&1 &
EVIDENCE_PID=$!
echo "   PID: $EVIDENCE_PID (logs: tail -f /tmp/evidence-service.log)"

# Wait a bit
sleep 3

# Start Rating Service (if not already running)
if ! curl -s http://localhost:8082/actuator/health > /dev/null 2>&1; then
    echo "2. Starting Rating Service (port 8082)..."
    ./gradlew :backend:rating-service:bootRun > /tmp/rating-service.log 2>&1 &
    RATING_PID=$!
    echo "   PID: $RATING_PID (logs: tail -f /tmp/rating-service.log)"
else
    echo "2. Rating Service already running ✅"
fi

# Wait a bit
sleep 3

# Start Audit Log Service
if ! curl -s http://localhost:8081/actuator/health > /dev/null 2>&1; then
    echo "3. Starting Audit Log Service (port 8081)..."
    ./gradlew :backend:audit-log-service:bootRun > /tmp/audit-service.log 2>&1 &
    AUDIT_PID=$!
    echo "   PID: $AUDIT_PID (logs: tail -f /tmp/audit-service.log)"
else
    echo "3. Audit Log Service already running ✅"
fi

echo ""
echo "✅ Backend services starting..."
echo ""
echo "Wait about 30 seconds for services to fully start, then check:"
echo "  - Evidence: http://localhost:8080/actuator/health"
echo "  - Rating: http://localhost:8082/actuator/health"
echo "  - Audit: http://localhost:8081/actuator/health"
echo ""
echo "View logs:"
echo "  tail -f /tmp/evidence-service.log"
echo "  tail -f /tmp/rating-service.log"
echo "  tail -f /tmp/audit-service.log"
