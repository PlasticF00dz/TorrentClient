# BitTorrent Client API Test Script (PowerShell)
$BASE_URL = "http://localhost:8080/api/torrents"

Write-Host "🧪 Testing BitTorrent Client API" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: List all torrents
Write-Host "📋 Test 1: List all torrents" -ForegroundColor Yellow
Write-Host "GET $BASE_URL"
$response1 = Invoke-RestMethod -Uri $BASE_URL -Method Get -ContentType "application/json"
$response1 | ConvertTo-Json -Depth 3
Write-Host ""

# Test 2: Upload a torrent (manual approach since file upload is tricky)
Write-Host "📤 Test 2: Attempting torrent upload" -ForegroundColor Yellow
Write-Host "POST $BASE_URL/upload"
try {
    $filePath = "archlinux-2025.07.01-x86_64.iso.torrent"
    $uploadResult = curl -X POST -F "file=@$filePath" "$BASE_URL/upload"
    Write-Host $uploadResult
} catch {
    Write-Host "Upload failed: $_" -ForegroundColor Red
    Write-Host "We'll use H2 console to manually insert a test torrent" -ForegroundColor Blue
}
Write-Host ""

# Test 3: List torrents again
Write-Host "📋 Test 3: List torrents after upload attempt" -ForegroundColor Yellow
Write-Host "GET $BASE_URL"
$response3 = Invoke-RestMethod -Uri $BASE_URL -Method Get -ContentType "application/json"
$response3 | ConvertTo-Json -Depth 3

if ($response3.Count -gt 0) {
    $TORRENT_ID = $response3[0].id
    Write-Host "Found torrent with ID: $TORRENT_ID" -ForegroundColor Green
} else {
    $TORRENT_ID = 1
    Write-Host "No torrents found, using ID 1 for testing" -ForegroundColor Yellow
}
Write-Host ""

# Test 4: Start download
Write-Host "🚀 Test 4: Start download for torrent ID: $TORRENT_ID" -ForegroundColor Yellow
Write-Host "POST $BASE_URL/$TORRENT_ID/start"
try {
    $startResult = Invoke-RestMethod -Uri "$BASE_URL/$TORRENT_ID/start" -Method Post -ContentType "application/json"
    Write-Host $startResult -ForegroundColor Green
} catch {
    Write-Host "Start failed: $_" -ForegroundColor Red
}
Write-Host ""

# Test 5: Check download status
Write-Host "📊 Test 5: Check download status" -ForegroundColor Yellow
Write-Host "GET $BASE_URL/$TORRENT_ID/status"
try {
    $statusResult = Invoke-RestMethod -Uri "$BASE_URL/$TORRENT_ID/status" -Method Get -ContentType "application/json"
    $statusResult | ConvertTo-Json -Depth 3
} catch {
    Write-Host "Status check failed: $_" -ForegroundColor Red
}
Write-Host ""

# Test 6: Pause download
Write-Host "⏸️  Test 6: Pause download" -ForegroundColor Yellow
Write-Host "POST $BASE_URL/$TORRENT_ID/pause"
try {
    $pauseResult = Invoke-RestMethod -Uri "$BASE_URL/$TORRENT_ID/pause" -Method Post -ContentType "application/json"
    Write-Host $pauseResult -ForegroundColor Green
} catch {
    Write-Host "Pause failed: $_" -ForegroundColor Red
}
Write-Host ""

# Test 7: Resume download
Write-Host "▶️  Test 7: Resume download" -ForegroundColor Yellow
Write-Host "POST $BASE_URL/$TORRENT_ID/resume"
try {
    $resumeResult = Invoke-RestMethod -Uri "$BASE_URL/$TORRENT_ID/resume" -Method Post -ContentType "application/json"
    Write-Host $resumeResult -ForegroundColor Green
} catch {
    Write-Host "Resume failed: $_" -ForegroundColor Red
}
Write-Host ""

# Test 8: Final status check
Write-Host "📊 Test 8: Final status check" -ForegroundColor Yellow
Write-Host "GET $BASE_URL/$TORRENT_ID/status"
try {
    $finalStatus = Invoke-RestMethod -Uri "$BASE_URL/$TORRENT_ID/status" -Method Get -ContentType "application/json"
    $finalStatus | ConvertTo-Json -Depth 3
} catch {
    Write-Host "Final status check failed: $_" -ForegroundColor Red
}
Write-Host ""

Write-Host "✅ API Testing Complete!" -ForegroundColor Green
Write-Host "========================" -ForegroundColor Green

Write-Host ""
Write-Host "🔧 Manual Testing Commands:" -ForegroundColor Cyan
Write-Host "1. H2 Console: http://localhost:8080/h2-console" -ForegroundColor White
Write-Host "2. List torrents: curl -X GET http://localhost:8080/api/torrents" -ForegroundColor White
Write-Host "3. Upload torrent: curl -X POST -F `"file=@filename.torrent`" http://localhost:8080/api/torrents/upload" -ForegroundColor White
Write-Host "4. Start download: curl -X POST http://localhost:8080/api/torrents/1/start" -ForegroundColor White
Write-Host "5. Check status: curl -X GET http://localhost:8080/api/torrents/1/status" -ForegroundColor White
Write-Host "6. Pause: curl -X POST http://localhost:8080/api/torrents/1/pause" -ForegroundColor White
Write-Host "7. Resume: curl -X POST http://localhost:8080/api/torrents/1/resume" -ForegroundColor White
