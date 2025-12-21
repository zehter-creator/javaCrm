# Build Quick Start - TicariCRM Installer

## TL;DR - Fastest Path to Setup.exe

```powershell
# 1. Clone/Navigate to project
cd javaCrm

# 2. Run the automated build script
.\build-installer.ps1 -Version "1.0.0"

# 3. Wait ~5-10 minutes

# 4. Your installer is ready
.\dist\TicariCRM_Setup_1.0.0.exe
```

**That's it!** The script handles everything automatically.

---

## What You Need Installed

Before running the build script:

| Software | Required? | Download Link |
|----------|-----------|---------------|
| **Java JDK 17/21** | ✅ YES | https://adoptium.net/ |
| **Maven 3.6+** | ✅ YES | https://maven.apache.org/download.cgi |
| **Inno Setup 6** | ✅ YES | https://jrsoftware.org/isdl.php |
| **PowerShell 5.1+** | ✅ YES (pre-installed on Windows 10/11) | - |
| **Launch4j** | ⚠️ Optional | http://launch4j.sourceforge.net/ |
| **Code Signing Cert** | ⚠️ Optional | $200-$500/year from DigiCert/Sectigo |

---

## Build Commands

### Full Automated Build

```powershell
.\build-installer.ps1 -Version "1.0.0"
```

**What it does:**
- ✅ Builds application with Maven
- ✅ Downloads JRE automatically
- ✅ Copies all dependencies
- ✅ Creates native .exe (if Launch4j installed)
- ✅ Compiles Inno Setup installer
- ✅ Output: `dist/TicariCRM_Setup_1.0.0.exe`

**Time:** 5-10 minutes

---

### Manual Step-by-Step Build

If you prefer control over each step:

```powershell
# Step 1: Build application
mvn clean package -DskipTests

# Step 2: Copy dependencies
mvn dependency:copy-dependencies -DoutputDirectory=target/lib

# Step 3: Download JRE (if not exists)
# See COMMERCIAL_DEPLOYMENT_GUIDE.md section 1.1

# Step 4: Create native launcher (optional)
& "C:\Program Files (x86)\Launch4j\launch4jc.exe" launch4j-config.xml

# Step 5: Build installer
& "C:\Program Files (x86)\Inno Setup 6\ISCC.exe" installer\TicariCRM.iss
```

**Time:** 5-10 minutes

---

## What Gets Created

### Build Artifacts

```
javaCrm/
├── target/
│   ├── Crm-1.0-SNAPSHOT.jar      # Main application (~50 MB)
│   ├── lib/                       # 64 dependencies (~50 MB)
│   └── TicariCRM.exe             # Native launcher (~2 MB) [optional]
├── jre/                           # Bundled Java Runtime (~400 MB)
└── dist/
    └── TicariCRM_Setup_1.0.0.exe # FINAL INSTALLER (~250 MB)
```

### Installer Size

- **Without SQL Server:** ~250 MB
- **With SQL Server:** ~800 MB

---

## Testing Your Installer

### Quick Test (Current Machine)

```powershell
# Run the installer on your machine
.\dist\TicariCRM_Setup_1.0.0.exe
```

**Note:** This will actually install the application. You can uninstall it later.

---

### Proper Test (Clean Windows VM)

**Recommended approach:**

1. **Create VM:**
   - VMware Workstation Player (free)
   - Oracle VirtualBox (free)
   - Hyper-V (built into Windows 10/11 Pro)

2. **VM Specs:**
   - OS: Windows 10 or 11 (64-bit)
   - RAM: 4 GB minimum
   - Disk: 50 GB free space

3. **Test:**
   ```
   - Copy Setup.exe to VM
   - Double-click to install
   - Accept UAC prompt
   - Follow installation wizard
   - Launch application
   - Verify: Main window appears
   ```

4. **Success Criteria:**
   - ✅ No errors during installation
   - ✅ SQL Server installed automatically
   - ✅ Database created automatically
   - ✅ Application launches successfully

---

## Common Issues & Quick Fixes

### Issue: "Maven not found"

```powershell
# Install Maven using Chocolatey
choco install maven

# Or download manually from:
# https://maven.apache.org/download.cgi
```

---

### Issue: "Inno Setup not found"

```powershell
# Install Inno Setup from:
# https://jrsoftware.org/isdl.php

# Then verify path:
Test-Path "C:\Program Files (x86)\Inno Setup 6\ISCC.exe"
```

---

### Issue: "JRE download failed"

**Solution 1: Manual download**

1. Go to: https://adoptium.net/temurin/releases/
2. Download: JRE 21, Windows x64, ZIP
3. Extract to: `javaCrm/jre/`
4. Verify: `javaCrm/jre/bin/java.exe` exists

**Solution 2: Skip JRE download**

```powershell
.\build-installer.ps1 -SkipJREDownload
```

---

### Issue: "Build takes too long"

**Speed up the build:**

```powershell
# Skip tests
.\build-installer.ps1 -SkipTests

# Skip JRE download (if already downloaded)
.\build-installer.ps1 -SkipJREDownload

# Skip SQL Server download
.\build-installer.ps1 -SkipSQLServerDownload

# All together
.\build-installer.ps1 -SkipTests -SkipJREDownload -SkipSQLServerDownload
```

---

## Directory Structure Reference

```
javaCrm/
├── src/                              ← Source code
├── bootstrap/                        ← Bootstrap scripts
│   ├── install-sqlserver.ps1
│   └── bootstrap-launcher.ps1
├── installer/                        ← Installer config
│   └── TicariCRM.iss
├── build-installer.ps1               ← BUILD SCRIPT (RUN THIS!)
├── launch4j-config.xml               ← Native .exe config
├── jre/                              ← Bundled Java (download required)
├── target/                           ← Build output (auto-generated)
└── dist/                             ← Final installer (auto-generated)
    └── TicariCRM_Setup_1.0.0.exe    ← YOUR DELIVERABLE
```

---

## Build Script Parameters

```powershell
.\build-installer.ps1 `
    -Version "1.0.0" `              # Version number (default: 1.0.0)
    -SkipTests `                    # Skip Maven tests (faster)
    -SkipJREDownload `              # Use existing JRE
    -SkipSQLServerDownload `        # Don't bundle SQL Server
    -Clean                           # Clean previous builds (default: true)
```

### Examples

**Production build (everything included):**
```powershell
.\build-installer.ps1 -Version "1.0.0"
```

**Fast development build:**
```powershell
.\build-installer.ps1 -SkipTests -SkipJREDownload -SkipSQLServerDownload
```

**Clean build from scratch:**
```powershell
.\build-installer.ps1 -Clean -Version "1.0.0"
```

---

## File Sizes Reference

| Component | Size (Compressed) | Size (Extracted) |
|-----------|------------------|------------------|
| Application JAR | ~50 MB | - |
| Dependencies | ~50 MB | - |
| JRE 21 | ~150 MB | ~400 MB |
| SQL Server Express | ~500 MB | ~1.5 GB |
| **Final Installer (no SQL)** | **~250 MB** | **~500 MB installed** |
| **Final Installer (with SQL)** | **~800 MB** | **~2 GB installed** |

---

## Next Steps After Building

1. **Test the installer:**
   - Test on clean Windows VM
   - Verify SQL Server auto-installation
   - Verify application starts successfully

2. **Code signing (optional but recommended):**
   ```powershell
   signtool sign /f certificate.pfx /p password /t http://timestamp.digicert.com dist\TicariCRM_Setup_1.0.0.exe
   ```

3. **Distribute:**
   - Upload to your website
   - Share download link with customers
   - Or burn to USB drives

---

## Quick Reference Links

- **Full Build Guide:** See `COMMERCIAL_DEPLOYMENT_GUIDE.md`
- **Technical Details:** See `BOOTSTRAP_GUIDE.md`
- **Installer Integration:** See `WINDOWS_INSTALLER_GUIDE.md`
- **Troubleshooting:** See `COMMERCIAL_DEPLOYMENT_GUIDE.md` section "Troubleshooting"

---

## Support Checklist

Before asking for help:

- [ ] Verified Java is installed: `java -version`
- [ ] Verified Maven is installed: `mvn --version`
- [ ] Verified Inno Setup is installed: `Test-Path "C:\Program Files (x86)\Inno Setup 6\ISCC.exe"`
- [ ] Checked build log output for specific errors
- [ ] Tried manual step-by-step build
- [ ] Verified all paths are correct
- [ ] Checked if JRE exists in `javaCrm/jre/`

---

**Last Updated:** December 21, 2025  
**Version:** 1.0  
**Status:** Production Ready

---

## One-Liner Summary

```powershell
# Everything you need to know
.\build-installer.ps1 -Version "1.0.0"
# → Wait 5-10 minutes
# → dist/TicariCRM_Setup_1.0.0.exe is ready
# → Test on clean Windows VM
# → Distribute to users
# → Done! 🎉
```
