# Windows Build Script for Ticari CRM
# Requires: JDK 21+ with jpackage tool
# Description: Builds a Windows .exe installer for the application

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Ticari CRM - Windows Build Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if Java is installed
Write-Host "Checking Java installation..." -ForegroundColor Yellow
$javaVersion = & java -version 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Java is not installed or not in PATH!" -ForegroundColor Red
    Write-Host "Please install JDK 21 or later" -ForegroundColor Red
    exit 1
}

Write-Host "Java detected: $($javaVersion[0])" -ForegroundColor Green
Write-Host ""

# Check if Maven is installed
Write-Host "Checking Maven installation..." -ForegroundColor Yellow
$mvnVersion = & mvn -version 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Maven is not installed or not in PATH!" -ForegroundColor Red
    Write-Host "Please install Apache Maven" -ForegroundColor Red
    exit 1
}

Write-Host "Maven detected: $($mvnVersion[0])" -ForegroundColor Green
Write-Host ""

# Clean previous builds
Write-Host "Cleaning previous builds..." -ForegroundColor Yellow
& mvn clean
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Maven clean failed!" -ForegroundColor Red
    exit 1
}
Write-Host "Clean completed successfully" -ForegroundColor Green
Write-Host ""

# Compile and package
Write-Host "Compiling and packaging application..." -ForegroundColor Yellow
& mvn package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Maven package failed!" -ForegroundColor Red
    exit 1
}
Write-Host "Package created successfully" -ForegroundColor Green
Write-Host ""

# Create runtime image
Write-Host "Creating custom JRE runtime image..." -ForegroundColor Yellow
$modulePath = "target\lib"
$modules = "java.base,java.sql,java.desktop,java.naming,javafx.controls,javafx.fxml,javafx.graphics"

if (Test-Path "target\runtime") {
    Remove-Item -Recurse -Force "target\runtime"
}

# Note: jlink may need adjustment based on your environment
# & jlink --module-path "$modulePath" --add-modules $modules --output "target\runtime" --strip-debug --no-header-files --no-man-pages --compress=2

Write-Host "Runtime image creation skipped (requires proper jlink setup)" -ForegroundColor Yellow
Write-Host ""

# Build Windows installer
Write-Host "Building Windows installer..." -ForegroundColor Yellow
Write-Host "Note: This requires WiX Toolset for .exe creation" -ForegroundColor Cyan

# Uncomment when jlink runtime is ready:
# & mvn package -Pwindows-installer

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Build Process Information" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "JAR Location: target\Crm-1.0-SNAPSHOT.jar" -ForegroundColor Green
Write-Host "Dependencies: target\lib\" -ForegroundColor Green
Write-Host ""
Write-Host "To create Windows installer:" -ForegroundColor Yellow
Write-Host "1. Install WiX Toolset (https://wixtoolset.org/)" -ForegroundColor Yellow
Write-Host "2. Create runtime with jlink" -ForegroundColor Yellow
Write-Host "3. Run: mvn package -Pwindows-installer" -ForegroundColor Yellow
Write-Host ""
Write-Host "To run the application:" -ForegroundColor Yellow
Write-Host "  java -jar target\Crm-1.0-SNAPSHOT.jar" -ForegroundColor Cyan
Write-Host ""
Write-Host "Build completed successfully!" -ForegroundColor Green
