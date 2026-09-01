# analyze.ps1 - Local code quality analysis
# Usage: ./analyze.ps1

$ErrorActionPreference = 'Continue'
$success = $true

function Write-Step($n, $msg) {
    Write-Host "`n[$n/3] $msg" -ForegroundColor Cyan
}
function Write-Ok($msg)   { Write-Host "  OK  $msg" -ForegroundColor Green }
function Write-Warn($msg) { Write-Host "  WARN $msg" -ForegroundColor Yellow }
function Write-Fail($msg) { Write-Host "  FAIL $msg" -ForegroundColor Red }

Write-Host "============================================" -ForegroundColor Magenta
Write-Host "   BackupManager - Code Quality Analysis   " -ForegroundColor Magenta
Write-Host "============================================" -ForegroundColor Magenta

# --- 1. Tests + JaCoCo ---
Write-Step 1 "Tests + Coverage (JaCoCo)"
./mvnw clean verify "-Dmaven.test.failure.ignore=true" -q
if ($LASTEXITCODE -eq 0) { Write-Ok "Build OK" } else { Write-Warn "Build had issues (coverage may be partial)" }

# --- 2. SpotBugs ---
Write-Step 2 "Bug & Vulnerability Analysis (SpotBugs)"
./mvnw spotbugs:spotbugs -q
if ($LASTEXITCODE -eq 0) { Write-Ok "SpotBugs OK" } else { Write-Warn "SpotBugs completed with findings" }

# --- 3. PMD + CPD ---
Write-Step 3 "Complexity & Duplication (PMD + CPD)"
./mvnw pmd:pmd pmd:cpd -q
if ($LASTEXITCODE -eq 0) { Write-Ok "PMD OK" } else { Write-Warn "PMD completed with findings" }

# --- Summary ---
Write-Host "`n============================================" -ForegroundColor Magenta
Write-Host "   Results Summary" -ForegroundColor Magenta
Write-Host "============================================" -ForegroundColor Magenta

# JaCoCo
$jacocoXml = "target/site/jacoco/jacoco.xml"
if (Test-Path $jacocoXml) {
    [xml]$xml = Get-Content $jacocoXml
    $lineCounter = $xml.report.counter | Where-Object { $_.type -eq "LINE" }
    if ($lineCounter) {
        $covered = [int]$lineCounter.covered
        $missed  = [int]$lineCounter.missed
        $total   = $covered + $missed
        $pct     = if ($total -gt 0) { [math]::Round(($covered / $total) * 100) } else { 0 }
        $icon    = if ($pct -ge 80) { "[OK]" } elseif ($pct -ge 60) { "[!!]" } else { "[XX]" }
        Write-Host "  $icon Test Coverage  : $pct% ($covered/$total lines)" -ForegroundColor $(if ($pct -ge 80) { "Green" } elseif ($pct -ge 60) { "Yellow" } else { "Red" })
    }
} else {
    Write-Host "  [--] Test Coverage  : no data" -ForegroundColor DarkGray
}

# SpotBugs
$spotbugsXml = "target/spotbugsXml.xml"
if (Test-Path $spotbugsXml) {
    [xml]$xml = Get-Content $spotbugsXml
    $bugs  = $xml.BugCollection.BugInstance
    $high  = ($bugs | Where-Object { $_.priority -eq "1" } | Measure-Object).Count
    $med   = ($bugs | Where-Object { $_.priority -eq "2" } | Measure-Object).Count
    $low   = ($bugs | Where-Object { $_.priority -eq "3" } | Measure-Object).Count
    $total = $high + $med + $low
    $icon  = if ($high -gt 0) { "[XX]" } elseif ($med -gt 0) { "[!!]" } else { "[OK]" }
    $color = if ($high -gt 0) { "Red" } elseif ($med -gt 0) { "Yellow" } else { "Green" }
    Write-Host "  $icon SpotBugs       : $total bugs  (High: $high  Med: $med  Low: $low)" -ForegroundColor $color
} else {
    Write-Host "  [--] SpotBugs       : no data" -ForegroundColor DarkGray
}

# PMD
$pmdXml = "target/pmd.xml"
if (Test-Path $pmdXml) {
    [xml]$xml = Get-Content $pmdXml
    $count = ($xml.pmd.file.violation | Measure-Object).Count
    $icon  = if ($count -eq 0) { "[OK]" } elseif ($count -lt 20) { "[!!]" } else { "[XX]" }
    $color = if ($count -eq 0) { "Green" } elseif ($count -lt 20) { "Yellow" } else { "Red" }
    Write-Host "  $icon PMD Complexity : $count violations" -ForegroundColor $color
} else {
    Write-Host "  [--] PMD Complexity : no data" -ForegroundColor DarkGray
}

# CPD
$cpdXml = "target/cpd.xml"
if (Test-Path $cpdXml) {
    [xml]$xml = Get-Content $cpdXml
    $count = ($xml.'pmd-cpd'.duplication | Measure-Object).Count
    $icon  = if ($count -eq 0) { "[OK]" } elseif ($count -lt 5) { "[!!]" } else { "[XX]" }
    $color = if ($count -eq 0) { "Green" } elseif ($count -lt 5) { "Yellow" } else { "Red" }
    Write-Host "  $icon CPD Duplication: $count duplicated blocks (>100 tokens)" -ForegroundColor $color
} else {
    Write-Host "  [--] CPD Duplication: no data" -ForegroundColor DarkGray
}

Write-Host ""
Write-Host "Open reports:" -ForegroundColor DarkGray
if (Test-Path "target/site/jacoco/index.html") {
    Write-Host "  Coverage   : " -NoNewline -ForegroundColor DarkGray
    Write-Host "Start-Process target/site/jacoco/index.html" -ForegroundColor White
} else {
    Write-Host "  Coverage   : (not generated)" -ForegroundColor DarkGray
}
Write-Host "  SpotBugs   : " -NoNewline -ForegroundColor DarkGray
Write-Host "./mvnw spotbugs:gui" -ForegroundColor White
if (Test-Path "target/pmd.xml") {
    Write-Host "  PMD        : " -NoNewline -ForegroundColor DarkGray
    Write-Host "Start-Process target/pmd.xml" -ForegroundColor White
} else {
    Write-Host "  PMD        : (not generated)" -ForegroundColor DarkGray
}
if (Test-Path "target/cpd.xml") {
    Write-Host "  CPD        : " -NoNewline -ForegroundColor DarkGray
    Write-Host "Start-Process target/cpd.xml" -ForegroundColor White
} else {
    Write-Host "  CPD        : (not generated)" -ForegroundColor DarkGray
}
Write-Host ""
