#!/bin/bash

# Test script for BitTorrent Backend API
BASE_URL="http://localhost:8080/api/torrents"

echo "=== BitTorrent Backend API Test ==="

# 1. List all torrents
echo -e "\n1. Listing all torrents:"
curl -s "$BASE_URL" | jq '.'

# 2. Get overview stats
echo -e "\n2. Getting overview stats:"
curl -s "$BASE_URL/stats/overview" | jq '.'

# 3. If there are torrents, get detailed stats for the first one
TORRENT_ID=$(curl -s "$BASE_URL" | jq -r '.[0].id // empty')
if [ ! -z "$TORRENT_ID" ]; then
    echo -e "\n3. Getting detailed stats for torrent ID $TORRENT_ID:"
    curl -s "$BASE_URL/$TORRENT_ID/stats" | jq '.'
    
    echo -e "\n4. Getting status for torrent ID $TORRENT_ID:"
    curl -s "$BASE_URL/$TORRENT_ID/status" | jq '.'
fi

echo -e "\n=== Test Complete ===" 