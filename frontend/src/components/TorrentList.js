import React, { useState, useEffect } from 'react';
import { getTorrents, getTorrentStatus, startTorrent, pauseTorrent, resumeTorrent, removeTorrent } from '../api/torrents';
import ContextMenu from './ContextMenu';

export default function TorrentList() {
    const [torrents, setTorrents] = useState([]);
    const [statuses, setStatuses] = useState({});
    const [menu, setMenu] = useState({ visible: false, x: 0, y: 0, id: null, status: null });

    // Fetch torrent list and statuses
    const refresh = async () => {
        const { data } = await getTorrents();
        setTorrents(data);
        for (const t of data) {
            if (t.id !== undefined && t.id !== null) {
                try {
                    const statusRes = await getTorrentStatus(t.id);
                    setStatuses(s => ({ ...s, [t.id]: statusRes.data[0] }));
                } catch (e) {
                    setStatuses(s => ({ ...s, [t.id]: { status: 'NOT_STARTED', progress: 0 } }));
                }
            }
        }
    };

    useEffect(() => { refresh(); }, []);
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
            status: statuses[id]?.status
        });
    };
    const handleCloseMenu = () => setMenu({ ...menu, visible: false });

    // Torrent actions
    const handlePause = async () => {
        await pauseTorrent(menu.id);
        handleCloseMenu();
        refresh();
    };
    const handleResume = async () => {
        await resumeTorrent(menu.id);
        handleCloseMenu();
        refresh();
    };
    const handleRemove = async () => {
        await removeTorrent(menu.id);
        handleCloseMenu();
        refresh();
    };
    const handleStart = async (id) => {
        await startTorrent(id);
        refresh();
    };

    return (
        <div onClick={handleCloseMenu} style={{ position: 'relative' }}>
            <table className="torrent-table">
                <thead>
                    <tr>
                        <th>Name</th><th>Progress</th><th>Status</th><th>Peers</th><th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {torrents.map(t => (
                        <tr key={t.id} onContextMenu={e => handleContextMenu(e, t.id)} className="torrent-row">
                            <td>{t.name}</td>
                            <td>
                                <progress value={statuses[t.id]?.progress || 0} max="1" style={{ width: 120 }} />
                                {((statuses[t.id]?.progress || 0) * 100).toFixed(1)}%
                            </td>
                            <td>{statuses[t.id]?.status || '-'}</td>
                            <td>{statuses[t.id]?.activePeers ?? '-'}</td>
                            <td>
                                <button className="start-btn" onClick={() => handleStart(t.id)} disabled={statuses[t.id]?.status === 'DOWNLOADING'}>Start</button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
            <ContextMenu
                visible={menu.visible}
                x={menu.x}
                y={menu.y}
                status={menu.status}
                onPause={handlePause}
                onResume={handleResume}
                onRemove={handleRemove}
            />
        </div>
    );
} 