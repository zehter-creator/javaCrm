# Complete Build and Installer Creation Pipeline for TicariCRM
# This script automates the entire build-to-installer process

param(
    [string]$Version = "1.0.0",
    [switch]$SkipTests = $true,
    [switch]$SkipJREDownload = $false,
    [switch]$SkipSQLServerDownload = $false,
    [switch]$Clean = $true
)

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  TicariCRM Installer Build Pipeline" -ForegroundColor Cyan
Write-Host "  Version: $Version" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$JREVersion = "21"
$JREUrl = "https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jre/hotspot/normal/eclipse?project=jdk"
$SQLServerUrl = "https://go.microsoft.com/fwlink/p/?linkid=2216019&clcid=0x409&culture=en-us&country=us"
$InnoSetupPath = "C:\Program Files (x86)\Inno Setup 6\ISCC.exe"
$Launch4jPath = "C:\Program Files (x86)\Launch4j\launch4jc.exe"

# Directory paths
$ProjectRoot = $PSScriptRoot
$TargetDir = Join-Path $ProjectRoot "target"
$JREDir = Join-Path $ProjectRoot "jre"
$InstallerDir = Join-Path $ProjectRoot "installer"
$DistDir = Join-Path $ProjectRoot "dist"
$TempDir = Join-Path $ProjectRoot "temp-build"

# Step 0: Prerequisites Check
Write-Host "Step 0: Checking Prerequisites" -ForegroundColor Yellow
Write-Host "-------------------------------" -ForegroundColor Gray
Write-Host ""

# Check Maven
$mavenVersion = & mvn --version 2>&1 | Select-String "Apache Maven"
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Maven not found!" -ForegroundColor Red
    Write-Host "Please install Maven: https://maven.apache.org/download.cgi" -ForegroundColor Yellow
    exit 1
}
Write-Host "  [OK] Maven: $mavenVersion" -ForegroundColor Green

# Check Java
$javaVersion = & java -version 2>&1 | Select-String "version"
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Java not found!" -ForegroundColor Red
    Write-Host "Please install Java 17 or 21: https://adoptium.net/" -ForegroundColor Yellow
    exit 1
}
Write-Host "  [OK] Java: $javaVersion" -ForegroundColor Green

# Check Inno Setup (optional but recommended)
if (Test-Path $InnoSetupPath) {
    Write-Host "  [OK] Inno Setup found" -ForegroundColor Green
} else {
    Write-Host "  [WARN] Inno Setup not found at: $InnoSetupPath" -ForegroundColor Yellow
    Write-Host "         Download from: https://jrsoftware.org/isdl.php" -ForegroundColor Yellow
}

Write-Host ""

# Step 1: Clean Previous Build
if ($Clean) {
    Write-Host "Step 1: Cleaning Previous Build" -ForegroundColor Yellow
    Write-Host "-------------------------------" -ForegroundColor Gray
    Write-Host ""
    
    if (Test-Path $TargetDir) {
        Write-Host "  Removing target directory..." -ForegroundColor Gray
        Remove-Item $TargetDir -Recurse -Force -ErrorAction SilentlyContinue
    }
    
    if (Test-Path $DistDir) {
        Write-Host "  Removing dist directory..." -ForegroundColor Gray
        Remove-Item $DistDir -Recurse -Force -ErrorAction SilentlyContinue
    }
    
    if (Test-Path $TempDir) {
        Write-Host "  Removing temp directory..." -ForegroundColor Gray
        Remove-Item $TempDir -Recurse -Force -ErrorAction SilentlyContinue
    }
    
    Write-Host "  [OK] Clean completed" -ForegroundColor Green
    Write-Host ""
}

# Step 2: Build Application
Write-Host "Step 2: Building Application with Maven" -ForegroundColor Yellow
Write-Host "-------------------------------" -ForegroundColor Gray
Write-Host ""

$mavenArgs = @("clean", "package")
if ($SkipTests) {
    $mavenArgs += "-DskipTests"
}

Write-Host "  Running: mvn $($mavenArgs -join ' ')" -ForegroundColor Gray
& mvn $mavenArgs

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "ERROR: Maven build failed!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "  [OK] Application built successfully" -ForegroundColor Green
Write-Host "  JAR: $TargetDir\Crm-1.0-SNAPSHOT.jar" -ForegroundColor Gray
Write-Host ""

# Step 3: Copy Dependencies
Write-Host "Step 3: Copying Dependencies" -ForegroundColor Yellow
Write-Host "-------------------------------" -ForegroundColor Gray
Write-Host ""

$libDir = Join-Path $TargetDir "lib"
if (-not (Test-Path $libDir)) {
    Write-Host "  Running: mvn dependency:copy-dependencies" -ForegroundColor Gray
    & mvn dependency:copy-dependencies -DoutputDirectory=target/lib
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "  [WARN] Failed to copy dependencies" -ForegroundColor Yellow
    } else {
        $depCount = (Get-ChildItem $libDir -File).Count
        Write-Host "  [OK] Copied $depCount dependencies" -ForegroundColor Green
    }
} else {
    $depCount = (Get-ChildItem $libDir -File).Count
    Write-Host "  [OK] Dependencies already exist: $depCount files" -ForegroundColor Green
}

Write-Host ""

# Step 4: Download/Prepare JRE
if (-not $SkipJREDownload) {
    Write-Host "Step 4: Preparing Java Runtime Environment" -ForegroundColor Yellow
    Write-Host "-------------------------------" -ForegroundColor Gray
    Write-Host ""
    
    if (Test-Path $JREDir) {
        Write-Host "  [OK] JRE already exists at: $JREDir" -ForegroundColor Green
        $jreSize = [math]::Round(((Get-ChildItem $JREDir -Recurse | Measure-Object -Property Length -Sum).Sum / 1MB), 2)
        Write-Host "  Size: $jreSize MB" -ForegroundColor Gray
    } else {
        Write-Host "  Downloading JRE $JREVersion from Adoptium..." -ForegroundColor Yellow
        Write-Host "  URL: $JREUrl" -ForegroundColor Gray
        Write-Host ""
        
        $jreZip = Join-Path $TempDir "jre.zip"
        New-Item -ItemType Directory -Force -Path $TempDir | Out-Null
        
        try {
            $ProgressPreference = 'SilentlyContinue'
            Invoke-WebRequest -Uri $JREUrl -OutFile $jreZip -UseBasicParsing
            Write-Host "  [OK] Downloaded JRE" -ForegroundColor Green
            
            Write-Host "  Extracting JRE..." -ForegroundColor Yellow
            Expand-Archive -Path $jreZip -DestinationPath $TempDir -Force
            
            # Find the JRE directory (it's nested)
            $extractedJre = Get-ChildItem $TempDir -Directory | Where-Object { $_.Name -like "jdk*" -or $_.Name -like "jre*" } | Select-Object -First 1
            
            if ($extractedJre) {
                # Move contents to jre directory
                New-Item -ItemType Directory -Force -Path $JREDir | Out-Null
                Get-ChildItem $extractedJre.FullName -Recurse | Move-Item -Destination $JREDir -Force
                Write-Host "  [OK] JRE extracted to: $JREDir" -ForegroundColor Green
            } else {
                Write-Host "  [WARN] Could not find extracted JRE directory" -ForegroundColor Yellow
            }
            
            Remove-Item $jreZip -Force
        } catch {
            Write-Host "  [ERROR] Failed to download JRE: $_" -ForegroundColor Red
            Write-Host "  Please download manually from: https://adoptium.net/temurin/releases/" -ForegroundColor Yellow
        }
    }
    
    Write-Host ""
}

# Step 5: Create Native Launcher with Launch4j (Optional)
Write-Host "Step 5: Creating Native Windows Executable" -ForegroundColor Yellow
Write-Host "-------------------------------" -ForegroundColor Gray
Write-Host ""

$exeOutput = Join-Path $TargetDir "TicariCRM.exe"

if (Test-Path $Launch4jPath) {
    $launch4jConfig = Join-Path $ProjectRoot "launch4j-config.xml"
    
    if (Test-Path $launch4jConfig) {
        Write-Host "  Running Launch4j..." -ForegroundColor Gray
        & $Launch4jPath $launch4jConfig
        
        if (Test-Path $exeOutput) {
            Write-Host "  [OK] Native executable created: TicariCRM.exe" -ForegroundColor Green
        } else {
            Write-Host "  [WARN] Launch4j did not create executable" -ForegroundColor Yellow
        }
    } else {
        Write-Host "  [WARN] Launch4j config not found: $launch4jConfig" -ForegroundColor Yellow
    }
} else {
    Write-Host "  [SKIP] Launch4j not found - will use JAR directly" -ForegroundColor Yellow
    Write-Host "  Install Launch4j from: http://launch4j.sourceforge.net/" -ForegroundColor Gray
}

Write-Host ""

# Step 6: Download SQL Server Express Installer (Optional)
if (-not $SkipSQLServerDownload) {
    Write-Host "Step 6: Preparing SQL Server Express Installer" -ForegroundColor Yellow
    Write-Host "-------------------------------" -ForegroundColor Gray
    Write-Host ""
    
    $sqlServerInstaller = Join-Path $InstallerDir "SQLServerExpress.exe"
    
    if (Test-Path $sqlServerInstaller) {
        $sqlSize = [math]::Round(((Get-Item $sqlServerInstaller).Length / 1MB), 2)
        Write-Host "  [OK] SQL Server installer already exists" -ForegroundColor Green
        Write-Host "  Size: $sqlSize MB" -ForegroundColor Gray
    } else {
        Write-Host "  SQL Server Express installer not found" -ForegroundColor Yellow
        Write-Host "  You can download it manually from:" -ForegroundColor Yellow
        Write-Host "  $SQLServerUrl" -ForegroundColor Gray
        Write-Host "  Save as: $sqlServerInstaller" -ForegroundColor Gray
        Write-Host ""
        Write-Host "  [SKIP] SQL Server installer will not be bundled" -ForegroundColor Yellow
        Write-Host "  Installer will download it on-demand or use existing installation" -ForegroundColor Gray
    }
    
    Write-Host ""
}

# Step 7: Create Installer with Inno Setup
Write-Host "Step 7: Creating Windows Installer" -ForegroundColor Yellow
Write-Host "-------------------------------" -ForegroundColor Gray
Write-Host ""

$issScript = Join-Path $InstallerDir "TicariCRM.iss"

if (-not (Test-Path $issScript)) {
    Write-Host "  [ERROR] Inno Setup script not found: $issScript" -ForegroundColor Red
    Write-Host "  Please ensure TicariCRM.iss exists in the installer directory" -ForegroundColor Yellow
    exit 1
}

if (Test-Path $InnoSetupPath) {
    Write-Host "  Running Inno Setup Compiler..." -ForegroundColor Gray
    Write-Host "  Script: $issScript" -ForegroundColor Gray
    Write-Host ""
    
    # Create dist directory if it doesn't exist
    New-Item -ItemType Directory -Force -Path $DistDir | Out-Null
    
    # Run Inno Setup
    & $InnoSetupPath $issScript /DMyAppVersion=$Version
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "  [OK] Installer created successfully!" -ForegroundColor Green
        
        $setupExe = Join-Path $DistDir "TicariCRM_Setup_$Version.exe"
        if (Test-Path $setupExe) {
            $setupSize = [math]::Round(((Get-Item $setupExe).Length / 1MB), 2)
            Write-Host "  Output: $setupExe" -ForegroundColor Green
            Write-Host "  Size: $setupSize MB" -ForegroundColor Gray
        }
    } else {
        Write-Host ""
        Write-Host "  [ERROR] Inno Setup compilation failed!" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "  [ERROR] Inno Setup not found at: $InnoSetupPath" -ForegroundColor Red
    Write-Host "  Please install Inno Setup from: https://jrsoftware.org/isdl.php" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Step 8: Cleanup
Write-Host "Step 8: Cleaning Up Temporary Files" -ForegroundColor Yellow
Write-Host "-------------------------------" -ForegroundColor Gray
Write-Host ""

if (Test-Path $TempDir) {
    Remove-Item $TempDir -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "  [OK] Temporary files removed" -ForegroundColor Green
}

Write-Host ""

# Summary
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Build Pipeline Completed!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Build Summary:" -ForegroundColor Cyan
Write-Host "  Application JAR: $TargetDir\Crm-1.0-SNAPSHOT.jar" -ForegroundColor White
Write-Host "  Dependencies: $TargetDir\lib\" -ForegroundColor White

if (Test-Path $JREDir) {
    Write-Host "  Java Runtime: $JREDir" -ForegroundColor White
}

if (Test-Path $exeOutput) {
    Write-Host "  Native Executable: $exeOutput" -ForegroundColor White
}

$finalSetup = Join-Path $DistDir "TicariCRM_Setup_$Version.exe"
if (Test-Path $finalSetup) {
    Write-Host ""
    Write-Host "Installer Location:" -ForegroundColor Cyan
    Write-Host "  $finalSetup" -ForegroundColor Green
    Write-Host ""
    
    $setupSize = [math]::Round(((Get-Item $finalSetup).Length / 1MB), 2)
    Write-Host "  Size: $setupSize MB" -ForegroundColor Gray
}

Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Cyan
Write-Host "  1. Test the installer on a clean Windows VM" -ForegroundColor White
Write-Host "  2. Verify SQL Server auto-installation works" -ForegroundColor White
Write-Host "  3. Test application startup after installation" -ForegroundColor White
Write-Host "  4. Code sign the installer (optional but recommended)" -ForegroundColor White
Write-Host "  5. Distribute to end users" -ForegroundColor White
Write-Host ""

Write-Host "Testing Command:" -ForegroundColor Cyan
Write-Host "  $finalSetup" -ForegroundColor White
Write-Host ""
