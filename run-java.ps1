$ErrorActionPreference = "Stop"

$libPath = Join-Path $PSScriptRoot "lib\*"
$jarPath = Join-Path $PSScriptRoot "lib\sqlite-jdbc-3.45.3.0.jar"
$sourcePath = Join-Path $PSScriptRoot "src\main\java\com\bankatm\BankAtmApp.java"
$outDir = Join-Path $PSScriptRoot "out"

if (-not (Test-Path $jarPath)) {
    Write-Host "SQLite JDBC driver is missing. Running setup-java.ps1 first..."
    & (Join-Path $PSScriptRoot "setup-java.ps1")
}

New-Item -ItemType Directory -Force -Path $outDir | Out-Null

javac -cp $libPath -d $outDir $sourcePath
java -cp "$outDir;$libPath" com.bankatm.BankAtmApp
