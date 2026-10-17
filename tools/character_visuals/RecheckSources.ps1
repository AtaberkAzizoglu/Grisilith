$ErrorActionPreference='Stop'
$taskDefinitions=@('sir_imbert','ywaleth_yoster','lavena_vol','koba','linlos_dustman') | ForEach-Object {
    @{name=$_;source=('tools/combat-targeting/previews/'+$_+'.png')}
}
$taskHashes=@{}
for($taskPass=1;$taskPass -le 3;$taskPass++) {
    foreach($taskDefinition in $taskDefinitions) {
        $taskOutput="assets/characters/aligned_v4/"+$taskDefinition.name+".png"
        ./tools/character_visuals/ImportAlignedAtlas.ps1 -InputPath $taskDefinition.source -OutputPath $taskOutput -Columns 0,280,561,841,1122,1402 -Rows 0,280,561,841,1122 -CellSize 256 -ConnectedSprites
        $taskHash=(Get-FileHash -LiteralPath $taskOutput -Algorithm SHA256).Hash
        if($taskPass -gt 1 -and $taskHashes[$taskDefinition.name] -ne $taskHash) {
            throw ("Non-deterministic source extraction: "+$taskDefinition.name)
        }
        $taskHashes[$taskDefinition.name]=$taskHash
    }
    Write-Output ("SOURCE_RECHECK_PASS "+$taskPass+": five approved v4 sources, stable cuts and padding.")
}
