# Windows Build Instructions - Creating TicariCRM_Setup_1.0.0.exe

## ⚠️ Important Notice

**The full installer (TicariCRM_Setup_1.0.0.exe) MUST be built on Windows.**

This repository is currently being built on a Linux environment, which can build the Java application but **cannot** create the Windows installer.

## What Was Built Successfully (Linux)

✅ **Application JAR:** `target/Crm-1.0-SNAPSHOT.jar` (47 MB)  
✅ **Dependencies:** 64 JAR files in `target/lib/` (49 MB)  
✅ **Total Build Size:** 96 MB

**Status:** The core application is ready. Now we need to complete the Windows-specific packaging.

---

## Why Windows is Required

The installer creation process requires several Windows-only tools:

| Tool | Purpose | Windows Only? |
|------|---------|---------------|
| **Inno Setup 6** | Creates the Windows installer (.exe) | ✅ YES |
| **Launch4j** | Creates native Windows .exe launcher | ✅ YES |
| **PowerShell Scripts** | SQL Server installation automation | ⚠️ Requires Windows PowerShell |
| **SQL Server Express** | Database runtime | ✅ YES - Windows only |

**Bottom Line:** You need a Windows machine (physical or VM) to complete the build.

---

## How to Complete the Build on Windows

### Option 1: Build on Your Windows Machine (Recommended)

#### Prerequisites Installation

1. **Install Java JDK 21**
   ```
   Download: https://adoptium.net/temurin/releases/
   Select: JDK 21, Windows x64, MSI installer
   Install to: C:\Program Files\Eclipse Adoptium\jdk-21
   ```

2. **Install Maven**
   ```
   Download: https://maven.apache.org/download.cgi
   Extract to: C:\Program Files\Apache\Maven
   Add to PATH: C:\Program Files\Apache\Maven\bin
   ```
   
   Verify:
   ```powershell
   mvn --version
   ```

3. **Install Inno Setup 6** ⭐ CRITICAL
   ```
   Download: https://jrsoftware.org/isdl.php
   Install to: C:\Program Files (x86)\Inno Setup 6\
   ```
   
   This is the tool that creates the actual Setup.exe file.

4. **Install Launch4j** (Optional but recommended)
   ```
   Download: http://launch4j.sourceforge.net/
   Install to: C:\Program Files (x86)\Launch4j\
   ```
   
   Creates a native .exe launcher instead of using java -jar.

5. **Install Git**
   ```
   Download: https://git-scm.com/download/win
   ```

#### Build Steps

```powershell
# Step 1: Clone the repository (if not already done)
cd C:\Users\YourName\Projects
git clone https://github.com/zehter-creator/javaCrm.git
cd javaCrm

# Step 2: Checkout the feature branch
git checkout feature/h2-database-runtime-config
git pull origin feature/h2-database-runtime-config

# Step 3: Build the Java application
mvn clean package -DskipTests

# Step 4: Copy dependencies
mvn dependency:copy-dependencies -DoutputDirectory=target/lib

# Step 5: Run the automated build script
.\build-installer.ps1 -Version "1.0.0"

# This will:
# - Download JRE 21 automatically (~150 MB)
# - Create native .exe with Launch4j (if installed)
# - Compile Inno Setup installer
# - Output: dist/TicariCRM_Setup_1.0.0.exe
```

**Expected Output:**
```
dist/
└── TicariCRM_Setup_1.0.0.exe (~250 MB)
```

**Build Time:** 5-10 minutes (first time, includes JRE download)

---

### Option 2: Build on Windows VM

If you don't have a Windows machine, use a virtual machine:

#### A. Using VMware Workstation Player (Free)

1. **Download VMware Workstation Player**
   ```
   https://www.vmware.com/products/workstation-player.html
   ```

2. **Download Windows 10 Evaluation**
   ```
   https://www.microsoft.com/en-us/evalcenter/download-windows-10-enterprise
   Valid for 90 days
   ```

3. **Create VM:**
   - RAM: 8 GB
   - Disk: 100 GB
   - CPU: 4 cores

4. **Install prerequisites** (Java, Maven, Inno Setup, Launch4j)

5. **Clone repository and build** (follow steps above)

#### B. Using GitHub Actions (Advanced)

Create a GitHub Actions workflow to build on Windows:

```yaml
# .github/workflows/build-installer.yml
name: Build Windows Installer

on:
  workflow_dispatch:
  push:
    branches:
      - feature/h2-database-runtime-config

jobs:
  build:
    runs-on: windows-latest
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Setup Java
      uses: actions/setup-java@v4
      with:
        distribution: 'temurin'
        java-version: '21'
    
    - name: Setup Maven
      uses: actions/setup-java@v4
      with:
        distribution: 'temurin'
        java-version: '21'
        cache: 'maven'
    
    - name: Build Application
      run: |
        mvn clean package -DskipTests
        mvn dependency:copy-dependencies -DoutputDirectory=target/lib
    
    - name: Download JRE
      run: |
        # Download and extract JRE
        Invoke-WebRequest -Uri "https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jre/hotspot/normal/eclipse?project=jdk" -OutFile jre.zip
        Expand-Archive jre.zip -DestinationPath jre-temp
        Move-Item jre-temp/*/* jre/
    
    - name: Install Inno Setup
      run: |
        choco install innosetup -y
    
    - name: Build Installer
      run: |
        & "C:\Program Files (x86)\Inno Setup 6\ISCC.exe" installer\TicariCRM.iss
    
    - name: Upload Artifact
      uses: actions/upload-artifact@v4
      with:
        name: TicariCRM-Setup
        path: dist/TicariCRM_Setup_*.exe
```

**Advantages:**
- ✅ No local Windows machine needed
- ✅ Automated builds
- ✅ Free for public repositories

**Limitations:**
- ⚠️ Cannot bundle SQL Server (file too large)
- ⚠️ Build artifacts expire after 90 days

---

## ❌ Why GitHub Doesn't Accept Large .exe Files

GitHub has strict file size limits:

| File Size | GitHub Behavior |
|-----------|-----------------|
| < 50 MB | ✅ Accepted without warnings |
| 50-100 MB | ⚠️ Warning, but accepted |
| 100+ MB | ❌ **REJECTED** |

**Our Installer Size:** ~250 MB → **TOO LARGE for GitHub**

### Solutions for Distributing the Installer

#### Solution 1: GitHub Releases (Recommended)

GitHub Releases support files up to 2 GB.

**Steps:**
1. Build installer on Windows
2. Create a GitHub Release
3. Upload `TicariCRM_Setup_1.0.0.exe` to the release

**How to create a release:**
```bash
# Tag the commit
git tag -a v1.0.0 -m "Version 1.0.0 - Commercial Windows Installer"
git push origin v1.0.0

# Then on GitHub:
# 1. Go to: https://github.com/zehter-creator/javaCrm/releases
# 2. Click "Draft a new release"
# 3. Select tag: v1.0.0
# 4. Upload: TicariCRM_Setup_1.0.0.exe
# 5. Publish release
```

**Result:** Users download from: `https://github.com/zehter-creator/javaCrm/releases/download/v1.0.0/TicariCRM_Setup_1.0.0.exe`

#### Solution 2: Git LFS (Large File Storage)

Enable Git LFS for large files:

```bash
# Install Git LFS
git lfs install

# Track .exe files
git lfs track "*.exe"
git add .gitattributes

# Add and commit
git add dist/TicariCRM_Setup_1.0.0.exe
git commit -m "Add installer executable"
git push
```

**Limitations:**
- Free tier: 1 GB storage, 1 GB bandwidth/month
- Paid tiers required for larger usage

#### Solution 3: External Hosting

Host the installer externally:

**Free Options:**
- Google Drive (15 GB free)
- Dropbox (2 GB free)
- OneDrive (5 GB free)

**Paid Options:**
- AWS S3 + CloudFront (CDN)
- Azure Blob Storage
- Your own website

---

## Current Status of This Repository

✅ **Code:** Ready for building  
✅ **Documentation:** Complete  
✅ **Java Application:** Built successfully (47 MB)  
✅ **Dependencies:** Copied (64 JARs)  
❌ **Windows Installer:** Requires Windows build environment  
❌ **JRE Bundle:** Not downloaded yet (requires Windows build)  
❌ **Final .exe:** Not created yet (requires Windows + Inno Setup)

---

## What You Need to Do Next

### Immediate Steps

1. **Get Access to Windows:**
   - Use your Windows PC, OR
   - Create Windows VM, OR
   - Use GitHub Actions

2. **Install Prerequisites:**
   - Java JDK 21
   - Maven
   - Inno Setup 6 ⭐ MOST IMPORTANT
   - Launch4j (optional)

3. **Clone and Build:**
   ```powershell
   git clone https://github.com/zehter-creator/javaCrm.git
   cd javaCrm
   git checkout feature/h2-database-runtime-config
   .\build-installer.ps1 -Version "1.0.0"
   ```

4. **Verify Output:**
   ```
   dist/TicariCRM_Setup_1.0.0.exe should exist (~250 MB)
   ```

5. **Test Installer:**
   - Copy to clean Windows VM
   - Run Setup.exe
   - Verify installation succeeds

6. **Distribute:**
   - Create GitHub Release
   - Upload installer to release
   - Share download link

---

## Alternative: Quick Test Build (Without Installer)

If you just want to test the application without creating the full installer:

### On Windows

```powershell
# Build application
mvn clean package -DskipTests

# Run directly
java -jar target\Crm-1.0-SNAPSHOT.jar --spring.profiles.active=h2
```

### On Linux/Mac (Development Only)

```bash
# Build application
mvn clean package -DskipTests

# Run with H2 database (no SQL Server needed)
java -jar target/Crm-1.0-SNAPSHOT.jar --spring.profiles.active=h2
```

This runs the application with an in-memory H2 database for testing.

---

## Summary

| Task | Status | Where to Do It |
|------|--------|----------------|
| Build Java Application | ✅ DONE | Anywhere (Linux/Windows/Mac) |
| Copy Dependencies | ✅ DONE | Anywhere |
| Download JRE 21 | ❌ TODO | Windows (or GitHub Actions) |
| Create Native .exe | ❌ TODO | Windows + Launch4j |
| Build Installer | ❌ TODO | Windows + Inno Setup |
| Test Installer | ❌ TODO | Clean Windows VM |
| Distribute | ❌ TODO | GitHub Releases or external hosting |

---

## Getting Help

**If you encounter issues during Windows build:**

1. Check prerequisites are installed correctly
2. Verify paths in `build-installer.ps1` match your installation
3. Check `COMMERCIAL_DEPLOYMENT_GUIDE.md` for troubleshooting
4. Review `BUILD_QUICKSTART.md` for quick reference

**Common Issues:**

| Error | Solution |
|-------|----------|
| "Inno Setup not found" | Install from https://jrsoftware.org/isdl.php |
| "Maven not found" | Add Maven to PATH |
| "Java not found" | Install JDK 21 and set JAVA_HOME |
| "JRE download failed" | Download manually from https://adoptium.net/ |

---

## Quick Start for Windows Build

```powershell
# 1. Install prerequisites (one-time setup)
# - Download and install: Java JDK 21, Maven, Inno Setup, Launch4j

# 2. Clone and checkout
git clone https://github.com/zehter-creator/javaCrm.git
cd javaCrm
git checkout feature/h2-database-runtime-config

# 3. Build everything
.\build-installer.ps1 -Version "1.0.0"

# 4. Output is here:
dir dist\TicariCRM_Setup_1.0.0.exe

# 5. Test it!
.\dist\TicariCRM_Setup_1.0.0.exe
```

**That's it!** 🎉

---

**Document Version:** 1.0  
**Last Updated:** December 21, 2025  
**Target Audience:** Developers building the Windows installer  
**Prerequisites:** Windows machine or VM with build tools installed
