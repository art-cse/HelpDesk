$ErrorActionPreference = "Stop"

$projectDirectory = Split-Path -Parent $MyInvocation.MyCommand.Path
$distDirectory = Join-Path $projectDirectory "dist"
$workDirectory = Join-Path $projectDirectory "package-work"
$classDirectory = Join-Path $workDirectory "classes"
$inputDirectory = Join-Path $workDirectory "input"

$jdkBinDirectory = "C:\Program Files\Android\openjdk\jdk-21.0.8\bin"
$javacCommand = Get-Command javac -ErrorAction SilentlyContinue
if ($javacCommand) {
    $jdkBinDirectory = Split-Path -Parent $javacCommand.Source
}
$javacPath = Join-Path $jdkBinDirectory "javac.exe"
$jarPath = Join-Path $jdkBinDirectory "jar.exe"
$jpackagePath = Join-Path $jdkBinDirectory "jpackage.exe"

if (-not (Test-Path -LiteralPath $javacPath)) {
    Write-Error "javac was not found. Install JDK 17 or newer."
}
if (-not (Test-Path -LiteralPath $jarPath)) {
    Write-Error "jar was not found in the selected JDK."
}
if (-not (Test-Path -LiteralPath $jpackagePath)) {
    Write-Error "jpackage was not found. Use a full JDK 17 or newer."
}

& (Join-Path $projectDirectory "test.ps1")
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

$expectedDistDirectory = [System.IO.Path]::GetFullPath((Join-Path $projectDirectory "dist"))
$expectedWorkDirectory = [System.IO.Path]::GetFullPath((Join-Path $projectDirectory "package-work"))
$actualDistDirectory = [System.IO.Path]::GetFullPath($distDirectory)
$actualWorkDirectory = [System.IO.Path]::GetFullPath($workDirectory)
if (($actualDistDirectory -ne $expectedDistDirectory) -or ($actualWorkDirectory -ne $expectedWorkDirectory)) {
    Write-Error "Refusing to clean unexpected packaging directories."
}
if (Test-Path -LiteralPath $actualDistDirectory) {
    Remove-Item -LiteralPath $actualDistDirectory -Recurse -Force
}
if (Test-Path -LiteralPath $actualWorkDirectory) {
    Remove-Item -LiteralPath $actualWorkDirectory -Recurse -Force
}
New-Item -ItemType Directory -Path $actualDistDirectory -Force | Out-Null
New-Item -ItemType Directory -Path $classDirectory -Force | Out-Null
New-Item -ItemType Directory -Path $inputDirectory -Force | Out-Null

$mainSources = Get-ChildItem -LiteralPath (Join-Path $projectDirectory "src\main\java") `
    -Filter "*.java" -File -Recurse | Select-Object -ExpandProperty FullName
& $javacPath --release 17 -encoding UTF-8 -d $classDirectory $mainSources
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

$inputJar = Join-Path $inputDirectory "FiberNetHelpDesk.jar"
& $jarPath --create --file $inputJar --main-class helpdesk.Main -C $classDirectory .
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}
Copy-Item -LiteralPath $inputJar -Destination `
    (Join-Path $actualDistDirectory "FiberNetHelpDesk.jar")

& $jpackagePath --type app-image --name "FiberNet HelpDesk" `
    --input $inputDirectory --main-jar "FiberNetHelpDesk.jar" `
    --main-class helpdesk.Main --dest $actualDistDirectory `
    --app-version "1.0" --vendor "University OOP Project" `
    --description "FiberNet IT and internet service HelpDesk"
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

$windowsAppDirectory = Join-Path $actualDistDirectory "FiberNet HelpDesk"
$windowsArchive = Join-Path $actualDistDirectory "FiberNet-HelpDesk-Windows.zip"
Compress-Archive -LiteralPath $windowsAppDirectory -DestinationPath $windowsArchive `
    -CompressionLevel Optimal -Force

Copy-Item -LiteralPath (Join-Path $projectDirectory "README.md") `
    -Destination (Join-Path $actualDistDirectory "PROJECT-README.md")
Write-Output "Packaging complete."
Write-Output "Runnable JAR: $actualDistDirectory\FiberNetHelpDesk.jar"
Write-Output "Windows archive: $windowsArchive"
Write-Output "Windows application: $windowsAppDirectory\FiberNet HelpDesk.exe"
