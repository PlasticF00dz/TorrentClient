package com.bittorrent.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Torrent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String infoHash;
    private String name;
    private String announceUrl;
    private Long length;
    private Integer pieceCount;
    private Integer pieceLength;
    private String filePath; // .torrent file storage path
    private LocalDateTime createdAt;
}
