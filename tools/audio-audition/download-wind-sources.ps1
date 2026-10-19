$ErrorActionPreference = 'Stop'
foreach ($source in @(
    @{Name='air-move'; Page='https://opengameart.org/content/air-woosh-move'; Match='air_move.wav'},
    @{Name='short-wind'; Page='https://opengameart.org/content/short-wind-sound'; Match='short'},
    @{Name='wind-loop'; Page='https://opengameart.org/content/wind-whoosh-loop'; Match='loop'}
)) {
    $target = Join-Path $PSScriptRoot ('sources/' + $source.Name)
    $audioTarget = Join-Path $target 'Audio'
    New-Item -ItemType Directory -Force -Path $audioTarget | Out-Null
    $pagePath = Join-Path $target 'source-page.html'
    if (!(Test-Path -LiteralPath $pagePath)) { Invoke-WebRequest $source.Page -UseBasicParsing -OutFile $pagePath }
    $pageHtml = Get-Content -LiteralPath $pagePath -Raw
    $urls = @([regex]::Matches($pageHtml,'href="(https://opengameart.org/sites/default/files/[^"]+\.(?:wav|ogg))"') | ForEach-Object { $_.Groups[1].Value } | Select-Object -Unique)
    $url = $urls | Where-Object { $_ -match $source.Match } | Select-Object -First 1
    if (!$url) { throw ('Missing source link: ' + $source.Name) }
    $extension = [IO.Path]::GetExtension(([Uri]$url).AbsolutePath)
    $output = Join-Path $audioTarget ('wind' + $extension)
    if (!(Test-Path -LiteralPath $output)) { Invoke-WebRequest $url -OutFile $output }
    Write-Output ($source.Name + ' -> ' + $url)
}
