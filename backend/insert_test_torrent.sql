-- Insert test torrent data
INSERT INTO TORRENT (info_hash, name, announce_url, length, piece_count, piece_length, file_path, created_at) 
VALUES ('d984f67af9917b214cd8b6048ab5624c7df6a07a', 'Test Folder', 'http://tracker.example.com:8080/announce', 1048576, 64, 16384, 'torrents/test_folder-d984f67af9917b214cd8b6048ab5624c7df6a07a.torrent', CURRENT_TIMESTAMP);

-- Query to verify insertion
SELECT * FROM TORRENT;
