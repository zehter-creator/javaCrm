#!/bin/bash

# Linux/Mac Build Script for Ticari CRM
# Requires: JDK 21+ with jpackage tool
# Description: Builds a Linux DEB/RPM or Mac DMG installer for the application

echo "========================================"
echo "  Ticari CRM - Linux/Mac Build Script"
echo "========================================"
echo ""

# Check if Java is installed
echo "Checking Java installation..."
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed or not in PATH!"
    echo "Please install JDK 21 or later"
    exit 1
fi

java -version
echo ""

# Check if Maven is installed
echo "Checking Maven installation..."
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven is not installed or not in PATH!"
    echo "Please install Apache Maven"
    exit 1
fi

mvn -version
echo ""

# Clean previous builds
echo "Cleaning previous builds..."
mvn clean
if [ $? -ne 0 ]; then
    echo "ERROR: Maven clean failed!"
    exit 1
fi
echo "Clean completed successfully"
echo ""

# Compile and package
echo "Compiling and packaging application..."
mvn package -DskipTests
if [ $? -ne 0 ]; then
    echo "ERROR: Maven package failed!"
    exit 1
fi
echo "Package created successfully"
echo ""

# Create runtime image
echo "Creating custom JRE runtime image..."
MODULE_PATH="target/lib"
MODULES="java.base,java.sql,java.desktop,java.naming,javafx.controls,javafx.fxml,javafx.graphics"

if [ -d "target/runtime" ]; then
    rm -rf "target/runtime"
fi

# Note: jlink may need adjustment based on your environment
# jlink --module-path "$MODULE_PATH" --add-modules $MODULES --output "target/runtime" \
#       --strip-debug --no-header-files --no-man-pages --compress=2

echo "Runtime image creation skipped (requires proper jlink setup)"
echo ""

# Build Linux installer
echo "Building installer..."
echo "Note: For DEB packages, you need dpkg-deb installed"
echo "      For RPM packages, you need rpmbuild installed"

# Uncomment when jlink runtime is ready:
# mvn package -Plinux-installer

echo ""
echo "========================================"
echo "  Build Process Information"
echo "========================================"
echo ""
echo "JAR Location: target/Crm-1.0-SNAPSHOT.jar"
echo "Dependencies: target/lib/"
echo ""
echo "To create Linux installer:"
echo "1. Create runtime with jlink"
echo "2. For DEB: mvn package -Plinux-installer"
echo "3. For RPM: mvn package -Plinux-installer -Dtype=RPM"
echo ""
echo "To run the application:"
echo "  java -jar target/Crm-1.0-SNAPSHOT.jar"
echo ""
echo "Build completed successfully!"
