param([string]$Source="assets/weapon_visuals/carriers_v4/double_swords.png")
# Explicit user-requested mechanical split: original pixels, no redraw/resampling.
Add-Type -AssemblyName System.Drawing
$taskSource=[Drawing.Bitmap]::new((Resolve-Path -LiteralPath $Source).Path)
try {
    $taskHalf=[int][Math]::Floor($taskSource.Width/2)
    foreach($taskSide in @("left","right")) {
        $taskX=if($taskSide -eq "left"){0}else{$taskHalf}
        $taskWidth=if($taskSide -eq "left"){$taskHalf}else{$taskSource.Width-$taskHalf}
        $taskRect=[Drawing.Rectangle]::new($taskX,0,$taskWidth,$taskSource.Height)
        $taskBlade=$taskSource.Clone($taskRect,[Drawing.Imaging.PixelFormat]::Format32bppArgb)
        try { $taskBlade.Save([IO.Path]::GetFullPath("assets/weapon_visuals/carriers_v4/double_sword_$taskSide.png"),[Drawing.Imaging.ImageFormat]::Png) }
        finally { $taskBlade.Dispose() }
    }
} finally { $taskSource.Dispose() }
