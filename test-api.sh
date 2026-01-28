#!/bin/bash
# API Testing Script

API_URL="${API_URL:-http://localhost:8080}"
DEVICE_ID="test-device-$(date +%s)"

echo "Testing ExamLoop API at $API_URL"
echo "Device ID: $DEVICE_ID"
echo ""

# Test health check
echo "1. Testing health endpoint..."
curl -s "$API_URL/actuator/health" | grep -q "UP" && echo "✓ Health check passed" || echo "✗ Health check failed"
echo ""

# Test login
echo "2. Testing login endpoint..."
LOGIN_RESPONSE=$(curl -s -X POST "$API_URL/anon/login" -H "Content-Type: application/json" -H "X-Device-Id: $DEVICE_ID")
echo "$LOGIN_RESPONSE" | grep -q "Login successful" && echo "✓ Login passed" || echo "✗ Login failed"
echo ""

# Test create goal
echo "3. Testing create goal..."
GOAL_RESPONSE=$(curl -s -X POST "$API_URL/goals" \
  -H "Content-Type: application/json" \
  -H "X-Device-Id: $DEVICE_ID" \
  -d '{"title":"Learn Java","description":"Master Spring Boot"}')
GOAL_ID=$(echo "$GOAL_RESPONSE" | grep -o '"id":[0-9]*' | grep -o '[0-9]*')
if [ ! -z "$GOAL_ID" ]; then
  echo "✓ Goal created with ID: $GOAL_ID"
else
  echo "✗ Failed to create goal"
  exit 1
fi
echo ""

# Test get goals
echo "4. Testing get goals..."
curl -s "$API_URL/goals" -H "X-Device-Id: $DEVICE_ID" | grep -q "Learn Java" && echo "✓ Get goals passed" || echo "✗ Get goals failed"
echo ""

# Test create item
echo "5. Testing create item..."
ITEM_RESPONSE=$(curl -s -X POST "$API_URL/items" \
  -H "Content-Type: application/json" \
  -H "X-Device-Id: $DEVICE_ID" \
  -d "{\"goalId\":$GOAL_ID,\"question\":\"What is Spring Boot?\",\"answer\":\"A Java framework for building applications\"}")
ITEM_ID=$(echo "$ITEM_RESPONSE" | grep -o '"id":[0-9]*' | grep -o '[0-9]*')
if [ ! -z "$ITEM_ID" ]; then
  echo "✓ Item created with ID: $ITEM_ID"
else
  echo "✗ Failed to create item"
  exit 1
fi
echo ""

# Test get today's items
echo "6. Testing get today's items..."
curl -s "$API_URL/session/today" -H "X-Device-Id: $DEVICE_ID" | grep -q "What is Spring Boot" && echo "✓ Get today's items passed" || echo "✗ Get today's items failed"
echo ""

# Test review item
echo "7. Testing review item..."
REVIEW_RESPONSE=$(curl -s -X POST "$API_URL/review/$ITEM_ID" \
  -H "Content-Type: application/json" \
  -H "X-Device-Id: $DEVICE_ID" \
  -d '{"correct":true}')
echo "$REVIEW_RESPONSE" | grep -q '"box":2' && echo "✓ Review item passed (moved to box 2)" || echo "✗ Review item failed"
echo ""

# Test billing checkout
echo "8. Testing billing checkout..."
curl -s -X POST "$API_URL/billing/checkout" -H "X-Device-Id: $DEVICE_ID" | grep -q "checkoutUrl" && echo "✓ Billing checkout passed" || echo "✗ Billing checkout failed"
echo ""

echo "All tests completed!"
