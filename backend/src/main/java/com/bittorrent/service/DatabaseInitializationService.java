package com.bittorrent.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;

@Component
@Slf4j
public class DatabaseInitializationService implements CommandLineRunner {

    @Value("${torrent.download.dir:downloads}")
    private String downloadDir;

    @Value("${torrent.meta.dir:torrents}")
    private String metaDir;

    @Override
    public void run(String... args) throws Exception {
        // Create necessary directories on startup
        try {
            Files.createDirectories(Paths.get(downloadDir));
            Files.createDirectories(Paths.get(metaDir));
            Files.createDirectories(Paths.get("data")); // For H2 database files
            
            log.info("✅ H2 Database initialized successfully");
            log.info("📁 Download directory: {}", downloadDir);
            log.info("📁 Torrent metadata directory: {}", metaDir);
            log.info("🔗 H2 Console available at: http://localhost:8080/h2-console");
            log.info("   JDBC URL: jdbc:h2:file:./data/bittorrent_client");
            log.info("   Username: sa");
            log.info("   Password: (leave empty)");
            
        } catch (Exception e) {
            log.error("❌ Failed to initialize application directories", e);
        }
    }
}
