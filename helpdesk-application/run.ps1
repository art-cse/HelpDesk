$ErrorActionPreference = "Stop"

$projectDirectory = Split-Path -Parent $MyInvocation.MyCommand.Path
& (Join-Path $projectDirectory "build.ps1")
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

$javawCommand = Get-Command javaw -ErrorAction SilentlyContinue
if ($javawCommand) {
    $javawPath = $javawCommand.Source
} else {
    $javawPath = "C:\Program Files\Android\openjdk\jdk-21.0.8\bin\javaw.exe"
}

if (-not (Test-Path -LiteralPath $javawPath)) {
    Write-Error "javaw was not found. Install JDK 17+ or add its bin directory to PATH."
}

$classPath = '"' + (Join-Path $projectDirectory "out") + '"'
Start-Process -FilePath $javawPath -ArgumentList @("-cp", $classPath, "helpdesk.Main")
Write-Output "FiberNet HelpDesk started."
