$ErrorActionPreference = 'Stop'
$sourceRoot = Join-Path $PSScriptRoot 'sources/beast-traits'
$audioRoot = Join-Path $sourceRoot 'Audio'
New-Item -ItemType Directory -Force -Path $audioRoot | Out-Null
$archive = Join-Path $sourceRoot 'breaking.zip'
if (-not (Test-Path -LiteralPath $archive)) {
    Invoke-WebRequest -UseBasicParsing 'https://opengameart.org/sites/default/files/sfx_breaking_and_falling.zip' -OutFile $archive
}
Expand-Archive -LiteralPath $archive -DestinationPath $audioRoot -Force
$egg = Join-Path $audioRoot 'egg.mp3'
if (-not (Test-Path -LiteralPath $egg)) {
    $page = (Invoke-WebRequest -UseBasicParsing 'https://freesound.org/people/Anthousai/sounds/336612/').Content
    $preview = [regex]::Match($page,'https://cdn\.freesound\.org/previews/336/336612_[0-9]+-hq\.mp3').Value
    if (-not $preview) { throw 'Verified public HQ egg preview not found' }
    Invoke-WebRequest -UseBasicParsing $preview -OutFile $egg
}
Get-ChildItem -LiteralPath $audioRoot -Recurse -File | Select-Object -ExpandProperty FullName
