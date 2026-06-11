#!/bin/bash

# Start all backend services

echo "🚀 Starting Backend Services..."
echo ""

cd "$(dirname "$0")"

# Start Evidence Service
echo "1. Starting Evidence Service (port 8080)..."
DATABASE_URL=jdbc:postgresql://localhost:15432/evidentia_evidence gradle :backend:evidence-service:bootRun > /tmp/evidence-service.log 2>&1 &
EVIDENCE_PID=$!
echo "   PID: $EVIDENCE_PID (logs: tail -f /tmp/evidence-service.log)"

# Wait a bit
sleep 3

# Start Rating Service (if not already running)
if ! curl -s http://localhost:8082/actuator/health > /dev/null 2>&1; then
    echo "2. Starting Rating Service (port 8082)..."
    DATABASE_URL=jdbc:postgresql://localhost:5435/evidentia_rating gradle :backend:rating-service:bootRun > /tmp/rating-service.log 2>&1 &
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
    DATABASE_URL=jdbc:postgresql://localhost:5433/evidentia_audit gradle :backend:audit-log-service:bootRun > /tmp/audit-service.log 2>&1 &
    AUDIT_PID=$!
    echo "   PID: $AUDIT_PID (logs: tail -f /tmp/audit-service.log)"
else
    echo "3. Audit Log Service already running ✅"
fi

if ! curl -s http://localhost:8083/actuator/health > /dev/null 2>&1; then
    echo "4. Starting Incident Service (port 8083)..."
    DATABASE_URL=jdbc:postgresql://localhost:5434/evidentia_incident gradle :backend:incident-service:bootRun > /tmp/incident-service.log 2>&1 &
    echo "   PID: $! (logs: tail -f /tmp/incident-service.log)"
else
    echo "4. Incident Service already running"
fi

if ! curl -s http://localhost:8084/actuator/health > /dev/null 2>&1; then
    echo "5. Starting Integration Service (port 8084)..."
    INTEGRATION_DB_URL=jdbc:postgresql://localhost:5436/evidentia_integration \
    INTEGRATION_DB_USER=evidentia \
    INTEGRATION_DB_PASS=evidentia \
    gradle :backend:integration-service:bootRun > /tmp/integration-service.log 2>&1 &
    echo "   PID: $! (logs: tail -f /tmp/integration-service.log)"
else
    echo "5. Integration Service already running"
fi

echo ""
echo "✅ Backend services starting..."
echo ""
echo "Wait about 30 seconds for services to fully start, then check:"
echo "  - Evidence: http://localhost:8080/actuator/health"
echo "  - Rating: http://localhost:8082/actuator/health"
echo "  - Audit: http://localhost:8081/actuator/health"
echo "  - Incident: http://localhost:8083/actuator/health"
echo "  - Integration: http://localhost:8084/actuator/health"
echo ""
echo "View logs:"
echo "  tail -f /tmp/evidence-service.log"
echo "  tail -f /tmp/rating-service.log"
echo "  tail -f /tmp/audit-service.log"
echo "  tail -f /tmp/incident-service.log"
echo "  tail -f /tmp/integration-service.log"
