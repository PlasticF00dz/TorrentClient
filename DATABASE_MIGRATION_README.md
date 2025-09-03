# BitTorrent Client - Database Migration to H2

This application has been successfully migrated from PostgreSQL to H2 Database, a lightweight, file-based database that requires no external setup.

## 🎯 What Changed

### ✅ Removed

-   PostgreSQL dependency and configuration
-   External database server requirements
-   Complex database setup steps

### ✅ Added

-   H2 Database (file-based, no external server needed)
-   H2 Console for database management
-   Automatic directory creation
-   Simplified configuration

## 🚀 Quick Start

### Prerequisites

-   Java 17+
-   Maven 3.6+

### Running the Application

1. **Build the application:**

    ```bash
    cd backend
    mvn clean package -DskipTests
    ```

2. **Run the application:**

    ```bash
    java -jar target/bittorrent-backend-1.0.0.jar
    ```

3. **Application will start on:** `http://localhost:8080`

## 📊 Database Management

### H2 Console Access

-   **URL:** `http://localhost:8080/h2-console`
-   **JDBC URL:** `jdbc:h2:file:./data/bittorrent_client`
-   **Username:** `sa`
-   **Password:** _(leave empty)_

### Database Files Location

-   Database files are stored in the `./data/` directory
-   Files are automatically created on first run
-   Data persists between application restarts

## 🔧 API Endpoints

All existing torrent functionality remains unchanged:

### Upload Torrent

```bash
curl -X POST -F "file=@example.torrent" http://localhost:8080/api/torrents/upload
```

### List Torrents

```bash
curl -X GET http://localhost:8080/api/torrents
```

### Start Download

```bash
curl -X POST http://localhost:8080/api/torrents/{id}/start
```

### Pause Download

```bash
curl -X POST http://localhost:8080/api/torrents/{id}/pause
```

### Resume Download

```bash
curl -X POST http://localhost:8080/api/torrents/{id}/resume
```

### Check Status

```bash
curl -X GET http://localhost:8080/api/torrents/{id}/status
```

### Delete Torrent

```bash
curl -X DELETE http://localhost:8080/api/torrents/{id}
```

## 📁 Directory Structure

The application creates these directories automatically:

-   `./data/` - H2 database files
-   `./downloads/` - Downloaded torrent content
-   `./torrents/` - Torrent metadata files

## 🛠️ Development Features

### H2 Console Features

-   View all tables and data
-   Execute SQL queries
-   Monitor database performance
-   Export/import data

### Logging

-   Database queries are logged (can be disabled by setting `spring.jpa.show-sql=false`)
-   Application logs show startup information and database connection details

## 🔒 Production Considerations

For production use, consider:

1. **Disable H2 Console:**

    ```properties
    spring.h2.console.enabled=false
    ```

2. **Disable SQL Logging:**

    ```properties
    spring.jpa.show-sql=false
    ```

3. **Set Up Regular Backups:**

    - Copy the `./data/` directory periodically
    - Consider using H2's backup tools

4. **Secure Database Access:**
    - Set a password in production
    - Restrict file system access

## 📋 Benefits of H2 Migration

1. **No External Dependencies:** No need to install/configure PostgreSQL
2. **Portable:** Database files travel with the application
3. **Zero Configuration:** Works out of the box
4. **Development Friendly:** Built-in web console for database management
5. **Fast:** In-memory performance with file persistence
6. **Small Footprint:** Minimal resource usage

## 🐛 Troubleshooting

### Database Connection Issues

-   Ensure the `./data/` directory is writable
-   Check if port 8080 is available
-   Verify Java version (requires Java 17+)

### Application Won't Start

-   Check if another instance is running
-   Verify the JAR file exists in `target/` directory
-   Check application logs for specific errors

### Data Loss

-   Database files are in `./data/` directory
-   Backup this directory to preserve data
-   Files are created automatically if missing

---

_All torrent download functionality (start, stop, resume, progress tracking) remains fully functional with the new H2 database backend._
