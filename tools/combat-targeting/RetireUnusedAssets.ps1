$ErrorActionPreference='Stop'
$root=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
$assetRoot=[IO.Path]::GetFullPath((Join-Path $root 'assets'))+[IO.Path]::DirectorySeparatorChar
$archiveRoot=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot 'retired-assets'))+[IO.Path]::DirectorySeparatorChar
$audit=& (Join-Path $PSScriptRoot 'AuditAssets.ps1')
# Only reviewed static UI files and replaced/source-only families, not ambiguous dynamic actors.
$planned=@($audit | Where-Object { $_ -match '^(ui/components/|map/obstacles/road_to_home_obstacles\.png$|map/obstacles/trouble_at_home_house\.png$|map/tiles/road_to_home_grass_source\.png$|icons/source/grisilith_app_icon_master\.png$|ui/animations/page_corner_arrows\.png$)' })
$families=@('characters/aligned','weapon_visuals/melee_v2','tamga_visuals/status_icons','tamga_visuals/phantom_steed')
foreach($family in $families) {
 $dir=Join-Path $assetRoot $family
 if(Test-Path -LiteralPath $dir) {
  $planned+=Get-ChildItem -LiteralPath $dir -File -Recurse | ForEach-Object { [IO.Path]::GetRelativePath($assetRoot,$_.FullName).Replace('\','/') }
 }
}
$planned+=@('characters/sir_imbert.png','characters/ywaleth_yoster.png','characters/lavena_vol.png','characters/koba.png','characters/linlos_dustman.png','characters/koba_v2.png','characters/linlos_dustman_v2.png',
 'weapon_visuals/melee_weapons.png','weapon_visuals/ranged_and_shield.png','weapon_visuals/spell_focus_weapons.png','weapon_visuals/shield_attack.png','weapon_visuals/minions.png','weapon_visuals/minions_2.png',
 'weapons_animations/projectiles/projectile_source_sheet.png','weapons_animations/weapon_skills/air_slash/air_slash.png')
for($i=0;$i -lt 8;$i++){$planned+=('weapon_visuals/melee_v3/lance_spin_'+$i+'.png')}
$code=(Get-ChildItem -LiteralPath (Join-Path $root 'core/src') -Recurse -Filter *.kt | ForEach-Object { Get-Content -LiteralPath $_.FullName -Raw }) -join "`n"
$moved=0;$bytes=0
foreach($relative in ($planned | Sort-Object -Unique)) {
 $source=[IO.Path]::GetFullPath((Join-Path $assetRoot $relative))
 $destination=[IO.Path]::GetFullPath((Join-Path $archiveRoot $relative))
 if(-not $source.StartsWith($assetRoot,[StringComparison]::OrdinalIgnoreCase) -or -not $destination.StartsWith($archiveRoot,[StringComparison]::OrdinalIgnoreCase)){throw 'Invalid retirement path'}
 if(-not (Test-Path -LiteralPath $source -PathType Leaf)){continue}
 if($code.Contains('"'+$relative+'"')){throw ('Still referenced: '+$relative)}
 if(Test-Path -LiteralPath $destination){throw ('Recovery asset already exists: '+$relative)}
 New-Item -ItemType Directory -Force -Path ([IO.Path]::GetDirectoryName($destination)) | Out-Null
 $bytes+=(Get-Item -LiteralPath $source).Length
 Move-Item -LiteralPath $source -Destination $destination
 Write-Output ('RETIRED '+$relative)
 $moved++
}
Write-Output ('REMOVED_FROM_PACKAGE '+$moved+' files; '+[Math]::Round($bytes/1MB,2)+' MB; recoverable in '+$archiveRoot)
