$ErrorActionPreference = 'Stop'
$sourceRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot 'sources/stillnorth-textures'))
$audioTarget = Join-Path $sourceRoot 'Audio'
New-Item -ItemType Directory -Force -Path $audioTarget | Out-Null
$page = 'https://opengameart.org/content/medieval-sound-effects-weapon-textures'
if (!(Test-Path -LiteralPath (Join-Path $sourceRoot 'source-page.html'))) {
    Invoke-WebRequest $page -UseBasicParsing -OutFile (Join-Path $sourceRoot 'source-page.html')
}
$archive = Join-Path $sourceRoot 'medieval_sfx_textures_1_of_2.7z'
if (!(Test-Path -LiteralPath $archive)) {
    Invoke-WebRequest 'https://opengameart.org/sites/default/files/medieval_sfx_textures_1_of_2.7z' -OutFile $archive
}
$entries = @(& tar -tf $archive)
if ($LASTEXITCODE -ne 0) { throw 'Could not list the source archive' }
$selected = @($entries | Where-Object { $_ -match '(?i)(crossbow.*shoot|longbow.*shoot|dagger.*swing|recurve.*shoot)' -and $_ -match '\.wav$' })
foreach ($entry in $selected) {
    $resolved = [IO.Path]::GetFullPath((Join-Path $audioTarget $entry))
    if (!$resolved.StartsWith([IO.Path]::GetFullPath($audioTarget) + [IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)) { throw 'Archive path escapes selected directory' }
    if (!(Test-Path -LiteralPath $resolved)) {
        & tar -xf $archive -C $audioTarget -- $entry
        if ($LASTEXITCODE -ne 0) { throw 'Could not extract selected audio' }
    }
}
$selected
