const API_BASE = "http://localhost:8080/api";

export const uploadTorrent = async (file) => {
    const formData = new FormData();
    formData.append("file", file);

    const response = await fetch(`http://localhost:8080/api/torrents/upload`, {
        method: "POST",
        body: formData,
    });

    if (!response.ok) {
        const errorText = await response.text();
        console.error(
            "Upload failed with status:",
            response.status,
            "and message:",
            errorText
        );
        throw new Error(`Upload failed: ${errorText}`);
    }

    return response.json();
};

export const getTorrents = async () => {
    const response = await fetch(`${API_BASE}/torrents`);

    if (!response.ok) {
        throw new Error("Failed to fetch torrents");
    }

    return response.json();
};

export const startDownload = async (torrentId) => {
    const response = await fetch(`${API_BASE}/torrents/${torrentId}/start`, {
        method: "POST",
    });

    if (!response.ok) {
        throw new Error("Failed to start download");
    }

    return response.text();
};

export const pauseDownload = async (torrentId) => {
    const response = await fetch(`${API_BASE}/torrents/${torrentId}/pause`, {
        method: "POST",
    });

    if (!response.ok) {
        throw new Error("Failed to pause download");
    }

    return response.text();
};

export const resumeDownload = async (torrentId) => {
    const response = await fetch(`${API_BASE}/torrents/${torrentId}/resume`, {
        method: "POST",
    });

    if (!response.ok) {
        throw new Error("Failed to resume download");
    }

    return response.text();
};

export const deleteTorrent = async (torrentId) => {
    const response = await fetch(`${API_BASE}/torrents/${torrentId}`, {
        method: "DELETE",
    });

    if (!response.ok) {
        throw new Error("Failed to delete torrent");
    }

    return response.text();
};

export const getTorrentStatus = async (torrentId) => {
    const response = await fetch(`${API_BASE}/torrents/${torrentId}/status`);

    if (!response.ok) {
        throw new Error("Failed to get torrent status");
    }

    return response.json();
};
