@echo off
echo Starting BitTorrent Client with H2 Database...
echo.
cd /d "%~dp0backend"
java -jar target\bittorrent-backend-1.0.0.jar
pause
