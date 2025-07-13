import React from 'react';

export default function ContextMenu({ visible, x, y, onPause, onResume, onRemove, status }) {
    if (!visible) return null;
    return (
        <ul className="context-menu" style={{ position: 'absolute', top: y, left: x }}>
            {status === 'DOWNLOADING' ? (
                <li onClick={onPause} className="context-menu-item">Pause</li>
            ) : status === 'PAUSED' ? (
                <li onClick={onResume} className="context-menu-item">Resume</li>
            ) : null}
            <li onClick={onRemove} className="context-menu-item remove">Remove</li>
        </ul>
    );
} 