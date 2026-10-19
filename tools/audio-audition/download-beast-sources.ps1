$ErrorActionPreference = 'Stop'
$sourceRoot = Join-Path $PSScriptRoot 'sources/beast-voices'
$audioRoot = Join-Path $sourceRoot 'Audio'
New-Item -ItemType Directory -Force -Path $audioRoot | Out-Null
$archives = @{
    'bear.zip' = 'https://opengameart.org/sites/default/files/bear.zip'
    'gorilla.zip' = 'https://opengameart.org/sites/default/files/gorilla.zip'
    'eagle.7z' = 'https://opengameart.org/sites/default/files/pla1554alaska-ccby-gpl-eagle.7z'
    'bee.zip' = 'https://opengameart.org/sites/default/files/bee.zip'
    'dog.zip' = 'https://opengameart.org/sites/default/files/archive_1.zip'
}
foreach ($name in $archives.Keys) {
    $archive = Join-Path $sourceRoot $name
    if (-not (Test-Path -LiteralPath $archive)) { Invoke-WebRequest -UseBasicParsing $archives[$name] -OutFile $archive }
    $extract = Join-Path $audioRoot ([IO.Path]::GetFileNameWithoutExtension($name))
    New-Item -ItemType Directory -Force -Path $extract | Out-Null
    if ($name.EndsWith('.zip')) { Expand-Archive -LiteralPath $archive -DestinationPath $extract -Force }
    else { & tar -xf $archive -C $extract; if ($LASTEXITCODE -ne 0) { throw "Cannot extract $archive" } }
}
$files = @{'boar.ogg'='https://opengameart.org/sites/default/files/boar_0.ogg'}
foreach ($name in @('ribbit_01','ribbit_02','ribbit_03','pig_idle','pig_idle2','pig_idle3','pig_idle4')) {
    $files["$name.mp3"]="https://opengameart.org/sites/default/files/$name.mp3"
}
foreach ($name in $files.Keys) {
    $destination = Join-Path $audioRoot $name
    if (-not (Test-Path -LiteralPath $destination)) { Invoke-WebRequest -UseBasicParsing $files[$name] -OutFile $destination }
}
$crocPath = Join-Path $audioRoot 'alligator.mp3'
if (-not (Test-Path -LiteralPath $crocPath)) {
    $html = (Invoke-WebRequest -UseBasicParsing 'https://freesound.org/people/D.jones/sounds/527844/').Content
    $preview = [regex]::Match($html,'https://cdn\.freesound\.org/previews/527/527844_[0-9]+-hq\.mp3').Value
    if (-not $preview) { throw 'No verified public alligator HQ preview URL in source page' }
    Invoke-WebRequest -UseBasicParsing $preview -OutFile $crocPath
}
Get-ChildItem -LiteralPath $audioRoot -Recurse -File | Select-Object -ExpandProperty FullName
