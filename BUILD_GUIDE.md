# Ticari CRM - Production Build Guide

This guide explains how to build the Ticari CRM application for production deployment on Windows, Linux, and macOS.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Quick Start](#quick-start)
3. [Development Build](#development-build)
4. [Production Build](#production-build)
5. [Platform-Specific Builds](#platform-specific-builds)
6. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software

1. **Java Development Kit (JDK) 21 or later**
   - Download from: https://adoptium.net/ or https://www.oracle.com/java/technologies/downloads/
   - Ensure `JAVA_HOME` environment variable is set
   - Verify: `java -version` and `javac -version`

2. **Apache Maven 3.8+**
   - Download from: https://maven.apache.org/download.cgi
   - Ensure Maven is in your PATH
   - Verify: `mvn -version`

3. **Microsoft SQL Server Express**
   - Download from: https://www.microsoft.com/sql-server/sql-server-downloads
   - Configure connection settings in `src/main/resources/application.properties`

### Platform-Specific Requirements

#### Windows
- **WiX Toolset** (for .exe installer creation)
  - Download from: https://wixtoolset.org/
  - Version 3.x or later
  - Add to PATH after installation

#### Linux
- **dpkg-deb** (for DEB packages) - Usually pre-installed on Debian/Ubuntu
- **rpmbuild** (for RPM packages) - Install via: `sudo dnf install rpm-build` (Fedora/RHEL)

#### macOS
- **Xcode Command Line Tools**
  - Install via: `xcode-select --install`

---

## Quick Start

### Running in Development Mode

```bash
# Clone the repository
git clone https://github.com/zehter-creator/javaCrm.git
cd javaCrm

# Build and run
mvn clean javafx:run
```

### Creating a Simple JAR

```bash
# Build JAR with dependencies
mvn clean package

# Run the JAR
java -jar target/Crm-1.0-SNAPSHOT.jar
```

---

## Development Build

### Compile Only

```bash
mvn clean compile
```

### Run Tests

```bash
mvn test
```

### Run Application with Maven

```bash
mvn javafx:run
```

### Package as JAR

```bash
mvn clean package
```

This creates:
- `target/Crm-1.0-SNAPSHOT.jar` - Main application JAR
- `target/lib/` - All dependencies

---

## Production Build

### Step 1: Build the Application

```bash
mvn clean package -DskipTests
```

### Step 2: Create Custom JRE (Optional but Recommended)

Creating a custom JRE reduces the application size and includes only necessary modules.

```bash
# Determine required modules
jdeps --class-path 'target/lib/*' --multi-release 21 --print-module-deps target/Crm-1.0-SNAPSHOT.jar

# Create custom runtime (example)
jlink \
  --module-path $JAVA_HOME/jmods:target/lib \
  --add-modules java.base,java.sql,java.desktop,java.naming,javafx.controls,javafx.fxml,javafx.graphics,java.logging \
  --output target/runtime \
  --strip-debug \
  --no-header-files \
  --no-man-pages \
  --compress=2
```

### Step 3: Test the Build

```bash
# Run with custom JRE
target/runtime/bin/java -jar target/Crm-1.0-SNAPSHOT.jar

# Or run with system Java
java -jar target/Crm-1.0-SNAPSHOT.jar
```

---

## Platform-Specific Builds

### Windows Installer (.exe)

#### Using Build Script

```powershell
.\build-windows.ps1
```

#### Manual Build Steps

1. **Build the application**
   ```powershell
   mvn clean package
   ```

2. **Create custom JRE**
   ```powershell
   jlink --module-path "%JAVA_HOME%\jmods;target\lib" `
         --add-modules java.base,java.sql,java.desktop,java.naming,javafx.controls,javafx.fxml,javafx.graphics `
         --output target\runtime `
         --strip-debug --no-header-files --no-man-pages --compress=2
   ```

3. **Create Windows installer**
   ```powershell
   mvn package -Pwindows-installer
   ```

4. **Find the installer**
   - Location: `target/dist/TicariCRM-1.0.0.exe`

#### Configuration Options

Edit `pom.xml` under the `windows-installer` profile to customize:
- Application name
- Version
- Vendor information
- Installation directory
- Desktop/Start Menu shortcuts
- Memory settings (Xms, Xmx)

### Linux Installer (DEB/RPM)

#### Using Build Script

```bash
./build-linux.sh
```

#### Manual Build Steps

1. **Build the application**
   ```bash
   mvn clean package
   ```

2. **Create custom JRE**
   ```bash
   jlink --module-path $JAVA_HOME/jmods:target/lib \
         --add-modules java.base,java.sql,java.desktop,java.naming,javafx.controls,javafx.fxml,javafx.graphics \
         --output target/runtime \
         --strip-debug --no-header-files --no-man-pages --compress=2
   ```

3. **Create DEB package**
   ```bash
   mvn package -Plinux-installer
   ```

4. **Find the installer**
   - Location: `target/dist/ticaricrm_1.0.0-1_amd64.deb`

#### Install on Linux

```bash
# Debian/Ubuntu
sudo dpkg -i target/dist/ticaricrm_1.0.0-1_amd64.deb

# Fedora/RHEL (if RPM built)
sudo rpm -i target/dist/ticaricrm-1.0.0-1.x86_64.rpm
```

### macOS Installer (DMG)

1. **Build the application**
   ```bash
   mvn clean package
   ```

2. **Create custom JRE**
   ```bash
   jlink --module-path $JAVA_HOME/jmods:target/lib \
         --add-modules java.base,java.sql,java.desktop,java.naming,javafx.controls,javafx.fxml,javafx.graphics \
         --output target/runtime \
         --strip-debug --no-header-files --no-man-pages --compress=2
   ```

3. **Create DMG (requires macOS)**
   ```bash
   jpackage --input target \
            --name "TicariCRM" \
            --main-jar Crm-1.0-SNAPSHOT.jar \
            --runtime-image target/runtime \
            --type dmg \
            --app-version 1.0.0 \
            --vendor "Ticari Solutions" \
            --mac-package-name "TicariCRM"
   ```

---

## Application Configuration

### Database Configuration

Edit `src/main/resources/application.properties`:

```properties
# Database Connection
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=TicariDB;encrypt=false
spring.datasource.username=your_username
spring.datasource.password=your_password

# JPA/Hibernate Settings
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Application Settings
spring.application.name=Ticari CRM
```

### Memory Configuration

Adjust JVM memory settings in `pom.xml` under jpackage configuration:

```xml
<javaOptions>
    <option>-Xms256m</option>  <!-- Initial heap size -->
    <option>-Xmx1024m</option> <!-- Maximum heap size -->
</javaOptions>
```

---

## Troubleshooting

### Common Issues

#### 1. jpackage fails with "No runtime image specified"

**Problem:** Custom JRE not created or path incorrect

**Solution:** Create runtime image first using jlink:
```bash
jlink --module-path $JAVA_HOME/jmods:target/lib \
      --add-modules $(jdeps --print-module-deps target/Crm-1.0-SNAPSHOT.jar) \
      --output target/runtime \
      --strip-debug --compress=2
```

#### 2. Windows installer creation fails

**Problem:** WiX Toolset not installed or not in PATH

**Solution:**
1. Install WiX Toolset from https://wixtoolset.org/
2. Add WiX bin directory to PATH
3. Restart terminal/PowerShell

#### 3. Database connection fails

**Problem:** SQL Server not running or connection string incorrect

**Solution:**
1. Verify SQL Server is running
2. Check connection string in `application.properties`
3. Ensure database exists: `CREATE DATABASE TicariDB;`
4. Verify username/password

#### 4. JavaFX runtime components missing

**Problem:** JavaFX modules not included in build

**Solution:**
- Ensure JavaFX dependencies are in pom.xml
- Verify platform-specific JavaFX JARs are included (e.g., javafx-graphics-21.0.6-windows.jar)
- Use `mvn dependency:tree` to check dependencies

### Getting Help

If you encounter issues:

1. Check Maven output for specific errors: `mvn clean package -X`
2. Verify Java version: `java -version` (should be 21+)
3. Check module dependencies: `jdeps target/Crm-1.0-SNAPSHOT.jar`
4. Review logs in `logs/` directory

---

## Build Artifacts

After a successful build, you'll find:

- **Development JAR:** `target/Crm-1.0-SNAPSHOT.jar`
- **Dependencies:** `target/lib/*.jar`
- **Custom JRE:** `target/runtime/` (if created)
- **Installers:** `target/dist/` (platform-specific)

---

## Deployment Checklist

- [ ] Database configured and accessible
- [ ] Application.properties updated with production settings
- [ ] Custom JRE created and tested
- [ ] Application tested with production database
- [ ] Installer created and tested on target platform
- [ ] User documentation prepared
- [ ] Backup and recovery procedures documented
- [ ] Security settings reviewed (database passwords, encryption)

---

## Version Information

- **Application Version:** 1.0-SNAPSHOT
- **Java Version:** 21
- **Spring Boot Version:** 3.2.1
- **JavaFX Version:** 21.0.6
- **Maven Version:** 3.8+

---

## Additional Resources

- [JavaFX Documentation](https://openjfx.io/)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [jpackage Documentation](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jpackage.html)
- [Maven Assembly Plugin](https://maven.apache.org/plugins/maven-assembly-plugin/)

---

**Note:** This guide assumes a production environment with SQL Server Express. For other database systems, adjust the JDBC driver and connection settings accordingly.
