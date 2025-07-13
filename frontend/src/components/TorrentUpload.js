import React, { useRef } from 'react';
import { uploadTorrent } from '../api/torrents';

export default function TorrentUpload({ onUpload }) {
    const fileInput = useRef();

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (fileInput.current.files.length === 0) return;
        await uploadTorrent(fileInput.current.files[0]);
        fileInput.current.value = '';
        onUpload();
    };

    return (
        <form onSubmit={handleSubmit} className="upload-form">
            <input type="file" ref={fileInput} accept=".torrent" className="upload-input" />
            <button type="submit" className="upload-btn">Upload Torrent</button>
        </form>
    );
} 