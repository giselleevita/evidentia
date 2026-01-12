#!/bin/bash

# Quick start - Frontend only (for seeing UI improvements)
# Use this if you only want to see the frontend/logout features

set -e

echo "🚀 Starting Frontend Only"
echo "========================"
echo ""

cd frontend/compliance-portal || exit

# Install dependencies if needed
if [ ! -d "node_modules" ]; then
    echo "📦 Installing dependencies..."
    npm install
    echo ""
fi

# Check if port is in use
if lsof -Pi :5173 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
    echo "⚠️  Port 5173 is in use. Stopping existing process..."
    lsof -ti:5173 | xargs kill -9 2>/dev/null || true
    sleep 1
fi

echo "🔥 Starting frontend..."
echo ""
echo "📍 Frontend: http://localhost:5173"
echo ""
echo "✨ Logout Features:"
echo "   • Exit button in header (top-right)"
echo "   • Floating exit button (bottom-right - expands on hover)"
echo "   • Press Ctrl+Q (or Cmd+Q on Mac) to logout"
echo "   • Press Alt+X to logout"
echo ""
echo "Press Ctrl+C to stop"
echo "========================"
echo ""

npm run dev
