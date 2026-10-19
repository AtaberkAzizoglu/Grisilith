$ErrorActionPreference = 'Stop'
$ttsDir = Join-Path $PSScriptRoot 'sources/character-tts'
New-Item -ItemType Directory -Force -Path $ttsDir | Out-Null
$pythonRuntime = 'C:/Users/aaata/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe'
$localPython = Join-Path $ttsDir 'runtime/Scripts/python.exe'
if (!(Test-Path -LiteralPath $localPython)) { & $pythonRuntime -m venv (Join-Path $ttsDir 'runtime'); if ($LASTEXITCODE) { throw 'Cannot create local TTS environment' } }
& $localPython -m pip install --disable-pip-version-check kokoro-onnx soundfile scipy
if ($LASTEXITCODE) { throw 'Cannot install local TTS dependencies' }
$expectedSizes = @{'kokoro-v1.0.onnx'=325505369; 'voices-v1.0.bin'=28214398}
foreach ($name in @('kokoro-v1.0.onnx','voices-v1.0.bin')) {
    $target = Join-Path $ttsDir $name
    if (!(Test-Path -LiteralPath $target) -or (Get-Item -LiteralPath $target).Length -ne $expectedSizes[$name]) {
        & curl.exe --fail -L --retry 2 --connect-timeout 30 --max-time 300 --silent --show-error -o $target ('https://github.com/thewh1teagle/kokoro-onnx/releases/download/model-files-v1.1/' + $name)
        if ($LASTEXITCODE) { throw ('Cannot download ' + $name) }
        if ((Get-Item -LiteralPath $target).Length -ne $expectedSizes[$name]) { throw ('Incomplete download: ' + $name) }
    }
}
foreach ($item in @(
    @('MODEL-LICENSE.txt','https://www.apache.org/licenses/LICENSE-2.0.txt'),
    @('VOICES.md','https://huggingface.co/hexgrad/Kokoro-82M/raw/main/VOICES.md'),
    @('RUNTIME-LICENSE.txt','https://raw.githubusercontent.com/thewh1teagle/kokoro-onnx/main/LICENSE')
)) {
    Invoke-WebRequest -UseBasicParsing -Uri $item[1] -OutFile (Join-Path $ttsDir $item[0])
}
& $localPython -c 'import kokoro_onnx; print("LOCAL_CHARACTER_TTS_READY")'
