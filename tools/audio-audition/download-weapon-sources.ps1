$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$sourceRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot 'sources'))
foreach ($pack in @(
    @{Name='swishes'; Page='https://opengameart.org/content/swishes-sound-pack'; File='https://opengameart.org/sites/default/files/swishes.zip'},
    @{Name='tinysized'; Page='https://opengameart.org/content/fantasy-sound-effects-tinysized-sfx'; File='https://opengameart.org/sites/default/files/tinysized.zip'},
    @{Name='reemax-whip'; Page='https://opengameart.org/content/whip-sound'; File='https://opengameart.org/sites/default/files/whip.wav'},
    @{Name='thwack'; Page='https://opengameart.org/content/thwack-sounds'; File='https://opengameart.org/sites/default/files/thwack-1.0.zip'}
)) {
    $target = Join-Path $sourceRoot $pack.Name
    $audioTarget = [IO.Path]::GetFullPath((Join-Path $target 'Audio'))
    New-Item -ItemType Directory -Force -Path $audioTarget | Out-Null
    $pagePath = Join-Path $target 'source-page.html'
    if (!(Test-Path -LiteralPath $pagePath)) { Invoke-WebRequest $pack.Page -UseBasicParsing -OutFile $pagePath }
    $extension = [IO.Path]::GetExtension($pack.File)
    $download = Join-Path $sourceRoot ($pack.Name + $extension)
    if (!(Test-Path -LiteralPath $download)) { Invoke-WebRequest $pack.File -OutFile $download }
    if ($extension -eq '.zip') {
        $archive = [IO.Compression.ZipFile]::OpenRead($download)
        try {
            foreach ($entry in $archive.Entries) {
                if ($entry.FullName -match '(^|/)__MACOSX/|(^|/)\._') { continue }
                if ([IO.Path]::GetExtension($entry.FullName) -notin '.wav','.ogg','.txt') { continue }
                $resolved = [IO.Path]::GetFullPath((Join-Path $audioTarget $entry.FullName))
                if (!$resolved.StartsWith($audioTarget + [IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)) { throw 'Archive entry escaped its directory' }
                if (!(Test-Path -LiteralPath $resolved)) {
                    New-Item -ItemType Directory -Force -Path ([IO.Path]::GetDirectoryName($resolved)) | Out-Null
                    [IO.Compression.ZipFileExtensions]::ExtractToFile($entry,$resolved,$false)
                }
            }
        } finally { $archive.Dispose() }
    } else {
        $destination = Join-Path $audioTarget 'whip.wav'
        if (!(Test-Path -LiteralPath $destination)) { Copy-Item -LiteralPath $download -Destination $destination }
    }
    Write-Output $pack.Name
    Get-ChildItem -LiteralPath $audioTarget -Recurse -File | ForEach-Object { $_.FullName.Substring($audioTarget.Length+1) }
}
