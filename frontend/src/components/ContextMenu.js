import React from "react";

export default function ContextMenu({
    visible,
    x,
    y,
    onStop,
    onRemove,
    status,
}) {
    if (!visible) return null;
    return (
        <ul
            className="context-menu"
            style={{ position: "absolute", top: y, left: x }}
        >
            {status === "DOWNLOADING" && (
                <li onClick={onStop} className="context-menu-item">
                    Stop
                </li>
            )}
            <li onClick={onRemove} className="context-menu-item remove">
                Remove
            </li>
        </ul>
    );
}
