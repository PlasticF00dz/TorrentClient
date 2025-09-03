#!/bin/bash
echo "Starting BitTorrent Client with H2 Database..."
echo
cd "$(dirname "$0")/backend"
java -jar target/bittorrent-backend-1.0.0.jar
