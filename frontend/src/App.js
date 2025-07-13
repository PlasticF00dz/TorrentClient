import React, { useState } from 'react';
import TorrentUpload from './components/TorrentUpload';
import TorrentList from './components/TorrentList';

function App() {
    const [refreshKey, setRefreshKey] = useState(0);
    return (
        <div className="app-container">
            <h2 className="app-title">Torrent Client Dashboard</h2>
            <TorrentUpload onUpload={() => setRefreshKey(k => k + 1)} />
            <TorrentList key={refreshKey} />
        </div>
    );
}

export default App; 