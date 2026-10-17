$ErrorActionPreference='Stop'
Add-Type -AssemblyName System.Drawing
$inputImage=[Drawing.Bitmap]::new((Join-Path $PSScriptRoot 'previews/tamga_status_atlas.png'))
$dest=Join-Path $PSScriptRoot '../../assets/tamga_visuals/status_icons'
New-Item -ItemType Directory -Force -Path $dest | Out-Null
$names=@('charger','height_insecurity','phantom_steed','holy_lights','arcane_aura','flame_blades','quick_shot','panic')
for($i=0;$i -lt 8;$i++) {
 $x0=[int][Math]::Floor(($i%4)*$inputImage.Width/4)
 $y0=[int][Math]::Floor([Math]::Floor($i/4)*$inputImage.Height/2)
 $w=[int][Math]::Floor($inputImage.Width/4); $h=[int][Math]::Floor($inputImage.Height/2)
 $tile=[Drawing.Bitmap]::new(128,128)
 $g=[Drawing.Graphics]::FromImage($tile)
 $g.InterpolationMode=[Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
 $g.DrawImage($inputImage,[Drawing.Rectangle]::new(4,4,120,120),[Drawing.Rectangle]::new($x0,$y0,$w,$h),[Drawing.GraphicsUnit]::Pixel)
 $tile.Save((Join-Path $dest ($names[$i]+'.png')),[Drawing.Imaging.ImageFormat]::Png)
 $g.Dispose();$tile.Dispose()
}
$inputImage.Dispose()
