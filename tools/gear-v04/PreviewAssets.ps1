Add-Type -AssemblyName System.Drawing
$gearManifest = Get-Content -Raw "$PSScriptRoot/generated-assets.json" | ConvertFrom-Json
$gearPreview = New-Object System.Drawing.Bitmap 1600,1280
$gearGraphics = [System.Drawing.Graphics]::FromImage($gearPreview)
$gearGraphics.Clear([System.Drawing.Color]::FromArgb(35,32,28))
$gearGraphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
$gearFont = New-Object System.Drawing.Font 'Arial',13
$gearBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::White)
$gearIndex = 0
foreach($gearAsset in $gearManifest) {
    $gearPath = Join-Path (Get-Location) $gearAsset.target
    $gearImage = [System.Drawing.Image]::FromFile($gearPath)
    $gearBitmap = [System.Drawing.Bitmap]$gearImage
    if($gearBitmap.GetPixel(0,0).A -gt 2) { throw "Asset corner is not transparent: $($gearAsset.key)" }
    $gearX = ($gearIndex % 5) * 320
    $gearY = [int][Math]::Floor($gearIndex / 5) * 320
    $gearGraphics.DrawImage($gearImage,$gearX+20,$gearY+35,280,280)
    $gearGraphics.DrawString($gearAsset.key,$gearFont,$gearBrush,$gearX+8,$gearY+8)
    $gearImage.Dispose()
    $gearIndex++
}
$gearPreview.Save("$PSScriptRoot/assets-preview.png",[System.Drawing.Imaging.ImageFormat]::Png)
$gearGraphics.Dispose(); $gearPreview.Dispose(); $gearFont.Dispose(); $gearBrush.Dispose()
Write-Output "ASSET_ALPHA_PASS: $gearIndex transparent generated assets; preview saved."
