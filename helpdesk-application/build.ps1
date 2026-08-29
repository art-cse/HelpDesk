$ErrorActionPreference = "Stop"

$projectDirectory = Split-Path -Parent $MyInvocation.MyCommand.Path
$outputDirectory = Join-Path $projectDirectory "out"
$sourceDirectories = @(
    (Join-Path $projectDirectory "src\main\java"),
    (Join-Path $projectDirectory "src\test\java")
)

$javacCommand = Get-Command javac -ErrorAction SilentlyContinue
if ($javacCommand) {
    $javacPath = $javacCommand.Source
} else {
    $javacPath = "C:\Program Files\Android\openjdk\jdk-21.0.8\bin\javac.exe"
}

if (-not (Test-Path -LiteralPath $javacPath)) {
    Write-Error "javac was not found. Install JDK 17+ or add its bin directory to PATH."
}

$expectedOutputDirectory = [System.IO.Path]::GetFullPath((Join-Path $projectDirectory "out"))
$actualOutputDirectory = [System.IO.Path]::GetFullPath($outputDirectory)
if ($actualOutputDirectory -ne $expectedOutputDirectory) {
    Write-Error "Refusing to clean an unexpected output directory."
}
if (Test-Path -LiteralPath $actualOutputDirectory) {
    Remove-Item -LiteralPath $actualOutputDirectory -Recurse -Force
}
New-Item -ItemType Directory -Path $actualOutputDirectory -Force | Out-Null

$sourceFiles = Get-ChildItem -LiteralPath $sourceDirectories -Filter "*.java" -File -Recurse |
    Select-Object -ExpandProperty FullName

& $javacPath --release 17 -encoding UTF-8 -d $actualOutputDirectory $sourceFiles
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Write-Output "Build successful. Compiled classes are in $actualOutputDirectory"
