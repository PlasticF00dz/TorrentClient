# BitTorrent Client

A modern, full-stack BitTorrent client application built with **Spring Boot** backend and **React** frontend. Upload torrent files, manage downloads, and monitor progress in real-time.

## 🚀 Features

- **Torrent Upload**: Upload `.torrent` files through web interface
- **Download Management**: Start, pause, resume, and remove downloads
- **Real-time Progress Tracking**: Monitor download progress, speed, and peer connections
- **Multi-torrent Support**: Handle multiple simultaneous downloads
- **Peer Statistics**: Track active, connected, and available peers
- **Speed Monitoring**: Real-time download and upload speed tracking
- **Modern React Dashboard**: Clean, responsive user interface with context menus

## 🛠️ Technology Stack

**Backend:**
- Java 17 + Spring Boot 3.3.0
- Spring Data JPA + PostgreSQL
- TTorrent Core 1.5 (BitTorrent protocol)
- Maven build tool

**Frontend:**
- React + JavaScript (ES6+)
- CSS3 styling
- Fetch API for HTTP requests

## 📋 Prerequisites

- Java 17+
- Maven 3.6+
- Node.js 14+ and npm
- PostgreSQL 12+

## 🚀 Quick Start

### 1. Database Setup
Create PostgreSQL database named `bittorrent_client` and update credentials in `backend/src/main/resources/application.properties`

### 2. Backend Setup
Navigate to backend directory and run:
- `mvn clean compile`
- `mvn spring-boot:run`

Backend starts on `http://localhost:8080`

### 3. Frontend Setup
Create missing API client file `frontend/src/api/torrents.js` with HTTP request functions, then:
- `cd frontend`
- `npm install` 
- `npm start`

Frontend starts on `http://localhost:3000`

## 📱 Usage

1. **Upload Torrent**: Click "Choose File" → select `.torrent` file → "Upload Torrent"
2. **Start Download**: Click "Start" button next to uploaded torrent
3. **Manage Downloads**: Right-click torrents for pause/resume/remove options
4. **Monitor Progress**: View real-time progress bars, speeds, and peer counts

## 🔧 API Endpoints

**Torrent Management:**
- `POST /api/torrents/upload` - Upload torrent file
- `GET /api/torrents` - List all torrents
- `DELETE /api/torrents/{id}` - Remove torrent

**Download Control:**
- `POST /api/torrents/{id}/start` - Start download
- `POST /api/torrents/{id}/pause` - Pause download
- `POST /api/torrents/{id}/resume` - Resume download

**Statistics:**
- `GET /api/torrents/{id}/status` - Get download status
- `GET /api/torrents/{id}/stats` - Get detailed statistics
- `GET /api/torrents/stats/overview` - Get overview statistics

## 📊 Architecture

**Backend Structure:**
- `BitTorrentApplication.java` - Main application entry point
- `TorrentController.java` - REST API endpoints
- `TorrentService.java` - Core BitTorrent logic with progress tracking
- `Torrent.java` & `Download.java` - JPA entity models
- `WebConfig.java` - CORS configuration

**Frontend Structure:**
- `App.js` - Main React component
- `TorrentUpload.js` - File upload component
- `TorrentList.js` - Torrent management with real-time updates
- `ContextMenu.js` - Right-click actions

## 🔍 Testing

Use the provided test script: `./backend/test_api.sh`

---

**Note:** This project is for educational purposes. Ensure compliance with local laws regarding BitTorrent usage.
