$ErrorActionPreference = 'Stop'
$auditionRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot 'sources'))
New-Item -ItemType Directory -Force -Path $auditionRoot | Out-Null
Add-Type -AssemblyName System.IO.Compression.FileSystem
foreach ($pack in @('interface-sounds','rpg-audio','impact-sounds')) {
    $pageUrl = "https://kenney.nl/assets/$pack"
    $html = (Invoke-WebRequest $pageUrl -UseBasicParsing).Content
    $url = [regex]::Match($html, "https://kenney\.nl/[^'\""\s]+\.zip").Value
    if (!$url) { throw "No official archive found: $pack" }
    $archivePath = Join-Path $auditionRoot "$pack.zip"
    $target = [IO.Path]::GetFullPath((Join-Path $auditionRoot $pack))
    if (!(Test-Path -LiteralPath $archivePath)) { Invoke-WebRequest $url -OutFile $archivePath }
    if (!(Test-Path -LiteralPath $target)) {
        $zip = [IO.Compression.ZipFile]::OpenRead($archivePath)
        try {
            foreach ($entry in $zip.Entries) {
                $resolved = [IO.Path]::GetFullPath((Join-Path $target $entry.FullName))
                if (!$resolved.StartsWith($target + [IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)) {
                    throw 'Archive entry escaped the selected pack directory'
                }
            }
        } finally { $zip.Dispose() }
        [IO.Compression.ZipFile]::ExtractToDirectory($archivePath,$target)
    }
    Write-Output "$pack : $url"
    Get-ChildItem -LiteralPath $target -Recurse -File | Where-Object Extension -in '.ogg','.wav','.txt' | ForEach-Object {
        Write-Output $_.FullName.Substring($target.Length+1)
    }
}
