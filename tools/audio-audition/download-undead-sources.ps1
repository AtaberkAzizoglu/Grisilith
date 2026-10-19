$ErrorActionPreference = 'Stop'
$dest = Join-Path $PSScriptRoot 'sources/undead-voices'
New-Item -ItemType Directory -Force -Path (Join-Path $dest 'Audio') | Out-Null
$archives = @(
    @('zombies','https://opengameart.org/sites/default/files/zombies.zip'),
    @('zombienoises','https://opengameart.org/sites/default/files/zombienoises.zip'),
    @('ghostmoans','https://opengameart.org/sites/default/files/qubodup-GhostMoans.zip')
)
foreach ($item in $archives) {
    $zip = Join-Path $dest ($item[0] + '.zip')
    if (!(Test-Path -LiteralPath $zip)) { Invoke-WebRequest -UseBasicParsing -Uri $item[1] -OutFile $zip }
    Expand-Archive -LiteralPath $zip -DestinationPath (Join-Path $dest ('Audio/' + $item[0])) -Force
}
foreach ($item in @(
    @('ghostbreath.flac','https://opengameart.org/sites/default/files/ghostbreath.flac'),
    @('female_scream.ogg','https://opengameart.org/sites/default/files/female_scream_1.ogg')
)) {
    $target = Join-Path $dest ('Audio/' + $item[0])
    if (!(Test-Path -LiteralPath $target)) { Invoke-WebRequest -UseBasicParsing -Uri $item[1] -OutFile $target }
}
$voices = @(
    @('witch','kanaizo',724988),
    @('whispers_litruv','Litruv',175944),
    @('whispers_ienba','IENBA',656439),
    @('whispers_bluesiren','BlueSiren',377743),
    @('cry_amaiguri','Amaiguri',518993),
    @('cry_purplereptar','purplereptar',449439)
)
foreach ($voice in $voices) {
    $target = Join-Path $dest ('Audio/' + $voice[0] + '.mp3')
    if (Test-Path -LiteralPath $target) { continue }
    $id = $voice[2]
    $page = (Invoke-WebRequest -UseBasicParsing -Uri ('https://freesound.org/people/' + $voice[1] + '/sounds/' + $id + '/')).Content
    $pattern = 'https://cdn\.freesound\.org/previews/' + [math]::Floor($id / 1000) + '/' + $id + '_[0-9]+-hq\.mp3'
    $preview = [regex]::Match($page,$pattern).Value
    if (!$preview) { throw ('Public preview not found: ' + $voice[0]) }
    Invoke-WebRequest -UseBasicParsing -Uri $preview -OutFile $target
}
Get-ChildItem -LiteralPath (Join-Path $dest 'Audio') -Recurse -File | Select-Object FullName,Length
