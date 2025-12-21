# Bootstrap Launcher for Ticari CRM
# This script ensures SQL Server is ready before launching the application

param(
    [switch]$AutoInstall = $false,
    [switch]$SkipUI = $false
)

$ErrorActionPreference = "Stop"

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "  Ticari CRM Bootstrap Launcher" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

function Test-SqlServerInstalled {
    Write-Host "Checking SQL Server installation..." -ForegroundColor Yellow
    $services = Get-Service -Name "MSSQL*" -ErrorAction SilentlyContinue
    
    if ($services) {
        Write-Host "  [OK] SQL Server is installed" -ForegroundColor Green
        $services | ForEach-Object {
            Write-Host "    - $($_.Name): $($_.Status)" -ForegroundColor Gray
        }
        return $true
    } else {
        Write-Host "  [FAIL] SQL Server not found" -ForegroundColor Red
        return $false
    }
}

function Start-SqlServerService {
    Write-Host "Starting SQL Server service..." -ForegroundColor Yellow
    
    $serviceNames = @("MSSQL`$SQLEXPRESS", "MSSQLSERVER", "SQLServerExpress")
    $started = $false
    
    foreach ($serviceName in $serviceNames) {
        $service = Get-Service -Name $serviceName -ErrorAction SilentlyContinue
        
        if ($service) {
            if ($service.Status -ne 'Running') {
                try {
                    Write-Host "  Starting service: $serviceName..." -ForegroundColor Yellow
                    Start-Service -Name $serviceName -ErrorAction Stop
                    Start-Sleep -Seconds 3
                    Write-Host "  [OK] Service started successfully" -ForegroundColor Green
                    $started = $true
                    break
                } catch {
                    Write-Host "  [WARN] Could not start $serviceName : $_" -ForegroundColor Yellow
                }
            } else {
                Write-Host "  [OK] Service $serviceName is already running" -ForegroundColor Green
                $started = $true
                break
            }
        }
    }
    
    return $started
}

function Test-DatabaseConnection {
    Write-Host "Testing database connection..." -ForegroundColor Yellow
    
    $connectionStrings = @(
        "Server=localhost;User Id=sa;Password=YourPassword123!;TrustServerCertificate=True;Connection Timeout=5",
        "Server=localhost\SQLEXPRESS;User Id=sa;Password=YourPassword123!;TrustServerCertificate=True;Connection Timeout=5"
    )
    
    foreach ($connStr in $connectionStrings) {
        try {
            $connection = New-Object System.Data.SqlClient.SqlConnection($connStr)
            $connection.Open()
            $connection.Close()
            Write-Host "  [OK] Connected successfully" -ForegroundColor Green
            Write-Host "    Connection: $($connStr.Split(';')[0])" -ForegroundColor Gray
            return $true
        } catch {
            # Try next connection string
        }
    }
    
    Write-Host "  [FAIL] Cannot connect to SQL Server" -ForegroundColor Red
    return $false
}

function Install-SqlServer {
    Write-Host ""
    Write-Host "Installing SQL Server Express..." -ForegroundColor Yellow
    Write-Host "This will take approximately 10-15 minutes." -ForegroundColor Yellow
    Write-Host ""
    
    $scriptPath = Join-Path $PSScriptRoot "install-sqlserver.ps1"
    
    if (-not (Test-Path $scriptPath)) {
        Write-Host "ERROR: Installation script not found at: $scriptPath" -ForegroundColor Red
        return $false
    }
    
    try {
        & $scriptPath
        return $?
    } catch {
        Write-Host "ERROR: Installation failed: $_" -ForegroundColor Red
        return $false
    }
}

# Main bootstrap logic
Write-Host "Phase 1: SQL Server Detection" -ForegroundColor Cyan
Write-Host "------------------------------" -ForegroundColor Gray

$sqlInstalled = Test-SqlServerInstalled

if (-not $sqlInstalled) {
    Write-Host ""
    Write-Host "SQL Server Express is not installed." -ForegroundColor Yellow
    
    if ($AutoInstall) {
        Write-Host "Auto-install mode enabled. Starting installation..." -ForegroundColor Yellow
        $installed = Install-SqlServer
        
        if (-not $installed) {
            Write-Host ""
            Write-Host "ERROR: SQL Server installation failed" -ForegroundColor Red
            Write-Host "Please install SQL Server Express manually:" -ForegroundColor Yellow
            Write-Host "  https://www.microsoft.com/sql-server/sql-server-downloads" -ForegroundColor Yellow
            exit 1
        }
        
        Write-Host ""
        Write-Host "SQL Server installed successfully!" -ForegroundColor Green
        $sqlInstalled = $true
    } else {
        Write-Host ""
        Write-Host "You can install SQL Server Express by running:" -ForegroundColor Yellow
        Write-Host "  .\bootstrap\install-sqlserver.ps1" -ForegroundColor White
        Write-Host ""
        Write-Host "Or run this script with -AutoInstall flag:" -ForegroundColor Yellow
        Write-Host "  .\bootstrap\bootstrap-launcher.ps1 -AutoInstall" -ForegroundColor White
        Write-Host ""
        
        $response = Read-Host "Would you like to install SQL Server Express now? (Y/N)"
        if ($response -eq 'Y' -or $response -eq 'y') {
            $installed = Install-SqlServer
            if (-not $installed) {
                exit 1
            }
            $sqlInstalled = $true
        } else {
            Write-Host "Cannot proceed without SQL Server. Exiting." -ForegroundColor Red
            exit 1
        }
    }
}

Write-Host ""
Write-Host "Phase 2: SQL Server Service" -ForegroundColor Cyan
Write-Host "------------------------------" -ForegroundColor Gray

$serviceStarted = Start-SqlServerService

if (-not $serviceStarted) {
    Write-Host ""
    Write-Host "ERROR: Could not start SQL Server service" -ForegroundColor Red
    Write-Host "Please start the service manually via Services (services.msc)" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "Phase 3: Database Connection" -ForegroundColor Cyan
Write-Host "------------------------------" -ForegroundColor Gray

# Wait a moment for service to be fully ready
Write-Host "Waiting for SQL Server to be ready..." -ForegroundColor Gray
Start-Sleep -Seconds 3

$canConnect = Test-DatabaseConnection

if (-not $canConnect) {
    Write-Host ""
    Write-Host "WARNING: Cannot connect to SQL Server" -ForegroundColor Yellow
    Write-Host "The application will attempt to connect on startup." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Please verify:" -ForegroundColor Yellow
    Write-Host "  1. SQL Server service is running" -ForegroundColor White
    Write-Host "  2. SA password is set to: YourPassword123!" -ForegroundColor White
    Write-Host "  3. SQL Server Authentication is enabled" -ForegroundColor White
    Write-Host ""
}

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "  Bootstrap Complete!" -ForegroundColor Green
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "The application is ready to start." -ForegroundColor Green
Write-Host ""

if (-not $SkipUI) {
    Write-Host "Press any key to launch the application..." -ForegroundColor Yellow
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    
    # Launch the application
    Write-Host "Launching Ticari CRM..." -ForegroundColor Cyan
    
    $jarPath = Join-Path (Split-Path $PSScriptRoot -Parent) "target\Crm-1.0-SNAPSHOT.jar"
    
    if (Test-Path $jarPath) {
        Start-Process "java" -ArgumentList "-jar", "`"$jarPath`""
    } else {
        Write-Host "Application JAR not found at: $jarPath" -ForegroundColor Red
        Write-Host "Please build the application first: mvn clean package" -ForegroundColor Yellow
    }
}

exit 0
