$ErrorActionPreference = 'Stop'
$repo = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
$sfx = Join-Path $repo 'assets/audio/sfx'
$music = Join-Path $repo 'assets/audio/music'
New-Item -ItemType Directory -Force -Path $sfx,$music | Out-Null
$selection = @{
    '01/A1'='ui_click.wav'; '01/B5'='ui_confirm.wav'; '01/C2'='ui_back.wav';
    '01/D2'='ui_error.wav'; '01/E1'='page_turn.wav'; '01/F1'='chest_open.wav';
    '02/G1'='longsword_swing.wav'; '03/DS1'='double_swords_swing.wav';
    '03/DS1-upgraded'='double_swords_upgraded.wav'; '03/LN1'='lance_thrust.wav';
    '03/SH1'='shield_strike.wav'; '03/WP5'='whip_lash.wav';
    '05/TH3'='warhammer_impact.wav'; '04/BW3'='bow_shot.wav'; '04/CB4'='crossbow_shot.wav';
    '04/DG4'='dagger_throw.wav'; '04/SL1'='sling_throw.wav'; '08/GH3'='boomerang_throw.wav';
    '08/BV2'='bow_volley.wav'; '08/ST1'='sling_impact.wav';
    '06/SB5'='defensive_burst.wav'; '07/AW5'='air_slash.wav'
}
$animationFitted = @('longsword_swing.wav','double_swords_swing.wav','double_swords_upgraded.wav',
    'lance_thrust.wav','shield_strike.wav','whip_lash.wav','warhammer_impact.wav','air_slash.wav')
foreach ($id in $selection.Keys) {
    $parts = $id.Split('/')
    $source = Join-Path $PSScriptRoot ("listening-pack-" + $parts[0] + '/' + $parts[1] + '.wav')
    $target = Join-Path $sfx $selection[$id]
    if (!(Test-Path -LiteralPath $source)) { throw "Missing approved preview: $id" }
    if (Test-Path -LiteralPath $target) {
        if ((Get-FileHash $source).Hash -ne (Get-FileHash $target).Hash -and $selection[$id] -notin $animationFitted) {
            throw "Existing asset differs: $target"
        }
    } else { Copy-Item -LiteralPath $source -Destination $target }
}
$trackPath = Join-Path $music 'gregorian_chant.mp3'
if (!(Test-Path -LiteralPath $trackPath)) {
    Invoke-WebRequest 'https://incompetech.com/music/royalty-free/mp3-royaltyfree/Gregorian%20Chant.mp3' -OutFile $trackPath
}
$bytes = [IO.File]::ReadAllBytes($trackPath)
if ($bytes.Length -lt 100000 -or !(([Text.Encoding]::ASCII.GetString($bytes,0,3) -eq 'ID3') -or ($bytes[0] -eq 255))) {
    throw 'The downloaded track is not an expected MP3 file'
}
Push-Location -LiteralPath $repo
try {
    & (Join-Path $repo 'gradlew.bat') -I tools/audio-audition/audition.gradle :lwjgl3:installMeleeAbilityAudio --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Approved combat audio preparation failed' }
} finally { Pop-Location }
Get-ChildItem (Join-Path $repo 'assets/audio') -Recurse -File | Get-FileHash | Select-Object Path,Hash
