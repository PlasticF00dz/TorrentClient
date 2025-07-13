package com.bittorrent.service;

import com.bittorrent.model.Torrent;
import com.bittorrent.model.Download;
import com.bittorrent.repository.TorrentRepository;
import com.bittorrent.repository.DownloadRepository;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class TorrentService {
    private final TorrentRepository torrentRepo;
    private final DownloadRepository downloadRepo;

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Map<Long, Client> clientMap = new ConcurrentHashMap<>();
    private final Map<Long, DownloadProgressTracker> progressTrackers = new ConcurrentHashMap<>();

    @Value("${torrent.download.dir:downloads}")
    private String downloadDir;

    @Value("${torrent.meta.dir:torrents}")
    private String metaDir;

    public TorrentService(TorrentRepository torrentRepo, DownloadRepository downloadRepo) {
        this.torrentRepo = torrentRepo;
        this.downloadRepo = downloadRepo;
    }

    public Torrent uploadTorrent(MultipartFile file) throws IOException, NoSuchAlgorithmException {
        Files.createDirectories(Paths.get(metaDir));
        String fileName = file.getOriginalFilename();
        String targetPath = metaDir + "/" + fileName;
        file.transferTo(new File(targetPath));

        // Load the .torrent using ttorrent's Torrent class
        com.turn.ttorrent.common.Torrent tTorrent = com.turn.ttorrent.common.Torrent.load(new File(targetPath), true);

        Torrent torrent = new Torrent();
        torrent.setInfoHash(tTorrent.getHexInfoHash());
        torrent.setName(tTorrent.getName());
        torrent.setAnnounceUrl(tTorrent.getAnnounceList().get(0).get(0).toString());
        torrent.setLength(tTorrent.getSize());
        torrent.setPieceCount(0); // Default value since API method not available
        torrent.setPieceLength(0); // Default value since API method not available
        torrent.setFilePath(targetPath);
        torrent.setCreatedAt(LocalDateTime.now());

        return torrentRepo.save(torrent);
    }

    public void startDownload(Long torrentId) {
        Torrent torrent = torrentRepo.findById(torrentId)
                .orElseThrow(() -> new RuntimeException("Torrent not found"));

        Download download = new Download();
        download.setTorrent(torrent);
        download.setStartedAt(LocalDateTime.now());
        download.setLastUpdated(LocalDateTime.now());
        download.setStatus("DOWNLOADING");
        download.setProgress(0.0);
        download.setDownloadedBytes(0L);
        download.setTotalBytes(torrent.getLength());
        download.setDownloadSpeed(0L);
        download.setUploadSpeed(0L);
        download.setUploadedBytes(0L);
        download.setUploadRatio(0.0);
        download.setActivePeers(0);
        download.setConnectedPeers(0);
        download.setAvailablePeers(0);
        download.setEstimatedTimeRemaining(0L);

        Download savedDownload = downloadRepo.save(download);

        executor.submit(() -> {
            try {
                Files.createDirectories(Paths.get(downloadDir));

                // Load the .torrent again for download
                com.turn.ttorrent.common.Torrent tTorrent = com.turn.ttorrent.common.Torrent
                        .load(new File(torrent.getFilePath()), true);
                SharedTorrent st = new SharedTorrent(tTorrent, new File(downloadDir), false);

                Client client = new Client(InetAddress.getLocalHost(), st);
                clientMap.put(torrentId, client);

                // Create progress tracker
                DownloadProgressTracker tracker = new DownloadProgressTracker(savedDownload, downloadRepo, st,
                        tTorrent);
                progressTrackers.put(torrentId, tracker);

                // Start progress monitoring
                tracker.startMonitoring();

                client.download();
                client.waitForCompletion();

                // Final update
                tracker.updateProgress(1.0, tTorrent.getSize(), 0L, 0L, 0, 0, 0);
                savedDownload.setStatus("COMPLETED");
                savedDownload.setCompletedAt(LocalDateTime.now());
                downloadRepo.save(savedDownload);

                // Cleanup
                progressTrackers.remove(torrentId);

            } catch (Exception e) {
                log.error("Download error", e);
                savedDownload.setStatus("FAILED");
                savedDownload.setErrorMessage(e.getMessage());
                downloadRepo.save(savedDownload);
                progressTrackers.remove(torrentId);
            }
        });
    }

    public void pauseDownload(Long torrentId) {
        Client client = clientMap.get(torrentId);
        if (client != null) {
            client.stop();
        }

        // Update status
        Download download = downloadRepo.findByTorrentId(torrentId).stream()
                .filter(d -> "DOWNLOADING".equals(d.getStatus()))
                .findFirst()
                .orElse(null);

        if (download != null) {
            download.setStatus("PAUSED");
            download.setLastUpdated(LocalDateTime.now());
            downloadRepo.save(download);
        }
    }

    public void resumeDownload(Long torrentId) {
        Torrent torrent = torrentRepo.findById(torrentId)
                .orElseThrow(() -> new RuntimeException("Torrent not found"));

        // Update status
        Download download = downloadRepo.findByTorrentId(torrentId).stream()
                .filter(d -> "PAUSED".equals(d.getStatus()))
                .findFirst()
                .orElse(null);

        if (download != null) {
            download.setStatus("DOWNLOADING");
            download.setLastUpdated(LocalDateTime.now());
            downloadRepo.save(download);
        }

        try {
            com.turn.ttorrent.common.Torrent tTorrent = com.turn.ttorrent.common.Torrent
                    .load(new File(torrent.getFilePath()), true);
            SharedTorrent st = new SharedTorrent(tTorrent, new File(downloadDir), false);
            Client client = new Client(InetAddress.getLocalHost(), st);
            clientMap.put(torrentId, client);

            // Create new progress tracker
            DownloadProgressTracker tracker = new DownloadProgressTracker(download, downloadRepo, st, tTorrent);
            progressTrackers.put(torrentId, tracker);
            tracker.startMonitoring();

            executor.submit(() -> {
                try {
                    client.download();
                    client.waitForCompletion();

                    // Final update
                    tracker.updateProgress(1.0, tTorrent.getSize(), 0L, 0L, 0, 0, 0);
                    download.setStatus("COMPLETED");
                    download.setCompletedAt(LocalDateTime.now());
                    downloadRepo.save(download);

                    progressTrackers.remove(torrentId);
                } catch (Exception e) {
                    log.error("Resume error", e);
                    download.setStatus("FAILED");
                    download.setErrorMessage(e.getMessage());
                    downloadRepo.save(download);
                    progressTrackers.remove(torrentId);
                }
            });
        } catch (Exception e) {
            log.error("Resume error", e);
        }
    }

    // Helper class for tracking download progress
    private static class DownloadProgressTracker {
        private final Download download;
        private final DownloadRepository downloadRepo;
        private final SharedTorrent sharedTorrent;
        private final com.turn.ttorrent.common.Torrent torrent;
        private final AtomicLong lastDownloadedBytes = new AtomicLong(0);
        private final AtomicLong lastUploadedBytes = new AtomicLong(0);
        private LocalDateTime lastUpdateTime;
        private volatile boolean running = true;

        public DownloadProgressTracker(Download download, DownloadRepository downloadRepo,
                SharedTorrent sharedTorrent, com.turn.ttorrent.common.Torrent torrent) {
            this.download = download;
            this.downloadRepo = downloadRepo;
            this.sharedTorrent = sharedTorrent;
            this.torrent = torrent;
            this.lastUpdateTime = LocalDateTime.now();
        }

        public void startMonitoring() {
            Thread monitorThread = new Thread(() -> {
                while (running && !sharedTorrent.isComplete()) {
                    try {
                        updateProgress();
                        Thread.sleep(2000); // Update every 2 seconds
                    } catch (Exception e) {
                        log.error("Progress monitoring error", e);
                    }
                }
            });
            monitorThread.setDaemon(true);
            monitorThread.start();
        }

        private void updateProgress() {
            double progress = sharedTorrent.getCompletion();
            long downloadedBytes = (long) (progress * torrent.getSize());
            long uploadedBytes = 0; // Simplified - ttorrent doesn't expose this easily

            LocalDateTime now = LocalDateTime.now();
            long timeDiffSeconds = ChronoUnit.SECONDS.between(lastUpdateTime, now);

            // Calculate speeds
            long downloadSpeed = 0;
            long uploadSpeed = 0;

            if (timeDiffSeconds > 0) {
                downloadSpeed = (downloadedBytes - lastDownloadedBytes.get()) / timeDiffSeconds;
                uploadSpeed = (uploadedBytes - lastUploadedBytes.get()) / timeDiffSeconds;
            }

            // Calculate ETA
            long remainingBytes = torrent.getSize() - downloadedBytes;
            long estimatedTimeRemaining = downloadSpeed > 0 ? remainingBytes / downloadSpeed : 0;

            // Update peer counts (simplified - ttorrent doesn't expose this easily)
            int connectedPeers = 0; // Simplified
            int availablePeers = 0; // Simplified

            updateProgress(progress, downloadedBytes, downloadSpeed, uploadSpeed,
                    connectedPeers, availablePeers, estimatedTimeRemaining);

            lastDownloadedBytes.set(downloadedBytes);
            lastUploadedBytes.set(uploadedBytes);
            lastUpdateTime = now;
        }

        public void updateProgress(double progress, long downloadedBytes, long downloadSpeed,
                long uploadSpeed, int connectedPeers, int availablePeers,
                long estimatedTimeRemaining) {
            download.setProgress(progress);
            download.setDownloadedBytes(downloadedBytes);
            download.setDownloadSpeed(downloadSpeed);
            download.setUploadSpeed(uploadSpeed);
            download.setConnectedPeers(connectedPeers);
            download.setAvailablePeers(availablePeers);
            download.setActivePeers(connectedPeers);
            download.setEstimatedTimeRemaining(estimatedTimeRemaining);
            download.setUploadedBytes(
                    uploadSpeed > 0 ? download.getUploadedBytes() + uploadSpeed * 2 : download.getUploadedBytes());
            download.setUploadRatio(downloadedBytes > 0 ? (double) download.getUploadedBytes() / downloadedBytes : 0.0);
            download.setLastUpdated(LocalDateTime.now());

            downloadRepo.save(download);
        }

        public void stop() {
            running = false;
        }
    }
}
