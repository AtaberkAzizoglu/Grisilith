$ErrorActionPreference = 'Stop'
$sourceRoot = Join-Path $PSScriptRoot 'sources'
$archives = @(
    @{ Url='https://opengameart.org/sites/default/files/water-splash-slime-sfx.zip'; Name='water-splash.zip'; Folder='water-splash/Audio' },
    @{ Url='https://opengameart.org/sites/default/files/dark_magic.7z'; Name='dark_magic.7z'; Folder='dark-magic/Audio' },
    @{ Url='https://opengameart.org/sites/default/files/spells.7z'; Name='spells.7z'; Folder='fire-evil/Audio' }
)
foreach ($source in $archives) {
    $archivePath = Join-Path $sourceRoot $source.Name
    $destinationPath = Join-Path $sourceRoot $source.Folder
    New-Item -ItemType Directory -Force -Path $destinationPath | Out-Null
    if (-not (Test-Path -LiteralPath $archivePath)) { Invoke-WebRequest $source.Url -OutFile $archivePath }
    if ($source.Name.EndsWith('.zip')) {
        Expand-Archive -LiteralPath $archivePath -DestinationPath $destinationPath -Force
    } else {
        & tar -xf $archivePath -C $destinationPath
        if ($LASTEXITCODE -ne 0) { throw "Could not extract $archivePath" }
    }
}
$oceanPath = Join-Path $sourceRoot 'ocean-splash/Audio'
New-Item -ItemType Directory -Force -Path $oceanPath | Out-Null
$oceanFile = Join-Path $oceanPath 'cannon_miss.ogg'
if (-not (Test-Path -LiteralPath $oceanFile)) {
    Invoke-WebRequest 'https://opengameart.org/sites/default/files/cannon_miss_0.ogg' -OutFile $oceanFile
}
Write-Output 'Preview sources ready. Sources/licenses: magic-refinement-source-credits.txt. No game assets changed.'
