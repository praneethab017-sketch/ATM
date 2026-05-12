$ErrorActionPreference = "Stop"

$libDir = Join-Path $PSScriptRoot "lib"
$dependencies = @(
    @{
        Name = "SQLite JDBC driver"
        Path = Join-Path $libDir "sqlite-jdbc-3.45.3.0.jar"
        Url = "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.3.0/sqlite-jdbc-3.45.3.0.jar"
    },
    @{
        Name = "SLF4J API"
        Path = Join-Path $libDir "slf4j-api-2.0.13.jar"
        Url = "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.13/slf4j-api-2.0.13.jar"
    },
    @{
        Name = "SLF4J simple logger"
        Path = Join-Path $libDir "slf4j-simple-2.0.13.jar"
        Url = "https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.13/slf4j-simple-2.0.13.jar"
    }
)

New-Item -ItemType Directory -Force -Path $libDir | Out-Null

foreach ($dependency in $dependencies) {
    if (Test-Path $dependency.Path) {
        Write-Host "$($dependency.Name) already exists: $($dependency.Path)"
        continue
    }

    Write-Host "Downloading $($dependency.Name)..."
    Invoke-WebRequest -Uri $dependency.Url -OutFile $dependency.Path
    Write-Host "Downloaded: $($dependency.Path)"
}
