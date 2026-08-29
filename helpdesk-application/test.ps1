$ErrorActionPreference = "Stop"

$projectDirectory = Split-Path -Parent $MyInvocation.MyCommand.Path
& (Join-Path $projectDirectory "build.ps1")
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

$javaCommand = Get-Command java -ErrorAction SilentlyContinue
if ($javaCommand) {
    $javaPath = $javaCommand.Source
} else {
    $javaPath = "C:\Program Files\Android\openjdk\jdk-21.0.8\bin\java.exe"
}

if (-not (Test-Path -LiteralPath $javaPath)) {
    Write-Error "java was not found. Install JDK 17+ or add its bin directory to PATH."
}

$classPath = Join-Path $projectDirectory "out"
& $javaPath -cp $classPath helpdesk.HelpDeskTest
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}
& $javaPath -cp $classPath helpdesk.GuiWorkflowTest
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}
& $javaPath -cp $classPath helpdesk.RoleWorkflowTest
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}
