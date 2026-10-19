$ErrorActionPreference = 'Stop'
$auditionRoot = $PSScriptRoot
$archivePath = Join-Path $auditionRoot 'sources/whistles.zip'
$audioPath = Join-Path $auditionRoot 'sources/dklon-whistles/Audio'
# Public original archive; source/attribution: https://opengameart.org/content/whistles
# CC BY 3.0, credit dklon. Preview-only unless the user selects a recording for shipping.
if (-not (Test-Path -LiteralPath $archivePath)) {
    Invoke-WebRequest 'https://opengameart.org/sites/default/files/whistles.zip' -OutFile $archivePath
}
Expand-Archive -LiteralPath $archivePath -DestinationPath $audioPath -Force
1..4 | ForEach-Object {
    $recording = Join-Path $audioPath "whistle_$_.wav"
    if (-not (Test-Path -LiteralPath $recording)) { throw "Missing whistle recording: $recording" }
}
Write-Output 'Four original dklon whistle recordings ready (CC BY 3.0).'
