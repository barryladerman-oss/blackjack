# Blackjack Android Client - API Test Script
Write-Host "`nBlackjack API Test Suite`n" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080"
$passed = 0
$failed = 0

function Test-Api {
    param([string]$Name, [string]$Method, [string]$Url)
    Write-Host "Testing: $Name" -ForegroundColor Yellow
    try {
        $cmd = if ($Method -eq "POST") { "curl.exe `"$Url`" -X POST -s 2>&1" } else { "curl.exe `"$Url`" -s 2>&1" }
        $response = Invoke-Expression $cmd
        $json = $response | ConvertFrom-Json -ErrorAction Stop
        Write-Host "  [PASS] ID: $($json.id) Phase: $($json.phase)" -ForegroundColor Green
        $script:passed++
        return $json
    } catch {
        Write-Host "  [FAIL] $($_.Exception.Message)" -ForegroundColor Red
        $script:failed++
        return $null
    }
}

# Run tests
$game = Test-Api "Start Game" "POST" "$baseUrl/api/blackjack/start?hands=1"
if ($game) {
    $id = $game.id
    Test-Api "Get State" "GET" "$baseUrl/api/blackjack/state/$id"
    $hit = Test-Api "Hit Hand" "POST" "$baseUrl/api/blackjack/hit/$id`?hand=0"
    if ($hit -and -not $hit.hands[0].busted) {
        Test-Api "Stand Hand" "POST" "$baseUrl/api/blackjack/stand/$id`?hand=0"
    }
}

Write-Host "`nResults: $passed passed, $failed failed" -ForegroundColor Cyan
Write-Host "`nAndroid Config:" -ForegroundColor Yellow
Write-Host "  Emulator: http://10.0.2.2:8080/"
Write-Host "  Device: http://YOUR_IP:8080/`n"
