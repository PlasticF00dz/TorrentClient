#!/bin/bash

# Test script for torrent upload functionality

echo "Testing BitTorrent Client API..."

# Test 1: Get all torrents
echo "1. Getting all torrents:"
curl -X GET http://localhost:8080/api/torrents
echo -e "\n"

# Test 2: Upload a torrent file (if exists)
TORRENT_FILE="backend/torrents/ubuntu.torrent"
if [ -f "$TORRENT_FILE" ]; then
    echo "2. Uploading torrent file:"
    curl -X POST -F "file=@$TORRENT_FILE" http://localhost:8080/api/torrents/upload
    echo -e "\n"
else
    echo "2. No torrent file found at $TORRENT_FILE"
fi

# Test 3: Get all torrents again to see if upload worked
echo "3. Getting all torrents after upload:"
curl -X GET http://localhost:8080/api/torrents
echo -e "\n"
