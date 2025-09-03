#!/bin/bash
# BitTorrent Client API Test Script

BASE_URL="http://localhost:8080/api/torrents"
echo "🧪 Testing BitTorrent Client API"
echo "================================="
echo

# Test 1: List all torrents (should be empty initially)
echo "📋 Test 1: List all torrents"
echo "GET $BASE_URL"
curl -s -X GET "$BASE_URL" | python -m json.tool 2>/dev/null || curl -s -X GET "$BASE_URL"
echo -e "\n"

# Test 2: Upload a torrent (if upload works)
echo "📤 Test 2: Upload torrent"
echo "POST $BASE_URL/upload"
UPLOAD_RESULT=$(curl -s -X POST -F "file=@archlinux-2025.07.01-x86_64.iso.torrent" "$BASE_URL/upload")
echo "$UPLOAD_RESULT"
echo -e "\n"

# Extract torrent ID from upload result (if successful)
TORRENT_ID=$(echo "$UPLOAD_RESULT" | grep -o '"id":[0-9]*' | grep -o '[0-9]*' || echo "1")

# Test 3: List torrents after upload
echo "📋 Test 3: List torrents after upload"
echo "GET $BASE_URL"
curl -s -X GET "$BASE_URL" | python -m json.tool 2>/dev/null || curl -s -X GET "$BASE_URL"
echo -e "\n"

# Test 4: Start download
echo "🚀 Test 4: Start download for torrent ID: $TORRENT_ID"
echo "POST $BASE_URL/$TORRENT_ID/start"
START_RESULT=$(curl -s -X POST "$BASE_URL/$TORRENT_ID/start")
echo "$START_RESULT"
echo -e "\n"

# Test 5: Check download status
echo "📊 Test 5: Check download status"
echo "GET $BASE_URL/$TORRENT_ID/status"
curl -s -X GET "$BASE_URL/$TORRENT_ID/status" | python -m json.tool 2>/dev/null || curl -s -X GET "$BASE_URL/$TORRENT_ID/status"
echo -e "\n"

# Wait a few seconds for download to start
echo "⏳ Waiting 5 seconds for download to initialize..."
sleep 5

# Test 6: Check status again
echo "📊 Test 6: Check status after 5 seconds"
echo "GET $BASE_URL/$TORRENT_ID/status"
curl -s -X GET "$BASE_URL/$TORRENT_ID/status" | python -m json.tool 2>/dev/null || curl -s -X GET "$BASE_URL/$TORRENT_ID/status"
echo -e "\n"

# Test 7: Pause download
echo "⏸️  Test 7: Pause download"
echo "POST $BASE_URL/$TORRENT_ID/pause"
PAUSE_RESULT=$(curl -s -X POST "$BASE_URL/$TORRENT_ID/pause")
echo "$PAUSE_RESULT"
echo -e "\n"

# Test 8: Check status after pause
echo "📊 Test 8: Check status after pause"
echo "GET $BASE_URL/$TORRENT_ID/status"
curl -s -X GET "$BASE_URL/$TORRENT_ID/status" | python -m json.tool 2>/dev/null || curl -s -X GET "$BASE_URL/$TORRENT_ID/status"
echo -e "\n"

# Test 9: Resume download
echo "▶️  Test 9: Resume download"
echo "POST $BASE_URL/$TORRENT_ID/resume"
RESUME_RESULT=$(curl -s -X POST "$BASE_URL/$TORRENT_ID/resume")
echo "$RESUME_RESULT"
echo -e "\n"

# Test 10: Final status check
echo "📊 Test 10: Final status check"
echo "GET $BASE_URL/$TORRENT_ID/status"
curl -s -X GET "$BASE_URL/$TORRENT_ID/status" | python -m json.tool 2>/dev/null || curl -s -X GET "$BASE_URL/$TORRENT_ID/status"
echo -e "\n"

# Test 11: Delete torrent
echo "🗑️  Test 11: Delete torrent"
echo "DELETE $BASE_URL/$TORRENT_ID"
DELETE_RESULT=$(curl -s -X DELETE "$BASE_URL/$TORRENT_ID")
echo "$DELETE_RESULT"
echo -e "\n"

# Test 12: Verify deletion
echo "📋 Test 12: List torrents after deletion"
echo "GET $BASE_URL"
curl -s -X GET "$BASE_URL" | python -m json.tool 2>/dev/null || curl -s -X GET "$BASE_URL"
echo -e "\n"

echo "✅ API Testing Complete!"
echo "========================"
