# SQL Server Express 2022 Silent Installation Script
# This script downloads and installs SQL Server Express with default settings

param(
    [string]$SaPassword = "YourPassword123!",
    [string]$InstanceName = "SQLEXPRESS"
)

$ErrorActionPreference = "Stop"

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "  SQL Server Express Installation" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# Check if running as Administrator
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "ERROR: This script must be run as Administrator!" -ForegroundColor Red
    Write-Host "Right-click PowerShell and select 'Run as Administrator'" -ForegroundColor Yellow
    exit 1
}

# Check if SQL Server is already installed
Write-Host "Checking for existing SQL Server installation..." -ForegroundColor Yellow
$sqlServices = Get-Service -Name "MSSQL*" -ErrorAction SilentlyContinue
if ($sqlServices) {
    Write-Host "SQL Server is already installed!" -ForegroundColor Green
    Write-Host "Services found:"
    $sqlServices | Format-Table -Property Name, Status, DisplayName -AutoSize
    
    # Try to start the service if it's stopped
    foreach ($service in $sqlServices) {
        if ($service.Status -ne 'Running') {
            Write-Host "Starting service: $($service.Name)..." -ForegroundColor Yellow
            try {
                Start-Service -Name $service.Name -ErrorAction Stop
                Write-Host "Service started successfully!" -ForegroundColor Green
            } catch {
                Write-Host "Warning: Could not start service $($service.Name): $_" -ForegroundColor Yellow
            }
        }
    }
    
    exit 0
}

# Download SQL Server Express
$downloadUrl = "https://go.microsoft.com/fwlink/p/?linkid=2216019&clcid=0x409&culture=en-us&country=us"
$installerPath = "$env:TEMP\SQLServerExpress.exe"

Write-Host "Downloading SQL Server Express 2022..." -ForegroundColor Yellow
Write-Host "URL: $downloadUrl" -ForegroundColor Gray
Write-Host "Target: $installerPath" -ForegroundColor Gray

try {
    # Use Invoke-WebRequest with progress
    $ProgressPreference = 'SilentlyContinue'
    Invoke-WebRequest -Uri $downloadUrl -OutFile $installerPath -UseBasicParsing
    Write-Host "Download completed!" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Failed to download SQL Server Express" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}

# Verify download
if (-not (Test-Path $installerPath)) {
    Write-Host "ERROR: Installer file not found at $installerPath" -ForegroundColor Red
    exit 1
}

$fileSize = (Get-Item $installerPath).Length / 1MB
Write-Host "Installer size: $([math]::Round($fileSize, 2)) MB" -ForegroundColor Gray

# Prepare installation parameters
$configFile = "$env:TEMP\SQLServerConfig.ini"
$configContent = @"
[OPTIONS]
ACTION="Install"
QUIET="True"
FEATURES=SQLENGINE
INSTANCENAME="$InstanceName"
INSTANCEID="$InstanceName"
SQLSVCACCOUNT="NT AUTHORITY\SYSTEM"
SQLSYSADMINACCOUNTS="BUILTIN\Administrators"
SECURITYMODE="SQL"
SAPWD="$SaPassword"
TCPENABLED="1"
NPENABLED="0"
BROWSERSVCSTARTUPTYPE="Automatic"
SQLSVCSTARTUPTYPE="Automatic"
IACCEPTSQLSERVERLICENSETERMS="True"
SUPPRESSPRIVACYSTATEMENTNOTICE="True"
"@

Write-Host "Creating configuration file..." -ForegroundColor Yellow
$configContent | Out-File -FilePath $configFile -Encoding ASCII

Write-Host ""
Write-Host "Starting SQL Server installation..." -ForegroundColor Yellow
Write-Host "This may take 10-15 minutes. Please wait..." -ForegroundColor Yellow
Write-Host ""

try {
    # Run installer
    $process = Start-Process -FilePath $installerPath `
        -ArgumentList "/ConfigurationFile=`"$configFile`"" `
        -Wait -PassThru -NoNewWindow
    
    $exitCode = $process.ExitCode
    
    if ($exitCode -eq 0) {
        Write-Host ""
        Write-Host "SQL Server Express installed successfully!" -ForegroundColor Green
        Write-Host ""
        
        # Wait for service to start
        Write-Host "Waiting for SQL Server service to start..." -ForegroundColor Yellow
        Start-Sleep -Seconds 5
        
        $serviceName = "MSSQL`$$InstanceName"
        $service = Get-Service -Name $serviceName -ErrorAction SilentlyContinue
        
        if ($service) {
            if ($service.Status -ne 'Running') {
                Write-Host "Starting SQL Server service..." -ForegroundColor Yellow
                Start-Service -Name $serviceName
                Start-Sleep -Seconds 3
            }
            
            Write-Host "SQL Server service status: $($service.Status)" -ForegroundColor Green
        }
        
        # Enable TCP/IP protocol
        Write-Host "Configuring SQL Server network protocols..." -ForegroundColor Yellow
        try {
            # This requires SQL Server PowerShell module, so we'll skip if not available
            Write-Host "Note: You may need to manually enable TCP/IP in SQL Server Configuration Manager" -ForegroundColor Yellow
        } catch {
            Write-Host "Note: Could not configure protocols automatically" -ForegroundColor Yellow
        }
        
        Write-Host ""
        Write-Host "Installation Summary:" -ForegroundColor Cyan
        Write-Host "  Instance Name: $InstanceName" -ForegroundColor White
        Write-Host "  SA Password: $SaPassword" -ForegroundColor White
        Write-Host "  Connection String: Server=localhost\$InstanceName;User Id=sa;Password=$SaPassword;" -ForegroundColor White
        Write-Host ""
        Write-Host "Installation completed successfully!" -ForegroundColor Green
        
    } else {
        Write-Host ""
        Write-Host "ERROR: SQL Server installation failed with exit code: $exitCode" -ForegroundColor Red
        Write-Host "Please check the SQL Server installation logs in:" -ForegroundColor Yellow
        Write-Host "  C:\Program Files\Microsoft SQL Server\*\Setup Bootstrap\Log\" -ForegroundColor Yellow
        exit 1
    }
    
} catch {
    Write-Host "ERROR: Installation failed" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
} finally {
    # Cleanup
    Write-Host "Cleaning up temporary files..." -ForegroundColor Gray
    if (Test-Path $installerPath) {
        Remove-Item $installerPath -Force -ErrorAction SilentlyContinue
    }
    if (Test-Path $configFile) {
        Remove-Item $configFile -Force -ErrorAction SilentlyContinue
    }
}

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "  Installation Complete!" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan

exit 0
