# Test script for torrent upload functionality

Write-Host "Testing BitTorrent Client API..."

# Test 1: Get all torrents
Write-Host "1. Getting all torrents:"
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/torrents" -Method GET
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "Error: $_"
}
Write-Host ""

# Test 2: Upload a torrent file (if exists)
$torrentFile = "backend/torrents/ubuntu.torrent"
if (Test-Path $torrentFile) {
    Write-Host "2. Uploading torrent file:"
    try {
        $form = @{
            file = Get-Item $torrentFile
        }
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/torrents/upload" -Method POST -Form $form
        $response | ConvertTo-Json -Depth 3
    } catch {
        Write-Host "Upload Error: $_"
    }
} else {
    Write-Host "2. No torrent file found at $torrentFile"
}
Write-Host ""

# Test 3: Get all torrents again to see if upload worked
Write-Host "3. Getting all torrents after upload:"
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/torrents" -Method GET
    $response | ConvertTo-Json -Depth 3
} catch {
    Write-Host "Error: $_"
}
Write-Host ""
