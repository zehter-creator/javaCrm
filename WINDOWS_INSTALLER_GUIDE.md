# Windows Installer Integration Guide

## Overview

This guide explains how to package the Ticari CRM application as a Windows desktop installer using jpackage or third-party tools like Inno Setup, NSIS, or WiX Toolset.

## Packaging Options

### Option 1: jpackage (Built-in Java 17+)

jpackage is included with JDK 17+ and creates native Windows installers.

#### Step 1: Build Application

```bash
mvn clean package -DskipTests
```

#### Step 2: Create Runtime Image

```bash
jlink --add-modules java.base,java.desktop,java.sql,java.naming,java.management,jdk.unsupported \
      --output runtime \
      --strip-debug \
      --no-header-files \
      --no-man-pages \
      --compress=2
```

#### Step 3: Create Installer

```bash
jpackage --input target \
         --name "TicariCRM" \
         --main-jar Crm-1.0-SNAPSHOT.jar \
         --runtime-image runtime \
         --type exe \
         --win-dir-chooser \
         --win-menu \
         --win-shortcut \
         --win-per-user-install \
         --app-version 1.0 \
         --description "Ticari CRM - Windows Desktop Application" \
         --vendor "YourCompany" \
         --icon src/main/resources/icon.ico \
         --resource-dir installer-resources
```

### Option 2: Inno Setup (Recommended)

Inno Setup provides more control and is widely used for Windows applications.

#### Installation

1. Download Inno Setup: https://jrsoftware.org/isdl.php
2. Install Inno Setup Compiler

#### Sample Script: TicariCRM.iss

```ini
#define MyAppName "Ticari CRM"
#define MyAppVersion "1.0"
#define MyAppPublisher "Your Company"
#define MyAppURL "https://yourcompany.com"
#define MyAppExeName "TicariCRM.exe"

[Setup]
AppId={{YOUR-GUID-HERE}}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
DefaultDirName={autopf}\{#MyAppName}
DisableProgramGroupPage=yes
OutputBaseFilename=TicariCRM_Setup_v{#MyAppVersion}
Compression=lzma2
SolidCompression=yes
WizardStyle=modern
PrivilegesRequired=admin
ArchitecturesAllowed=x64
ArchitecturesInstallIn64BitMode=x64

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
; Application JAR
Source: "target\Crm-1.0-SNAPSHOT.jar"; DestDir: "{app}"; Flags: ignoreversion

; Dependencies
Source: "target\lib\*"; DestDir: "{app}\lib"; Flags: ignoreversion recursesubdirs createallsubdirs

; Bootstrap Scripts
Source: "bootstrap\*"; DestDir: "{app}\bootstrap"; Flags: ignoreversion recursesubdirs createallsubdirs

; Launcher Scripts
Source: "start-application.bat"; DestDir: "{app}"; Flags: ignoreversion
Source: "start-application.exe"; DestDir: "{app}"; Flags: ignoreversion

; Configuration
Source: "src\main\resources\application.properties"; DestDir: "{app}\config"; Flags: ignoreversion

; Documentation
Source: "README.md"; DestDir: "{app}"; Flags: ignoreversion
Source: "BOOTSTRAP_GUIDE.md"; DestDir: "{app}"; Flags: ignoreversion

; Bundled JRE (if you bundle Java)
Source: "jre\*"; DestDir: "{app}\jre"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{autoprograms}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon

[Run]
; First-time bootstrap
Filename: "powershell.exe"; \
  Parameters: "-NoProfile -ExecutionPolicy Bypass -File ""{app}\bootstrap\bootstrap-launcher.ps1"" -AutoInstall -SkipUI"; \
  WorkingDir: "{app}"; \
  Flags: runhidden; \
  Description: "Initialize database"; \
  StatusMsg: "Preparing application..."

; Launch application
Filename: "{app}\{#MyAppExeName}"; \
  Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; \
  Flags: nowait postinstall skipifsilent

[Code]
function IsSQLServerInstalled(): Boolean;
var
  ResultCode: Integer;
begin
  Result := False;
  
  // Check for SQL Server service
  if Exec('sc', 'query MSSQL$SQLEXPRESS', '', SW_HIDE, ewWaitUntilTerminated, ResultCode) then
  begin
    Result := (ResultCode = 0);
  end;
  
  if not Result then
  begin
    if Exec('sc', 'query MSSQLSERVER', '', SW_HIDE, ewWaitUntilTerminated, ResultCode) then
    begin
      Result := (ResultCode = 0);
    end;
  end;
end;

function PrepareToInstall(var NeedsRestart: Boolean): String;
var
  ResultCode: Integer;
begin
  Result := '';
  NeedsRestart := False;
  
  // Check if SQL Server is installed
  if not IsSQLServerInstalled() then
  begin
    if MsgBox('SQL Server Express is required but not installed.' + #13#10 + 
              'Would you like to install it now?' + #13#10 + #13#10 +
              'Note: This will download ~300 MB and take 10-15 minutes.',
              mbConfirmation, MB_YESNO) = IDYES then
    begin
      // Extract the PowerShell script
      ExtractTemporaryFile('install-sqlserver.ps1');
      
      // Run the installation script
      if Exec('powershell.exe', 
              '-NoProfile -ExecutionPolicy Bypass -File "' + 
              ExpandConstant('{tmp}\install-sqlserver.ps1') + '"',
              '', SW_SHOW, ewWaitUntilTerminated, ResultCode) then
      begin
        if ResultCode <> 0 then
        begin
          Result := 'SQL Server installation failed. Please install SQL Server Express manually.';
        end;
      end
      else
      begin
        Result := 'Failed to run SQL Server installer. Please install SQL Server Express manually.';
      end;
    end
    else
    begin
      if MsgBox('The application requires SQL Server Express to run.' + #13#10 +
                'Do you want to continue without installing it?' + #13#10 + #13#10 +
                'You will need to install SQL Server Express later.',
                mbConfirmation, MB_YESNO) = IDNO then
      begin
        Result := 'Installation cancelled.';
      end;
    end;
  end;
end;

[UninstallRun]
; Note: We don't uninstall SQL Server as it might be used by other applications
Filename: "powershell.exe"; \
  Parameters: "-Command ""if (Get-Service 'MSSQL$SQLEXPRESS' -ErrorAction SilentlyContinue) {{ Stop-Service 'MSSQL$SQLEXPRESS' }}"""; \
  RunOnceId: "StopSQLServer"; \
  Flags: runhidden
```

### Option 3: Launch4j + Inno Setup

Launch4j creates a native Windows .exe wrapper for your JAR file.

#### Step 1: Install Launch4j

Download from: http://launch4j.sourceforge.net/

#### Step 2: Create launch4j Configuration

File: `launch4j.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<launch4jConfig>
  <dontWrapJar>false</dontWrapJar>
  <headerType>gui</headerType>
  <jar>target/Crm-1.0-SNAPSHOT.jar</jar>
  <outfile>target/TicariCRM.exe</outfile>
  <errTitle>Ticari CRM</errTitle>
  <cmdLine></cmdLine>
  <chdir>.</chdir>
  <priority>normal</priority>
  <downloadUrl>https://adoptium.net/</downloadUrl>
  <supportUrl>https://yourcompany.com/support</supportUrl>
  <stayAlive>false</stayAlive>
  <restartOnCrash>false</restartOnCrash>
  <manifest></manifest>
  <icon>src/main/resources/icon.ico</icon>
  <jre>
    <path>jre</path>
    <bundledJre64Bit>true</bundledJre64Bit>
    <bundledJreAsFallback>false</bundledJreAsFallback>
    <minVersion>17</minVersion>
    <maxVersion></maxVersion>
    <jdkPreference>preferJre</jdkPreference>
    <runtimeBits>64</runtimeBits>
    <initialHeapSize>256</initialHeapSize>
    <maxHeapSize>1024</maxHeapSize>
  </jre>
  <splash>
    <file>src/main/resources/splash.bmp</file>
    <waitForWindow>true</waitForWindow>
    <timeout>60</timeout>
    <timeoutErr>true</timeoutErr>
  </splash>
  <versionInfo>
    <fileVersion>1.0.0.0</fileVersion>
    <txtFileVersion>1.0.0.0</txtFileVersion>
    <fileDescription>Ticari CRM</fileDescription>
    <copyright>Copyright © 2025</copyright>
    <productVersion>1.0.0.0</productVersion>
    <txtProductVersion>1.0.0.0</txtProductVersion>
    <productName>Ticari CRM</productName>
    <companyName>Your Company</companyName>
    <internalName>TicariCRM</internalName>
    <originalFilename>TicariCRM.exe</originalFilename>
  </versionInfo>
</launch4jConfig>
```

#### Step 3: Build Executable

```bash
launch4jc launch4j.xml
```

## Bundling Java Runtime

### Option A: jlink (Minimal Runtime)

Create a minimal JRE with only required modules:

```bash
jlink --add-modules java.base,java.desktop,java.sql,java.naming,java.management,java.xml,java.logging,jdk.crypto.ec \
      --output jre \
      --strip-debug \
      --no-header-files \
      --no-man-pages \
      --compress=2 \
      --include-locales=en
```

**Size:** ~50-80 MB (compressed)

### Option B: Full JRE Distribution

Download Temurin JRE 21:

```bash
# Windows x64
https://adoptium.net/temurin/releases/?version=21&os=windows&arch=x64&package=jre
```

Extract and include in installer as `jre/` directory.

**Size:** ~150-200 MB (compressed)

## Pre-Installation Requirements

### Administrator Privileges

SQL Server installation requires admin rights. Your installer must:

1. Request elevation: `PrivilegesRequired=admin` (Inno Setup)
2. Show UAC prompt on startup
3. Validate admin rights before SQL Server installation

### Disk Space Check

```ini
[Setup]
; Inno Setup
ExtraDiskSpaceRequired=8589934592  ; 8 GB for SQL Server + Application
```

### .NET Framework Check

SQL Server Express 2022 requires .NET Framework 4.7.2 or higher (usually pre-installed on Windows 10+).

## Installation Flow

### Recommended Installation Steps

```
1. Welcome Screen
   └─> Show application logo and description

2. License Agreement
   └─> Show EULA (if applicable)

3. Installation Directory Selection
   └─> Default: C:\Program Files\TicariCRM
   └─> Allow user to change path

4. SQL Server Check
   ├─> Check if SQL Server Express is installed
   ├─> If not installed:
   │   ├─> Show message: "SQL Server Express required"
   │   ├─> Ask: Install automatically? (Yes/No)
   │   └─> If Yes: Run install-sqlserver.ps1
   └─> If already installed: Skip

5. File Copying
   ├─> Extract application files
   ├─> Extract JRE (if bundled)
   ├─> Extract bootstrap scripts
   └─> Create config directory

6. Configuration
   ├─> Run bootstrap-launcher.ps1 -SkipUI
   ├─> Verify SQL Server service running
   ├─> Create database if needed
   └─> Verify connectivity

7. Shortcuts Creation
   ├─> Desktop shortcut (optional)
   ├─> Start Menu entry
   └─> Create uninstaller

8. Completion
   ├─> Show success message
   └─> Option to launch application
```

## Post-Installation Configuration

### Registry Keys

Create registry keys for application info:

```ini
[Registry]
Root: HKLM; Subkey: "Software\YourCompany\TicariCRM"; Flags: uninsdeletekey
Root: HKLM; Subkey: "Software\YourCompany\TicariCRM"; ValueType: string; ValueName: "InstallPath"; ValueData: "{app}"
Root: HKLM; Subkey: "Software\YourCompany\TicariCRM"; ValueType: string; ValueName: "Version"; ValueData: "{#MyAppVersion}"
```

### File Associations (Optional)

If your application handles specific file types:

```ini
[Registry]
Root: HKCR; Subkey: ".tcrm"; ValueType: string; ValueData: "TicariCRMFile"; Flags: uninsdeletekey
Root: HKCR; Subkey: "TicariCRMFile"; ValueType: string; ValueData: "Ticari CRM File"; Flags: uninsdeletekey
Root: HKCR; Subkey: "TicariCRMFile\DefaultIcon"; ValueType: string; ValueData: "{app}\TicariCRM.exe,0"
Root: HKCR; Subkey: "TicariCRMFile\shell\open\command"; ValueType: string; ValueData: """{app}\TicariCRM.exe"" ""%1"""
```

## Update/Upgrade Strategy

### Version Detection

```pascal
function InitializeSetup(): Boolean;
var
  InstalledVersion: String;
begin
  Result := True;
  
  if RegQueryStringValue(HKLM, 'Software\YourCompany\TicariCRM', 'Version', InstalledVersion) then
  begin
    if CompareVersion(InstalledVersion, '{#MyAppVersion}') >= 0 then
    begin
      if MsgBox('Version ' + InstalledVersion + ' is already installed.' + #13#10 +
                'Do you want to reinstall?', 
                mbConfirmation, MB_YESNO) = IDNO then
      begin
        Result := False;
      end;
    end;
  end;
end;

function CompareVersion(V1, V2: String): Integer;
// Returns: -1 if V1 < V2, 0 if V1 = V2, 1 if V1 > V2
// Implement version comparison logic
```

### Database Migration

For upgrades, preserve existing database:

```ini
[Code]
function PrepareToUpgrade(): Boolean;
var
  ResultCode: Integer;
begin
  Result := True;
  
  // Backup database before upgrade
  if Exec('sqlcmd', 
          '-S localhost\SQLEXPRESS -Q "BACKUP DATABASE TicariDB TO DISK=''C:\TicariDB_backup.bak''"',
          '', SW_HIDE, ewWaitUntilTerminated, ResultCode) then
  begin
    if ResultCode <> 0 then
    begin
      MsgBox('Warning: Could not backup database. Continue anyway?', mbInformation, MB_OK);
    end;
  end;
end;
```

## Uninstallation

### Clean Uninstall

```ini
[UninstallDelete]
Type: files; Name: "{app}\*.log"
Type: filesandordirs; Name: "{app}\temp"
Type: filesandordirs; Name: "{app}\cache"

[Code]
procedure CurUninstallStepChanged(CurUninstallStep: TUninstallStep);
var
  ResultCode: Integer;
begin
  if CurUninstallStep = usPostUninstall then
  begin
    // Ask user if they want to keep database
    if MsgBox('Do you want to remove the database as well?' + #13#10 +
              'This will delete all your data permanently!',
              mbConfirmation, MB_YESNO) = IDYES then
    begin
      // Drop database
      Exec('sqlcmd',
           '-S localhost\SQLEXPRESS -Q "DROP DATABASE TicariDB"',
           '', SW_HIDE, ewWaitUntilTerminated, ResultCode);
    end;
    
    // Ask about SQL Server
    if MsgBox('Do you want to uninstall SQL Server Express as well?' + #13#10 +
              '(Other applications might be using it)',
              mbConfirmation, MB_YESNO) = IDYES then
    begin
      // Uninstall SQL Server
      // This requires the SQL Server uninstaller path
    end;
  end;
end;
```

## Automated Build Script

Create a build script that handles everything:

### build-installer.ps1

```powershell
# Complete build and installer creation script

param(
    [string]$Version = "1.0.0"
)

$ErrorActionPreference = "Stop"

Write-Host "Building Ticari CRM Installer v$Version" -ForegroundColor Cyan
Write-Host ""

# Step 1: Clean and build application
Write-Host "Step 1: Building application..." -ForegroundColor Yellow
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) { exit 1 }

# Step 2: Download JRE (if not already present)
Write-Host "Step 2: Preparing JRE..." -ForegroundColor Yellow
if (-not (Test-Path "jre")) {
    Write-Host "  Downloading Temurin JRE 21..."
    $jreUrl = "https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jre/hotspot/normal/eclipse"
    Invoke-WebRequest -Uri $jreUrl -OutFile "jre.zip"
    Expand-Archive -Path "jre.zip" -DestinationPath "jre-temp"
    Move-Item "jre-temp\*\*" "jre" -Force
    Remove-Item "jre-temp" -Recurse -Force
    Remove-Item "jre.zip"
}

# Step 3: Create launcher executable with Launch4j
Write-Host "Step 3: Creating native executable..." -ForegroundColor Yellow
if (Test-Path "C:\Program Files (x86)\Launch4j\launch4jc.exe") {
    & "C:\Program Files (x86)\Launch4j\launch4jc.exe" launch4j.xml
} else {
    Write-Host "  Warning: Launch4j not found, skipping .exe creation"
}

# Step 4: Build installer with Inno Setup
Write-Host "Step 4: Building installer..." -ForegroundColor Yellow
if (Test-Path "C:\Program Files (x86)\Inno Setup 6\ISCC.exe") {
    & "C:\Program Files (x86)\Inno Setup 6\ISCC.exe" TicariCRM.iss /DMyAppVersion=$Version
} else {
    Write-Host "  Error: Inno Setup not found"
    exit 1
}

Write-Host ""
Write-Host "Build completed successfully!" -ForegroundColor Green
Write-Host "Installer: Output\TicariCRM_Setup_v$Version.exe"
```

## Testing Checklist

Before releasing the installer:

- [ ] Test on clean Windows 10 (no SQL Server)
- [ ] Test on Windows 10 with SQL Server already installed
- [ ] Test on Windows 11
- [ ] Test installation as Administrator
- [ ] Test installation as standard user (should prompt for elevation)
- [ ] Test upgrade from previous version
- [ ] Test uninstallation (keep database)
- [ ] Test uninstallation (remove database)
- [ ] Verify desktop shortcut works
- [ ] Verify Start Menu entry works
- [ ] Verify application starts after installation
- [ ] Verify SQL Server installation works
- [ ] Verify database creation works
- [ ] Test with antivirus enabled
- [ ] Test with Windows Firewall enabled
- [ ] Verify no files left after uninstall (except database if kept)

## Distribution

### Code Signing (Recommended)

To avoid Windows SmartScreen warnings:

1. Purchase code signing certificate (e.g., DigiCert, Sectigo)
2. Sign the installer:

```bash
signtool sign /f certificate.pfx /p password /t http://timestamp.digicert.com TicariCRM_Setup.exe
```

### Update Server

Consider setting up an update server:

```xml
<!-- In your Inno Setup script -->
[Setup]
AppUpdatesURL=https://yourcompany.com/updates/ticaricrm
```

Implement auto-update functionality in the application to check for new versions.

## Summary

### Recommended Approach

1. **Use Launch4j** to create native .exe wrapper
2. **Use Inno Setup** for installer creation
3. **Bundle JRE 21** with the installer (150-200 MB)
4. **Include SQL Server Express installer** or download on-demand
5. **Request Administrator privileges** for SQL Server installation
6. **Run bootstrap** during installation to ensure database is ready
7. **Code sign** the installer for professional appearance

### Expected Installer Size

- Application JAR: ~50 MB
- Bundled JRE: ~150 MB
- Dependencies: ~50 MB
- Bootstrap scripts: ~1 MB
- **Total: ~250 MB** (without SQL Server Express bundled)

If bundling SQL Server Express: **~550 MB**

### Installation Time

- **Fresh install (with SQL Server):** 15-20 minutes
- **Fresh install (SQL Server exists):** 2-3 minutes
- **Upgrade:** 1-2 minutes

---

**Last Updated:** 2025-12-21  
**Target OS:** Windows 10/11 (64-bit)  
**Java Version:** 17 or 21  
**SQL Server:** Express 2022
