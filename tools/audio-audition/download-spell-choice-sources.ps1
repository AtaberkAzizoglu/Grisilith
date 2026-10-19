$ErrorActionPreference = 'Stop'
$sourceRoot = Join-Path $PSScriptRoot 'sources/spell-choices'
$audioRoot = Join-Path $sourceRoot 'Audio'
New-Item -ItemType Directory -Force -Path $audioRoot | Out-Null
$files = @{
    'magic_words.wav'='https://opengameart.org/sites/default/files/magic_words.wav'
    'goblin_scream.wav'='https://opengameart.org/sites/default/files/Goblin%20Scream.wav'
    'wave_01.flac'='https://opengameart.org/sites/default/files/wave_01_cc0-11505__transitking__wavesound.flac'
    'wave_02.flac'='https://opengameart.org/sites/default/files/wave_02_cc0-11505__transitking__wavesound.flac'
    'wave_03.flac'='https://opengameart.org/sites/default/files/wave_03_cc0-11505__transitking__wavesound.flac'
}
foreach ($name in $files.Keys) {
    $destination=Join-Path $audioRoot $name
    if (-not (Test-Path -LiteralPath $destination)) { Invoke-WebRequest $files[$name] -OutFile $destination }
}
$archives = @{
    'mudchuteanimals.7z'='https://opengameart.org/sites/default/files/mudchuteanimals.7z'
    'vocal_samples.zip'='https://opengameart.org/sites/default/files/vocal_samples.zip'
    'explosions.7z'='https://opengameart.org/sites/default/files/explosions.7z'
    'male-test1_0.zip'='https://opengameart.org/sites/default/files/male-test1_0.zip'
}
foreach ($name in $archives.Keys) {
    $archive=Join-Path $sourceRoot $name
    if (-not (Test-Path -LiteralPath $archive)) { Invoke-WebRequest $archives[$name] -OutFile $archive }
    if ($name.EndsWith('.zip')) { Expand-Archive -LiteralPath $archive -DestinationPath $audioRoot -Force }
    else {
        & tar -xf $archive -C $audioRoot
        if ($LASTEXITCODE -ne 0) { throw "Cannot extract $archive" }
    }
}
Write-Output 'Preview-only source recordings ready; no game assets installed.'
Get-ChildItem $audioRoot -Recurse -File | Select-Object -ExpandProperty FullName
