@echo off
REM Ticari CRM Windows Launcher
REM This batch file starts the application with automatic SQL Server bootstrap

setlocal

echo ======================================
echo   Ticari CRM - Windows Launcher
echo ======================================
echo.

REM Check if running as Administrator
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo WARNING: Not running as Administrator
    echo Some features may require admin privileges
    echo.
)

REM Check Java installation
java -version >nul 2>&1
if %errorLevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17 or higher
    echo Download from: https://adoptium.net/
    pause
    exit /b 1
)

echo Java installation found
java -version 2>&1 | findstr /C:"version"
echo.

REM Find JAR file
set JAR_FILE=target\Crm-1.0-SNAPSHOT.jar

if not exist "%JAR_FILE%" (
    echo ERROR: Application JAR not found at: %JAR_FILE%
    echo.
    echo Please build the application first:
    echo   mvn clean package
    echo.
    pause
    exit /b 1
)

echo Starting application...
echo JAR File: %JAR_FILE%
echo.

REM Launch the application
REM The bootstrap will run automatically from JavaFXApplication.init()
java -jar "%JAR_FILE%"

echo.
echo Application terminated
pause
