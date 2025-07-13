package com.bittorrent.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Download {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Torrent torrent;

    private Long downloadedBytes;
    private Long totalBytes;
    private Double progress;
    private String status; // DOWNLOADING, COMPLETED, FAILED, PAUSED
    private Integer activePeers;
    private Integer connectedPeers;
    private Integer availablePeers;

    // Speed tracking
    private Long downloadSpeed; // bytes per second
    private Long uploadSpeed; // bytes per second

    // ETA and time tracking
    private Long estimatedTimeRemaining; // seconds
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime lastUpdated;

    // Additional info
    private String errorMessage;
    private Long uploadedBytes;
    private Double uploadRatio;
}
