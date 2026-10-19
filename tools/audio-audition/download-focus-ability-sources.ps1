$ErrorActionPreference = 'Stop'
$sourceRoot = Join-Path $PSScriptRoot 'sources/focus-ability/Audio'
New-Item -ItemType Directory -Force -Path $sourceRoot | Out-Null
$files = @{
    'teleport.wav' = 'https://opengameart.org/sites/default/files/teleport.wav'
    'acid_bubble.wav' = 'https://opengameart.org/sites/default/files/Acid%20Bubble.wav'
    'thunder.wav' = 'https://opengameart.org/sites/default/files/thunderclap_0.ogg'
    'fire.wav' = 'https://opengameart.org/sites/default/files/fire.wav'
    'boil_cover.ogg' = 'https://opengameart.org/sites/default/files/cooking_with_cover_01.ogg'
    'boil_open.ogg' = 'https://opengameart.org/sites/default/files/cooking_without_cover_01_0.ogg'
}
foreach ($name in $files.Keys) {
    $destination = Join-Path $sourceRoot $name
    if (-not (Test-Path -LiteralPath $destination)) { Invoke-WebRequest $files[$name] -OutFile $destination }
}
Write-Output 'Preview-only source recordings downloaded; licenses recorded in focus-ability-source-credits.txt.'
