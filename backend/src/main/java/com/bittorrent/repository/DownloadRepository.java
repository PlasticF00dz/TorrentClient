package com.bittorrent.repository;

import com.bittorrent.model.Download;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DownloadRepository extends JpaRepository<Download, Long> {
    List<Download> findByTorrentId(Long torrentId);
}
