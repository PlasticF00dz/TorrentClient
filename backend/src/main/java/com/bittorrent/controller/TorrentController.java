package com.bittorrent.controller;

import com.bittorrent.model.Torrent;
import com.bittorrent.model.Download;
import com.bittorrent.repository.TorrentRepository;
import com.bittorrent.repository.DownloadRepository;
import com.bittorrent.service.TorrentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/torrents")
public class TorrentController {
    private final TorrentService torrentService;
    private final TorrentRepository torrentRepo;
    private final DownloadRepository downloadRepo;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            Torrent saved = torrentService.uploadTorrent(file);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<?> start(@PathVariable Long id) {
        try {
            torrentService.startDownload(id);
            return ResponseEntity.ok("Download started for torrent ID: " + id);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Torrent not found: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to start download: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<?> pause(@PathVariable Long id) {
        try {
            torrentService.pauseDownload(id);
            return ResponseEntity.ok("Paused download for torrent ID: " + id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to pause download: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<?> resume(@PathVariable Long id) {
        try {
            torrentService.resumeDownload(id);
            return ResponseEntity.ok("Resumed download for torrent ID: " + id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to resume download: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!torrentRepo.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Torrent not found");
        }
        // Delete associated downloads first
        downloadRepo.findByTorrentId(id).forEach(d -> downloadRepo.deleteById(d.getId()));
        torrentRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<Torrent> list() {
        return torrentRepo.findAll();
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<?> status(@PathVariable Long id) {
        try {
            List<Download> downloads = downloadRepo.findByTorrentId(id);
            if (downloads.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No downloads found for torrent ID: " + id);
            }
            return ResponseEntity.ok(downloads);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch status: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<?> getStats(@PathVariable Long id) {
        try {
            List<Download> downloads = downloadRepo.findByTorrentId(id);
            if (downloads.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No downloads found for torrent ID: " + id);
            }

            Download latestDownload = downloads.stream()
                    .max((d1, d2) -> d1.getLastUpdated().compareTo(d2.getLastUpdated()))
                    .orElse(downloads.get(0));

            Map<String, Object> stats = new HashMap<>();
            stats.put("download", latestDownload);
            stats.put("formattedStats", formatStats(latestDownload));

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch stats: " + e.getMessage());
        }
    }

    @GetMapping("/stats/overview")
    public ResponseEntity<?> getOverview() {
        try {
            List<Download> allDownloads = downloadRepo.findAll();

            Map<String, Object> overview = new HashMap<>();
            overview.put("totalDownloads", allDownloads.size());
            overview.put("activeDownloads", allDownloads.stream()
                    .filter(d -> "DOWNLOADING".equals(d.getStatus())).count());
            overview.put("completedDownloads", allDownloads.stream()
                    .filter(d -> "COMPLETED".equals(d.getStatus())).count());
            overview.put("failedDownloads", allDownloads.stream()
                    .filter(d -> "FAILED".equals(d.getStatus())).count());
            overview.put("pausedDownloads", allDownloads.stream()
                    .filter(d -> "PAUSED".equals(d.getStatus())).count());

            return ResponseEntity.ok(overview);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch overview: " + e.getMessage());
        }
    }

    private Map<String, String> formatStats(Download download) {
        Map<String, String> formatted = new HashMap<>();

        // Format download speed
        if (download.getDownloadSpeed() != null) {
            formatted.put("downloadSpeed", formatBytes(download.getDownloadSpeed()) + "/s");
        }

        // Format upload speed
        if (download.getUploadSpeed() != null) {
            formatted.put("uploadSpeed", formatBytes(download.getUploadSpeed()) + "/s");
        }

        // Format progress
        if (download.getProgress() != null) {
            formatted.put("progress", String.format("%.2f%%", download.getProgress() * 100));
        }

        // Format downloaded/total
        if (download.getDownloadedBytes() != null && download.getTotalBytes() != null) {
            formatted.put("downloaded", formatBytes(download.getDownloadedBytes()));
            formatted.put("total", formatBytes(download.getTotalBytes()));
        }

        // Format ETA
        if (download.getEstimatedTimeRemaining() != null && download.getEstimatedTimeRemaining() > 0) {
            formatted.put("eta", formatTime(download.getEstimatedTimeRemaining()));
        } else {
            formatted.put("eta", "Unknown");
        }

        return formatted;
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    private String formatTime(long seconds) {
        if (seconds < 60)
            return seconds + "s";
        if (seconds < 3600)
            return (seconds / 60) + "m " + (seconds % 60) + "s";
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        return hours + "h " + minutes + "m";
    }
}
