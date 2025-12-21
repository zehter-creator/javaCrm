# Commercial Deployment Guide for TicariCRM

## Executive Summary

This guide provides complete instructions for creating a **professional, commercial-grade Windows installer** (Setup.exe) for the TicariCRM desktop application. The installer guarantees successful first-run deployment without manual database or SQL Server setup.

**Target Outcome:** A single `TicariCRM_Setup_1.0.0.exe` file that:
- Installs the application
- Installs SQL Server Express (if needed)
- Creates the database automatically
- Ensures the application runs successfully on first launch
- Requires zero manual configuration from end users

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────┐
│           TicariCRM_Setup_1.0.0.exe                 │
│         (Inno Setup Installer)                       │
└──────────────────┬──────────────────────────────────┘
                   │
         ┌─────────┴─────────┐
         │                   │
         ▼                   ▼
┌──────────────────┐   ┌──────────────────┐
│  SQL Server      │   │  Application     │
│  Express 2022    │   │  Files           │
│  (Silent Install)│   │  + Bundled JRE   │
└────────┬─────────┘   └────────┬─────────┘
         │                      │
         └──────────┬───────────┘
                    │
                    ▼
         ┌──────────────────────┐
         │  Bootstrap Process   │
         │  (Automatic)         │
         │  - Detect SQL Server │
         │  - Start Service     │
         │  - Create Database   │
         │  - Create Tables     │
         └──────────┬───────────┘
                    │
                    ▼
         ┌──────────────────────┐
         │  Application Starts  │
         │  Successfully        │
         └──────────────────────┘
```

---

## Prerequisites for Building the Installer

### Required Software

1. **Java Development Kit 17 or 21**
   - Download: https://adoptium.net/temurin/releases/
   - Used for building the application

2. **Apache Maven 3.6+**
   - Download: https://maven.apache.org/download.cgi
   - Used for building the Java application

3. **Inno Setup 6.x**
   - Download: https://jrsoftware.org/isdl.php
   - Used for creating the Windows installer
   - **CRITICAL:** This is the installer creation tool

4. **Launch4j (Optional but Recommended)**
   - Download: http://launch4j.sourceforge.net/
   - Creates native .exe wrapper for the JAR file
   - Improves user experience (no visible Java console)

5. **PowerShell 5.1 or higher**
   - Pre-installed on Windows 10/11
   - Used for build automation scripts

### Optional Software

6. **Code Signing Certificate**
   - Purchase from DigiCert, Sectigo, or similar CA
   - Prevents Windows SmartScreen warnings
   - Estimated cost: $200-$500/year
   - **Highly recommended for commercial distribution**

---

## Directory Structure

Before building, your project should have this structure:

```
javaCrm/
├── src/                              # Application source code
├── target/                           # Maven build output (auto-generated)
│   ├── Crm-1.0-SNAPSHOT.jar         # Main application JAR
│   ├── lib/                         # Dependencies
│   └── TicariCRM.exe                # Native launcher (if using Launch4j)
├── jre/                              # Bundled Java Runtime (download required)
│   ├── bin/
│   ├── lib/
│   └── ...
├── bootstrap/                        # Bootstrap PowerShell scripts
│   ├── install-sqlserver.ps1       # SQL Server installer script
│   └── bootstrap-launcher.ps1      # Pre-flight validation
├── installer/                        # Installer configuration
│   ├── TicariCRM.iss               # Inno Setup script
│   └── SQLServerExpress.exe        # SQL Server offline installer (download required)
├── dist/                            # Final installer output (auto-generated)
│   └── TicariCRM_Setup_1.0.0.exe  # YOUR FINAL DELIVERABLE
├── launch4j-config.xml             # Launch4j configuration
├── build-installer.ps1             # Automated build script
└── pom.xml                          # Maven configuration
```

---

## Step-by-Step Build Instructions

### Phase 1: Prepare External Dependencies

#### 1.1 Download Java Runtime Environment (JRE)

You need to bundle a JRE so end users don't need Java installed.

**Option A: Automatic Download (Recommended)**
```powershell
# Run the build script with automatic JRE download
.\build-installer.ps1 -Version "1.0.0"
```

**Option B: Manual Download**
1. Go to: https://adoptium.net/temurin/releases/
2. Select:
   - **Version:** 21
   - **Operating System:** Windows
   - **Architecture:** x64
   - **Package Type:** JRE
3. Download the `.zip` file
4. Extract to `javaCrm/jre/`
5. Verify structure: `javaCrm/jre/bin/java.exe` should exist

**Expected Size:** ~150-200 MB compressed, ~300-400 MB extracted

#### 1.2 Download SQL Server Express Installer (Optional)

You can bundle SQL Server Express for offline installation.

**Manual Download:**
1. Go to: https://go.microsoft.com/fwlink/p/?linkid=2216019
2. Download `SQL2022-SSEI-Expr.exe` (web installer)
3. OR download full offline installer: https://www.microsoft.com/en-us/sql-server/sql-server-downloads
4. Save as: `javaCrm/installer/SQLServerExpress.exe`

**Expected Size:** ~500-1500 MB (full offline installer)

**Note:** If you don't bundle it, the installer will download SQL Server on-demand during installation (requires internet connection).

---

### Phase 2: Build the Application

#### 2.1 Clean and Build with Maven

```powershell
# Navigate to project directory
cd javaCrm

# Clean previous builds
mvn clean

# Build application (skip tests for faster build)
mvn package -DskipTests
```

**Expected Output:**
- `target/Crm-1.0-SNAPSHOT.jar` (main application)
- Build time: ~30 seconds

#### 2.2 Copy Dependencies

```powershell
# Copy all dependencies to target/lib
mvn dependency:copy-dependencies -DoutputDirectory=target/lib
```

**Expected Output:**
- `target/lib/*.jar` (64 dependency JARs)

---

### Phase 3: Create Native Launcher (Optional)

This step creates `TicariCRM.exe` instead of relying on `java -jar Crm.jar`.

#### 3.1 Install Launch4j

Download and install from: http://launch4j.sourceforge.net/

Default installation path: `C:\Program Files (x86)\Launch4j\`

#### 3.2 Build Native Executable

```powershell
# Using Launch4j command line
& "C:\Program Files (x86)\Launch4j\launch4jc.exe" launch4j-config.xml
```

**Expected Output:**
- `target/TicariCRM.exe` (native Windows executable)
- Size: ~2-3 MB

**Benefits:**
- ✅ No visible Java console window
- ✅ Professional Windows executable
- ✅ Custom icon in Task Manager
- ✅ Single-instance checking (prevents multiple launches)

---

### Phase 4: Build the Installer

#### 4.1 Install Inno Setup

Download and install from: https://jrsoftware.org/isdl.php

Default installation path: `C:\Program Files (x86)\Inno Setup 6\`

#### 4.2 Build Installer with Inno Setup

**Option A: Automated Build Script (Recommended)**

```powershell
# This runs the entire pipeline: build + installer creation
.\build-installer.ps1 -Version "1.0.0"
```

**Option B: Manual Compilation**

```powershell
# Compile the Inno Setup script
& "C:\Program Files (x86)\Inno Setup 6\ISCC.exe" installer\TicariCRM.iss
```

**Expected Output:**
- `dist/TicariCRM_Setup_1.0.0.exe`
- Size: ~250-800 MB (depending on what's bundled)

**Build Time:**
- Without SQL Server bundled: ~2-3 minutes
- With SQL Server bundled: ~5-10 minutes (large file to compress)

---

### Phase 5: Code Signing (Production Only)

Code signing prevents Windows SmartScreen warnings and builds user trust.

#### 5.1 Obtain Code Signing Certificate

**Certificate Authorities:**
- DigiCert: https://www.digicert.com/signing/code-signing-certificates
- Sectigo: https://www.sectigo.com/ssl-certificates-tls/code-signing
- GlobalSign: https://www.globalsign.com/en/code-signing-certificate

**Cost:** $200-$500/year

**You'll receive:** 
- `.pfx` file (certificate + private key)
- Password to unlock the certificate

#### 5.2 Sign the Installer

```powershell
# Using Windows SDK signtool
& "C:\Program Files (x86)\Windows Kits\10\bin\10.0.19041.0\x64\signtool.exe" sign `
    /f "path\to\certificate.pfx" `
    /p "YourCertificatePassword" `
    /t "http://timestamp.digicert.com" `
    /d "Ticari CRM" `
    /du "https://ticaricrm.com" `
    "dist\TicariCRM_Setup_1.0.0.exe"
```

**Verification:**
```powershell
# Verify signature
& "C:\Program Files (x86)\Windows Kits\10\bin\10.0.19041.0\x64\signtool.exe" verify /pa "dist\TicariCRM_Setup_1.0.0.exe"
```

**Result:** Windows will show "Verified publisher: [Your Company]" instead of "Unknown publisher"

---

## Installer Behavior and Features

### First-Run Installation Flow

```
User double-clicks TicariCRM_Setup_1.0.0.exe
    ↓
Windows UAC Prompt (Administrator privileges required)
    ↓
[Installer Welcome Screen]
    ↓
[License Agreement] (if LICENSE.txt provided)
    ↓
[Select Installation Directory]
    Default: C:\Program Files\Ticari CRM\
    ↓
[Select Components]
    ☑ Core Application Files (required)
    ☑ Java Runtime Environment 21 (required)
    ☑ Microsoft SQL Server Express 2022 (optional)
    ☑ Desktop and Start Menu Shortcuts
    ↓
[Ready to Install]
    Shows disk space required and installation path
    ↓
[Installing...]
    Progress Bar showing:
    1. Copying application files (5%)
    2. Installing JRE (25%)
    3. Installing SQL Server Express (75%) ← Takes 10-15 minutes
    4. Configuring database (90%)
    5. Creating shortcuts (95%)
    ↓
[Bootstrap Process - Automatic]
    PowerShell script runs silently:
    - Detects SQL Server
    - Starts SQL Server service
    - Creates TicariDB database
    - Tests connectivity
    ↓
[Installation Complete]
    ☑ Launch Ticari CRM
    [Finish]
    ↓
Application starts successfully on first run
```

### What Gets Installed

**Installation Directory: `C:\Program Files\Ticari CRM\`**

```
Ticari CRM/
├── TicariCRM.exe               # Native launcher
├── Crm-1.0-SNAPSHOT.jar        # Main application
├── lib/                        # 64 dependency JARs
├── jre/                        # Bundled Java Runtime (~400 MB)
│   ├── bin/java.exe
│   └── ...
├── bootstrap/                  # Bootstrap scripts
│   ├── install-sqlserver.ps1
│   └── bootstrap-launcher.ps1
├── config/                     # Configuration files
│   ├── application.properties
│   └── application-h2.properties
├── docs/                       # Documentation
│   ├── README.md
│   ├── BOOTSTRAP_README.md
│   └── BOOTSTRAP_GUIDE.md
├── icon.ico                    # Application icon
└── unins000.exe               # Uninstaller
```

**Start Menu: `Start → Programs → Ticari CRM\`**
- Ticari CRM (launches application)
- Ticari CRM (Safe Mode - H2 Database) (fallback mode)
- Documentation
- Uninstall Ticari CRM

**Desktop:** `Ticari CRM` shortcut (if selected during installation)

**Registry Keys:**
- `HKEY_LOCAL_MACHINE\Software\TicariCRM\Ticari CRM\`
  - `InstallPath` → Installation directory
  - `Version` → 1.0.0

### SQL Server Installation Details

**If SQL Server Express is selected for installation:**

**Instance Name:** `SQLEXPRESS`
**Connection String:** `localhost\SQLEXPRESS`
**SA Password:** `YourPassword123!` (configurable in code)
**Authentication:** Mixed Mode (Windows + SQL Server Auth)
**TCP/IP:** Enabled on port 1433
**Service Startup:** Automatic
**Installation Time:** 10-15 minutes

**Database Created:** `TicariDB`
**Tables:** Auto-created by Hibernate on first application launch (19 tables)

### Uninstallation Behavior

```
User runs: Uninstall Ticari CRM (from Start Menu or Control Panel)
    ↓
[Uninstall Confirmation]
    Are you sure you want to uninstall Ticari CRM?
    ↓
[Database Preservation]
    Dialog: "Do you want to remove the database as well?"
    Warning: This will permanently delete all your data!
    
    [Yes] → Drops TicariDB database
    [No] → Keeps database for future installations
    ↓
[SQL Server Preservation]
    Dialog: "Do you want to uninstall SQL Server Express?"
    Warning: Other applications might be using SQL Server!
    Recommendation: Keep SQL Server installed
    
    [Yes] → Shows manual uninstall instructions
    [No] → Keeps SQL Server (recommended)
    ↓
[Uninstalling...]
    Removes:
    - Application files
    - JRE
    - Shortcuts
    - Registry keys
    
    Keeps (if user chose):
    - SQL Server Express
    - TicariDB database
    ↓
[Uninstall Complete]
```

---

## Testing the Installer

### Test Environment Requirements

**Recommended:** Clean Windows 10 or Windows 11 Virtual Machine

**VM Configuration:**
- **OS:** Windows 10 Pro/Enterprise or Windows 11
- **RAM:** 4 GB minimum (8 GB recommended)
- **Disk:** 50 GB free space
- **Internet:** Required for first-time SQL Server download (if not bundled)

**VM Software Options:**
- VMware Workstation Player (Free): https://www.vmware.com/products/workstation-player.html
- Oracle VirtualBox (Free): https://www.virtualbox.org/
- Hyper-V (Built into Windows 10/11 Pro)

### Testing Procedure

#### Test 1: Fresh Installation (No SQL Server)

**Purpose:** Verify SQL Server auto-installation works

**Steps:**
1. Start clean Windows VM (no SQL Server installed)
2. Copy `TicariCRM_Setup_1.0.0.exe` to VM
3. Double-click installer
4. Accept UAC prompt
5. Follow installation wizard
6. Select "Microsoft SQL Server Express" component
7. Wait for installation to complete (15-20 minutes)
8. Check "Launch Ticari CRM" and click Finish
9. **Expected:** Application starts successfully
10. **Verify:** Main window appears, no error dialogs

**Success Criteria:**
- ✅ Installer completes without errors
- ✅ SQL Server Express installs successfully
- ✅ Database TicariDB created automatically
- ✅ Application launches and shows main window
- ✅ No crash or error dialogs

#### Test 2: Installation with Existing SQL Server

**Purpose:** Verify installer detects existing SQL Server

**Steps:**
1. Install SQL Server Express manually (or reuse VM from Test 1)
2. Copy new version of `TicariCRM_Setup_1.0.0.exe` to VM
3. Double-click installer
4. Follow installation wizard
5. **Note:** SQL Server component should be unchecked or skipped
6. Wait for installation
7. Launch application
8. **Expected:** Application uses existing SQL Server instance

**Success Criteria:**
- ✅ Installer detects existing SQL Server
- ✅ Does not attempt to reinstall SQL Server
- ✅ Application launches successfully
- ✅ Connects to existing SQL Server instance

#### Test 3: Upgrade Installation

**Purpose:** Verify upgrades preserve data

**Steps:**
1. Install version 1.0.0 (from Test 1 or 2)
2. Launch application and create some test data:
   - Add a customer
   - Add a product
   - Create an invoice
3. Close application
4. Build new installer with version 1.0.1 (change version in build-installer.ps1)
5. Install version 1.0.1 over version 1.0.0
6. Launch application
7. **Expected:** All previous data still exists

**Success Criteria:**
- ✅ Upgrade completes without errors
- ✅ Database and data preserved
- ✅ Application launches successfully
- ✅ All previously entered data intact

#### Test 4: Uninstallation

**Purpose:** Verify clean uninstallation

**Steps:**
1. Open Control Panel → Programs and Features
2. Find "Ticari CRM" in the list
3. Click Uninstall
4. When prompted about database: Click "No" (keep database)
5. When prompted about SQL Server: Click "No" (keep SQL Server)
6. Wait for uninstallation
7. **Expected:** Application removed, database and SQL Server remain

**Verification:**
1. Check: `C:\Program Files\Ticari CRM\` should NOT exist
2. Check: Start Menu entry should be removed
3. Check SQL Server: Open SQL Server Management Studio
   - Connect to `localhost\SQLEXPRESS`
   - Database `TicariDB` should still exist ✅
4. Reinstall application
5. **Expected:** Application reconnects to existing database with preserved data

**Success Criteria:**
- ✅ Application files removed cleanly
- ✅ Database preserved (if chosen)
- ✅ SQL Server preserved (if chosen)
- ✅ Reinstallation works with preserved data

#### Test 5: Error Scenarios

**Purpose:** Verify graceful error handling

**Test 5A: Insufficient Disk Space**
- Reduce VM disk to < 8 GB free
- Attempt installation
- **Expected:** Installer warns about insufficient disk space

**Test 5B: No Internet + SQL Server Not Bundled**
- Disable VM network adapter
- Install without SQL Server component bundled
- **Expected:** Installer either:
  - Prompts to skip SQL Server installation, OR
  - Shows clear error message with resolution steps

**Test 5C: Installation Interrupted**
- Start installation
- Kill installer process mid-installation
- Restart VM
- **Expected:** Incomplete installation cleaned up
- Retry installation
- **Expected:** Second attempt succeeds

---

## Troubleshooting Common Issues

### Issue 1: Installer Creation Fails

**Error:** "Inno Setup Compiler not found"

**Solution:**
1. Install Inno Setup from: https://jrsoftware.org/isdl.php
2. Verify installation path: `C:\Program Files (x86)\Inno Setup 6\ISCC.exe`
3. If installed elsewhere, edit `build-installer.ps1` and update `$InnoSetupPath`

---

### Issue 2: JRE Download Fails

**Error:** "Failed to download JRE"

**Solution (Manual Download):**
1. Go to: https://adoptium.net/temurin/releases/
2. Download: JRE 21, Windows x64, ZIP format
3. Extract ZIP to `javaCrm/jre/`
4. Verify: `javaCrm/jre/bin/java.exe` exists
5. Run build script: `.\build-installer.ps1 -SkipJREDownload`

---

### Issue 3: Installer Size Too Large

**Problem:** Installer is 1+ GB due to SQL Server bundling

**Solutions:**

**Option A: Don't Bundle SQL Server (Recommended)**
- Remove `SQLServerExpress.exe` from `installer/` directory
- Installer will download SQL Server on-demand during installation
- Reduces installer size to ~250 MB

**Option B: Use Web Installer for SQL Server**
- Download SQL Server web installer (smaller, ~10 MB)
- Modify `install-sqlserver.ps1` to use web installer
- Still requires internet during installation

**Option C: Distribute via Download Manager**
- Host installer on your website
- Use a download manager (e.g., AWS S3 + CloudFront)
- Users download directly from your servers

---

### Issue 4: Windows SmartScreen Warning

**Problem:** Windows shows "Windows protected your PC" warning

**Cause:** Installer is not code signed

**Temporary Workaround for Testing:**
1. Click "More info"
2. Click "Run anyway"

**Permanent Solution:**
1. Purchase code signing certificate ($200-$500/year)
2. Sign the installer with signtool
3. Build reputation (Windows learns your certificate over time)

---

### Issue 5: SQL Server Installation Fails

**Error:** "SQL Server installation failed with exit code: [ERROR_CODE]"

**Solutions:**

**Check Installation Logs:**
```
C:\Program Files\Microsoft SQL Server\150\Setup Bootstrap\Log\Summary.txt
```

**Common Issues:**
1. **Insufficient disk space** → Free up at least 8 GB
2. **Previous installation corrupt** → Uninstall remnants manually
3. **.NET Framework missing** → Install .NET Framework 4.7.2+
4. **Conflicting services** → Stop any conflicting SQL Server services

**Manual Verification:**
```powershell
# Check if SQL Server installed
Get-Service MSSQL* | Format-Table

# Try to start service
net start MSSQL$SQLEXPRESS

# Check if port is listening
netstat -an | findstr 1433
```

---

## Production Deployment Checklist

### Before Release

- [ ] **Testing Complete**
  - [ ] Fresh Windows 10 installation tested
  - [ ] Fresh Windows 11 installation tested
  - [ ] Upgrade scenario tested
  - [ ] Uninstall scenario tested
  - [ ] All error scenarios tested

- [ ] **Code Signing**
  - [ ] Certificate obtained
  - [ ] Installer signed
  - [ ] Signature verified

- [ ] **Documentation**
  - [ ] User manual created
  - [ ] Installation guide for end users
  - [ ] Troubleshooting guide
  - [ ] System requirements documented

- [ ] **Legal/Compliance**
  - [ ] License agreement (EULA) included
  - [ ] Privacy policy (if collecting data)
  - [ ] SQL Server license compliance verified (Express is free)

- [ ] **Distribution**
  - [ ] Hosting decided (website, cloud storage, USB drives)
  - [ ] Download page created
  - [ ] Auto-update mechanism (optional)

### Recommended Distribution Methods

**Method 1: Website Download**
- Host on your company website
- Use CDN for faster downloads (Cloudflare, AWS CloudFront)
- Provide MD5/SHA256 checksums for verification

**Method 2: Cloud Storage**
- Google Drive (free, up to 15 GB)
- Dropbox (free, up to 2 GB)
- AWS S3 (pay-per-download)

**Method 3: Physical Distribution**
- USB drives (for corporate customers)
- Include offline SQL Server installer on USB
- Professional packaging and branding

**Method 4: Software Distribution Platforms**
- Microsoft Store (requires certification)
- Ninite (for IT professionals)
- Chocolatey (package manager for Windows)

---

## Post-Release Maintenance

### Version Updates

**For Minor Updates (1.0.0 → 1.0.1):**
1. Change version in `pom.xml`
2. Change version in `build-installer.ps1`
3. Change version in `installer/TicariCRM.iss`
4. Rebuild: `.\build-installer.ps1 -Version "1.0.1"`
5. Test installer on clean VM
6. Deploy to users

**For Major Updates (1.0.0 → 2.0.0):**
- Consider database migration scripts
- Test upgrade path thoroughly
- Provide rollback instructions

### Gathering Telemetry (Optional)

To improve the product, consider adding:
- Anonymous usage statistics
- Crash reporting (e.g., Sentry, Rollbar)
- Update checking mechanism
- User feedback mechanism

**Privacy Note:** Always disclose data collection in privacy policy and obtain user consent.

---

## Summary

### What You've Built

✅ **Complete Bootstrap System**
- Auto-detects SQL Server
- Auto-installs SQL Server if missing
- Auto-creates database
- Graceful error handling

✅ **Professional Installer**
- Single Setup.exe file
- Bundles Java Runtime
- Optionally bundles SQL Server
- Creates shortcuts
- Supports upgrades
- Clean uninstallation

✅ **Commercial-Grade Quality**
- Zero manual configuration required
- First-run success guaranteed (if prerequisites met)
- User-friendly error messages
- Professional appearance

### Final Deliverable

**File:** `dist/TicariCRM_Setup_1.0.0.exe`

**Size:**
- Without SQL Server: ~250 MB
- With SQL Server: ~800 MB

**Installation Time:**
- Without SQL Server install: 2-3 minutes
- With SQL Server install: 15-20 minutes

**End User Experience:**
1. Double-click Setup.exe
2. Follow installation wizard
3. Wait 15-20 minutes (first time)
4. Application launches automatically
5. Ready to use immediately

---

## Support

For questions or issues during the build process:

1. **Check documentation:**
   - BOOTSTRAP_GUIDE.md (technical details)
   - WINDOWS_INSTALLER_GUIDE.md (installer integration)
   - This guide (deployment process)

2. **Check logs:**
   - Maven build: `target/maven-build.log`
   - Inno Setup: Console output during compilation
   - SQL Server install: `C:\Program Files\Microsoft SQL Server\*\Setup Bootstrap\Log\`

3. **Verify file paths:**
   - All paths in scripts are relative to project root
   - JRE must be in `javaCrm/jre/`
   - Installer script must be in `javaCrm/installer/TicariCRM.iss`

---

**Version:** 1.0  
**Last Updated:** December 21, 2025  
**Status:** Production Ready  
**Target:** Commercial Windows Desktop Distribution
