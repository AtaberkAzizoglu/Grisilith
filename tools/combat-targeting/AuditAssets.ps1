$ErrorActionPreference='Stop'
$root=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
$sources=Get-ChildItem -LiteralPath (Join-Path $root 'core/src'),(Join-Path $root 'lwjgl3/src'),(Join-Path $root 'android/src') -Recurse -File | Where-Object Extension -in '.kt','.java','.json','.xml'
$code=($sources | ForEach-Object { Get-Content -LiteralPath $_.FullName -Raw }) -join "`n"
$files=Get-ChildItem -LiteralPath (Join-Path $root 'assets') -Recurse -File | Where-Object Extension -in '.png','.jpg','.jpeg','.webp'
$dynamicPrefixes=[Collections.Generic.List[string]]::new()
foreach($match in [regex]::Matches($code,'"([^"\r\n]*\$[^"\r\n]*)"')) {
 $v=$match.Groups[1].Value
 if($v.Contains('/')) { $prefix=$v.Substring(0,$v.IndexOf('$'));if($prefix.Length -gt 5){$dynamicPrefixes.Add($prefix)} }
}
foreach($match in [regex]::Matches($code,'"([^"\r\n]+/)"')) {
 $prefix=$match.Groups[1].Value
 # UI component names are explicit and can be audited separately; actor strips are dynamic.
 if($prefix -ne 'ui/components/' -and $prefix.Length -gt 5){$dynamicPrefixes.Add($prefix)}
}
$candidates=@()
foreach($file in $files) {
 $rel=[IO.Path]::GetRelativePath((Join-Path $root 'assets'),$file.FullName).Replace('\','/')
 if($code.Contains($rel) -or $code.Contains($file.Name) -or $code.Contains('"'+$file.BaseName+'"')){continue}
 if($dynamicPrefixes | Where-Object { $rel.StartsWith($_) }){continue}
 $candidates+=[pscustomobject]@{path=$rel;bytes=$file.Length}
}
Write-Output ('Scanned '+$files.Count+' image files; '+$candidates.Count+' require reference review (not automatic deletion).')
$candidates | ForEach-Object { Write-Output $_.path }
