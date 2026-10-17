param([string]$ProjectRoot = (Join-Path $PSScriptRoot '../..'))
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$iconRoot = [IO.Path]::GetFullPath($ProjectRoot)
$master = [Drawing.Bitmap]::new((Join-Path $iconRoot 'assets/icons/source/grisilith_app_icon_master.png'))
function Icon-Png([int]$Size) {
    $bitmap = [Drawing.Bitmap]::new($Size, $Size, [Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [Drawing.Graphics]::FromImage($bitmap)
    $graphics.Clear([Drawing.Color]::Transparent)
    $graphics.CompositingMode = [Drawing.Drawing2D.CompositingMode]::SourceCopy
    $graphics.InterpolationMode = [Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.PixelOffsetMode = [Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $graphics.DrawImage($master, [Drawing.Rectangle]::new(0, 0, $Size, $Size))
    $stream = [IO.MemoryStream]::new()
    $bitmap.Save($stream, [Drawing.Imaging.ImageFormat]::Png)
    $bytes = $stream.ToArray()
    $stream.Dispose(); $graphics.Dispose(); $bitmap.Dispose()
    return ,$bytes
}
function Save-Icon([string]$RelativePath, [byte[]]$Bytes) {
    $target = [IO.Path]::GetFullPath((Join-Path $iconRoot $RelativePath))
    if (-not $target.StartsWith($iconRoot + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) { throw 'Icon target escaped the project.' }
    [IO.Directory]::CreateDirectory([IO.Path]::GetDirectoryName($target)) | Out-Null
    [IO.File]::WriteAllBytes($target, $Bytes)
}
try {
    $sizes = @(16, 32, 48, 64, 128, 256)
    $pngs = @{}
    foreach ($size in $sizes) { $pngs[$size] = Icon-Png $size }
    foreach ($size in @(16, 32, 64, 128)) { Save-Icon "assets/icons/grisilith_icon_$size.png" $pngs[$size] }
    Save-Icon 'lwjgl3/icons/logo.png' (Icon-Png 512)
    # ICO entries plus PNG payloads, supported by Windows Vista and later.
    $stream = [IO.MemoryStream]::new(); $writer = [IO.BinaryWriter]::new($stream)
    $writer.Write([uint16]0); $writer.Write([uint16]1); $writer.Write([uint16]$sizes.Count)
    $offset = 6 + 16 * $sizes.Count
    foreach ($size in $sizes) {
        $dimension = if ($size -eq 256) { 0 } else { $size }
        $writer.Write([byte]$dimension); $writer.Write([byte]$dimension)
        $writer.Write([byte]0); $writer.Write([byte]0)
        $writer.Write([uint16]1); $writer.Write([uint16]32)
        $writer.Write([uint32]$pngs[$size].Length); $writer.Write([uint32]$offset)
        $offset += $pngs[$size].Length
    }
    foreach ($size in $sizes) { $writer.Write([byte[]]$pngs[$size]) }
    $writer.Flush()
    Save-Icon 'lwjgl3/icons/logo.ico' $stream.ToArray()
    Save-Icon 'assets/icons/grisilith.ico' $stream.ToArray()
    $writer.Dispose(); $stream.Dispose()
    # ICNS tagged PNG representations, with big-endian chunk lengths.
    $chunks = @(@('ic07', 128), @('ic08', 256), @('ic09', 512), @('ic10', 1024))
    $payloads = @($chunks | ForEach-Object { ,(Icon-Png $_[1]) })
    $stream = [IO.MemoryStream]::new(); $writer = [IO.BinaryWriter]::new($stream)
    function Write-BigEndian([uint32]$Number) {
        $bytes = [BitConverter]::GetBytes($Number); [Array]::Reverse($bytes); $writer.Write($bytes)
    }
    $writer.Write([Text.Encoding]::ASCII.GetBytes('icns'))
    Write-BigEndian (8 + ($payloads | ForEach-Object { $_.Length + 8 } | Measure-Object -Sum).Sum)
    for ($i = 0; $i -lt $chunks.Count; $i++) {
        $writer.Write([Text.Encoding]::ASCII.GetBytes($chunks[$i][0]))
        Write-BigEndian ($payloads[$i].Length + 8); $writer.Write([byte[]]$payloads[$i])
    }
    $writer.Flush(); Save-Icon 'lwjgl3/icons/logo.icns' $stream.ToArray()
    $writer.Dispose(); $stream.Dispose()
    $densities = @{ mdpi = 48; hdpi = 72; xhdpi = 96; xxhdpi = 144; xxxhdpi = 192 }
    foreach ($tree in @('android/res', 'android/src/main/res')) {
        foreach ($density in $densities.Keys) { Save-Icon "$tree/drawable-$density/ic_launcher.png" (Icon-Png $densities[$density]) }
    }
    Save-Icon 'android/res/drawable-nodpi/ic_launcher_emblem.png' (Icon-Png 512)
    Save-Icon 'android/ic_launcher-web.png' (Icon-Png 512)
    Write-Output 'APP_ICONS_SYNCED: Windows, desktop window, macOS and Android use the Grisilith emblem.'
} finally { $master.Dispose() }
