$ErrorActionPreference='Stop'
$root=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
$dest=Join-Path $root 'assets/characters/aligned_v4'
New-Item -ItemType Directory -Force -Path $dest | Out-Null
$names=@('sir_imbert','ywaleth_yoster','lavena_vol','koba','linlos_dustman')
foreach($name in $names) {
 & (Join-Path $root 'tools/character_visuals/ImportAlignedAtlas.ps1') -InputPath (Join-Path $PSScriptRoot ('previews/'+$name+'.png')) -OutputPath (Join-Path $dest ($name+'.png')) -Columns 0,280,561,841,1122,1402 -Rows 0,280,561,841,1122 -CellSize 256 -ConnectedSprites
}
Add-Type -AssemblyName System.Drawing
$lance=[Drawing.Bitmap]::new((Join-Path $PSScriptRoot 'previews/lance.png'))
$lanceDest=Join-Path $root 'assets/weapon_visuals/lance_spin_v4'
New-Item -ItemType Directory -Force -Path $lanceDest | Out-Null
for($i=0;$i -lt 16;$i++) {
 $x=[int][Math]::Floor(($i%4)*$lance.Width/4); $y=[int][Math]::Floor([Math]::Floor($i/4)*$lance.Height/4)
 $w=[int][Math]::Floor($lance.Width/4);$h=[int][Math]::Floor($lance.Height/4)
 $frame=$lance.Clone([Drawing.Rectangle]::new($x,$y,$w,$h),[Drawing.Imaging.PixelFormat]::Format32bppArgb)
 $frame.Save((Join-Path $lanceDest ('frame_'+$i+'.png')),[Drawing.Imaging.ImageFormat]::Png);$frame.Dispose()
}
$lance.Dispose()
