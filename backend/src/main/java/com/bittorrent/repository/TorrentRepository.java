package com.bittorrent.repository;

import com.bittorrent.model.Torrent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TorrentRepository extends JpaRepository<Torrent, Long> {
    Torrent findByInfoHash(String infoHash);
}
