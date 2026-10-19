$ErrorActionPreference = 'Stop'
$sourceDir = Join-Path $PSScriptRoot 'sources/vampire-preview'
$audioDir = Join-Path $sourceDir 'Audio'
New-Item -ItemType Directory -Force -Path $audioDir | Out-Null
$packages = @(
    @('monster2','https://opengameart.org/content/monster-sound-effects-2','zip'),
    @('monster-new','https://opengameart.org/content/monster-sfx','zip'),
    @('wet','https://opengameart.org/content/8-wet-squish-slurp-impacts','7z')
)
foreach ($item in $packages) {
    $archive = Join-Path $sourceDir ($item[0] + '.' + $item[2])
    if (!(Test-Path -LiteralPath $archive)) {
        $html = (Invoke-WebRequest -UseBasicParsing -Uri $item[1]).Content
        $link = [regex]::Match($html,'https://opengameart\.org/sites/default/files/[^"<> ]+\.' + $item[2]).Value
        if (!$link) { throw ('Archive link not found: ' + $item[0]) }
        Invoke-WebRequest -UseBasicParsing -Uri ([System.Net.WebUtility]::HtmlDecode($link)) -OutFile $archive
    }
    $extractDir = Join-Path $audioDir $item[0]
    New-Item -ItemType Directory -Force -Path $extractDir | Out-Null
    if ($item[2] -eq 'zip') { Expand-Archive -LiteralPath $archive -DestinationPath $extractDir -Force }
    else { & tar -xf $archive -C $extractDir; if ($LASTEXITCODE -ne 0) { throw 'Cannot unpack wet foley' } }
}
$rumble = Join-Path $audioDir 'rumble.flac'
if (!(Test-Path -LiteralPath $rumble)) {
    $html = (Invoke-WebRequest -UseBasicParsing -Uri 'https://opengameart.org/content/rumbleexplosion').Content
    $link = [regex]::Match($html,'https://opengameart\.org/sites/default/files/[^/"<> ]+\.flac').Value
    if (!$link) { throw 'Rumble source link not found' }
    Invoke-WebRequest -UseBasicParsing -Uri $link -OutFile $rumble
}
foreach ($voice in @(
    @('lick','Breviceps',445974),
    @('slurp','Breviceps',573583),
    @('fast-slurp','needleballista',728585),
    @('comic-lick','ManuelGraf',176798)
)) {
    $target = Join-Path $audioDir ($voice[0] + '.mp3')
    if (Test-Path -LiteralPath $target) { continue }
    $id = $voice[2]
    $html = (Invoke-WebRequest -UseBasicParsing -Uri ('https://freesound.org/people/' + $voice[1] + '/sounds/' + $id + '/')).Content
    $pattern = 'https://cdn\.freesound\.org/previews/' + [math]::Floor($id/1000) + '/' + $id + '_[0-9]+-hq\.mp3'
    $link = [regex]::Match($html,$pattern).Value
    if (!$link) { throw ('Public preview link not found: ' + $voice[0]) }
    Invoke-WebRequest -UseBasicParsing -Uri $link -OutFile $target
}
Get-ChildItem -LiteralPath $audioDir -Recurse -File | ForEach-Object { $_.FullName }
