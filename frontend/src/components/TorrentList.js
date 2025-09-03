import React, { useState, useEffect } from "react";
import {
    getTorrents,
    getTorrentStatus,
    startDownload,
    pauseDownload,
    deleteTorrent,
} from "../api/torrents";
import ContextMenu from "./ContextMenu";

export default function TorrentList() {
    const [torrents, setTorrents] = useState([]);
    const [statuses, setStatuses] = useState({});
    const [locallyPaused, setLocallyPaused] = useState(new Set()); // Track locally paused torrents
    const [locallyStarted, setLocallyStarted] = useState(new Set()); // Track locally started torrents
    const [pausedProgress, setPausedProgress] = useState({}); // Store progress when paused
    const [menu, setMenu] = useState({
        visible: false,
        x: 0,
        y: 0,
        id: null,
        status: null,
    });

    // Fetch torrent list and statuses
    const refresh = async () => {
        const data = await getTorrents();
        setTorrents(data);
        for (const t of data) {
            if (t.id !== undefined && t.id !== null) {
                try {
                    const statusRes = await getTorrentStatus(t.id);
                    const dbStatus = statusRes[0];
                    setStatuses((s) => ({ ...s, [t.id]: dbStatus }));

                    // Only clear local overrides when database status confirms our action worked
                    // Clear locally started override when database shows DOWNLOADING
                    if (
                        dbStatus?.status === "DOWNLOADING" &&
                        locallyStarted.has(t.id)
                    ) {
                        setLocallyStarted((prev) => {
                            const newSet = new Set(prev);
                            newSet.delete(t.id);
                            return newSet;
                        });
                    }
                    // DON'T clear locally paused - let it persist until user clicks start
                } catch (e) {
                    setStatuses((s) => ({
                        ...s,
                        [t.id]: { status: "NOT_STARTED", progress: 0 },
                    }));
                }
            }
        }
    };

    useEffect(() => {
        refresh();
    }, []);
    useEffect(() => {
        const interval = setInterval(refresh, 2000);
        return () => clearInterval(interval);
    }, []);

    // Right-click context menu
    const handleContextMenu = (e, id) => {
        e.preventDefault();
        setMenu({
            visible: true,
            x: e.pageX,
            y: e.pageY,
            id,
            status: statuses[id]?.status,
        });
    };
    const handleCloseMenu = () => setMenu({ ...menu, visible: false });

    // Torrent actions
    const handleStop = async (id) => {
        const torrentId = id || menu.id;
        console.log(`Pausing torrent ${torrentId}`);

        // Store current progress before pausing
        const currentProgress = statuses[torrentId]?.progress || 0;
        setPausedProgress((prev) => ({
            ...prev,
            [torrentId]: currentProgress,
        }));

        await pauseDownload(torrentId);

        // Add to locally paused set and remove from started set
        setLocallyPaused((prev) => {
            const newSet = new Set([...prev, torrentId]);
            console.log(
                `Added ${torrentId} to locally paused. New set:`,
                newSet
            );
            return newSet;
        });
        setLocallyStarted((prev) => {
            const newSet = new Set(prev);
            newSet.delete(torrentId);
            return newSet;
        });

        handleCloseMenu();
        refresh();
    };
    const handleRemove = async () => {
        await deleteTorrent(menu.id);

        // Remove from both local state sets when deleted
        setLocallyPaused((prev) => {
            const newSet = new Set(prev);
            newSet.delete(menu.id);
            return newSet;
        });
        setLocallyStarted((prev) => {
            const newSet = new Set(prev);
            newSet.delete(menu.id);
            return newSet;
        });

        // Clear paused progress when deleting
        setPausedProgress((prev) => {
            const newProg = { ...prev };
            delete newProg[menu.id];
            return newProg;
        });

        handleCloseMenu();
        refresh();
    };
    const handleStart = async (id) => {
        console.log(`Starting torrent ${id}`);

        await startDownload(id);

        // Add to locally started set and remove from paused set
        setLocallyStarted((prev) => new Set([...prev, id]));
        setLocallyPaused((prev) => {
            const newSet = new Set(prev);
            newSet.delete(id);
            console.log(`Removed ${id} from locally paused. New set:`, newSet);
            return newSet;
        });

        // Clear paused progress when starting
        setPausedProgress((prev) => {
            const newProg = { ...prev };
            delete newProg[id];
            return newProg;
        });

        refresh();
    };

    // Helper function to get effective status (local override or database status)
    const getEffectiveStatus = (torrentId) => {
        // Priority: Local pause state overrides everything
        if (locallyPaused.has(torrentId)) {
            console.log(
                `Torrent ${torrentId} is locally paused, showing PAUSED`
            );
            return "PAUSED";
        }
        // Then local start state overrides database
        if (locallyStarted.has(torrentId)) {
            console.log(
                `Torrent ${torrentId} is locally started, showing DOWNLOADING`
            );
            return "DOWNLOADING";
        }
        // Finally fall back to database status
        const dbStatus = statuses[torrentId]?.status || "-";
        console.log(`Torrent ${torrentId} using database status: ${dbStatus}`);
        return dbStatus;
    };

    // Helper function to get effective progress (freeze progress when paused)
    const getEffectiveProgress = (torrentId) => {
        // If locally paused, use the frozen progress from when it was paused
        if (
            locallyPaused.has(torrentId) &&
            pausedProgress[torrentId] !== undefined
        ) {
            return pausedProgress[torrentId];
        }
        return statuses[torrentId]?.progress || 0;
    };

    return (
        <div onClick={handleCloseMenu} style={{ position: "relative" }}>
            <table className="torrent-table">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Progress</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {torrents.map((t) => (
                        <tr
                            key={t.id}
                            onContextMenu={(e) => handleContextMenu(e, t.id)}
                            className="torrent-row"
                        >
                            <td>{t.name}</td>
                            <td>
                                <progress
                                    value={getEffectiveProgress(t.id)}
                                    max="1"
                                    style={{ width: 120 }}
                                />
                                {(getEffectiveProgress(t.id) * 100).toFixed(1)}%
                            </td>
                            <td>{getEffectiveStatus(t.id)}</td>
                            <td>
                                {getEffectiveStatus(t.id) === "DOWNLOADING" ? (
                                    <button
                                        className="stop-btn"
                                        onClick={() => handleStop(t.id)}
                                    >
                                        Stop
                                    </button>
                                ) : (
                                    <button
                                        className="start-btn"
                                        onClick={() => handleStart(t.id)}
                                    >
                                        Start
                                    </button>
                                )}
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
            <ContextMenu
                visible={menu.visible}
                x={menu.x}
                y={menu.y}
                status={getEffectiveStatus(menu.id)}
                onStop={handleStop}
                onRemove={handleRemove}
            />
        </div>
    );
}
