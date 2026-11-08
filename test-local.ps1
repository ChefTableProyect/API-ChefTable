$env:DOCKER_HOST = 'npipe:////./pipe/dockerDesktopLinuxEngine'

& (Join-Path $PSScriptRoot 'mvnw.cmd') --batch-mode --no-transfer-progress test
exit $LASTEXITCODE
