# SPICE Android Application

A comprehensive Android application for healthcare management and patient care, built with modern Android development practices.

## Table of Contents

- Overview
- Prerequisites
- System Requirements
- Installation Guide
- Configuration
- Building the Application
- Running the Application
- Project Structure
- Troubleshooting

## Overview

SPICE (Sierra Leone Primary Care Initiative) is a comprehensive healthcare management application designed for community health workers, medical professionals, and healthcare administrators. The application supports patient registration, medical reviews, household management, and various healthcare workflows.

### Key Features
- **Patient Management**: Registration, search, and medical history tracking
- **Household Management**: Family registration and relationship tracking
- **Medical Reviews**: Comprehensive medical assessment tools
- **Offline Support**: Data synchronization and offline capabilities
- **Multi-language Support**: Localized content for different regions
- **Role-based Access**: Different interfaces for various healthcare roles

## Prerequisites

### Backend Setup
Before setting up the Android application, ensure you have access to the SPICE backend server:

1. **Backend Repository**: Clone the backend repository (contact your system administrator for access)
2. **Server Configuration**: Configure and run the essential backend services
3. **Server URL**: Obtain the backend server URL for configuration

### Required Tools

#### 1. Android Studio
- **Download**: [Android Studio](https://developer.android.com/studio/install)
- **Version**: Latest stable version (recommended: Android Studio Hedgehog or newer)
- **Installation**: Follow the official installation guide for your operating system

#### 2. Java Development Kit (JDK)
- **Version**: Java 18 (required)
- **Download**: [Oracle JDK 18](https://www.oracle.com/java/technologies/javase/jdk18-archive-downloads.html)

**Check Java Version:**
```bash
java -version
```

**Expected Output:**
```
java version "18.x.x"
Java(TM) SE Runtime Environment (build 18.x.x+x)
Java HotSpot(TM) 64-Bit Server VM (build 18.x.x+x, mixed mode, sharing)
```

#### 3. Git
- **Download**: [Git Official Site](https://git-scm.com/downloads)
- **Installation**: Follow platform-specific instructions

**Check Git Version:**
```bash
git --version
```

## System Requirements

### Development Environment
- **Operating System**: Windows 10/11, macOS 10.15+, or Ubuntu 18.04+
- **RAM**: Minimum 8GB (16GB recommended)
- **Storage**: At least 10GB free space
- **Processor**: Multi-core processor (Intel i5/AMD Ryzen 5 or better)

### Android Development
- **Android SDK**: API level 23 (Android 6.0) to API level 34 (Android 14)
- **Target SDK**: API level 34
- **Minimum SDK**: API level 23

## Installation Guide

### Step 1: Clone the Repository

```bash
# Clone the repository
git clone <repository-url>
cd Spice-SL

# Verify the project structure
ls -la
```

### Step 2: Open in Android Studio

You can open the project from either the repository root or the Spice-SL folder:

1. Launch Android Studio
2. Select **File** → **Open** (or "Open an existing Android Studio project")
3. Navigate to your clone and choose:
   - **Repository root** (`spice_mobile`) – recommended; Gradle is configured so the app and analytics modules are included from `Spice-SL/`
   - Or the **Spice-SL** directory if you prefer to open only that subfolder
4. Click **OK** and let Gradle sync

### Step 3: Configure Java Version

If you have multiple Java versions installed, ensure Android Studio uses Java 18:

1. Go to **File** → **Project Structure**
2. Select **SDK Location** from the left panel
3. Under **JDK Location**, click the dropdown and select Java 18
4. Click **Apply** and **OK**
5. Rebuild the project: **Build** → **Rebuild Project**

### Step 4: Sync Gradle Files

1. Click **File** → **Sync Project with Gradle Files**
2. Wait for the sync to complete
3. Resolve any dependency issues if they occur

## Configuration

### Step 1: Configure Environment Variables (SL Dev)

Environment-specific values (such as server URLs, salts, API keys, etc.) are managed via the `environment.properties` file located at the **project root** (next to `build.gradle.kts`, not inside `app/`).

1. **Create or update the `environment.properties` file** at the root of your project.
2. **Add the required keys and values for the `SL` dev flavor**. Example:

    ```properties
    # Development flavor only
    SL_DEV_API_BASE_URL=https://your-dev-api-url.dev/
    SL_DEV_ADMIN_BASE_URL=https://your-dev-admin-url.dev/
    SL_DEV_SALT_KEY=YOUR_DEV_SALT_KEY
    SL_DEV_DB_ENCRYPTION_KEY=YOUR_DEV_DB_ENCRYPTION_KEY

    STAGE_JKS_ALIAS=your_dev_key_alias
    STAGE_JKS_KEY_PASSWORD=your_dev_key_password
    STAGE_JKS_STORE_PASSWORD=your_dev_store_password
    ```

3. **Do not commit sensitive values** (like production salts or API keys) to version control. Use a template file (e.g., `environment.properties.example`) for sharing required keys with your team.

4. **The build system will automatically load these values** from `environment.properties` for the appropriate build variant, including server URLs and signing configuration. No manual editing of build.gradle.kts is required for these values.

---

### Step 2: Configure Product Flavors

The application supports multiple product flavors:

- **africa**: For African region deployments
- **sl**: For Sierra Leone deployments  
- **tiberbu**: For Tiberbu health deployments

Select the appropriate flavor for your deployment:

1. In Android Studio, go to **Build** → **Select Build Variant**
2. Choose your desired flavor and build type (debug/release/staging)

## Building the Application

### Debug Build
```bash
# Using Gradle wrapper
./gradlew assembleDebug

# Or using Android Studio
# Build → Make Project
```

### Release Build
```bash
# Using Gradle wrapper
./gradlew assembleRelease

# Or using Android Studio
# Build → Generate Signed Bundle/APK
```

### Build Variants
- **Debug**: Development version with debugging enabled
- **Release**: Production version with optimizations
- **Staging**: Testing version for staging environment
- **Training**: Training environment version

## Running the Application

### Option 1: Android Emulator

1. **Create Virtual Device**:
   - Go to **Tools** → **AVD Manager**
   - Click **Create Virtual Device**
   - Select device definition (e.g., Pixel 6)
   - Choose system image (API level 23 or higher)
   - Click **Finish**

2. **Start Emulator**:
   - Select your virtual device in AVD Manager
   - Click the **Play** button

3. **Run Application**:
   - Click **Run** button (green play icon) in Android Studio
   - Select your emulator as deployment target
   - Click **OK**

### Option 2: Physical Device

1. **Enable Developer Options**:
   - Go to **Settings** → **About Phone**
   - Tap **Build Number** 7 times
   - Go back to **Settings** → **Developer Options**
   - Enable **USB Debugging**

2. **Connect Device**:
   - Connect your device via USB
   - Allow USB debugging when prompted
   - Select your device in Android Studio

3. **Run Application**:
   - Click **Run** button
   - Select your device as deployment target

## Project Structure

```
Spice-SL/
├── app/                          # Main application module
│   ├── src/main/
│   │   ├── java/com/medtroniclabs/spice/
│   │   │   ├── ui/               # User interface components
│   │   │   ├── data/             # Data models and repositories
│   │   │   ├── db/               # Database entities and DAOs
│   │   │   ├── network/          # Network communication
│   │   │   ├── common/           # Common utilities and constants
│   │   │   └── di/               # Dependency injection
│   │   ├── res/                  # Resources (layouts, strings, etc.)
│   │   └── AndroidManifest.xml   # App manifest
│   └── build.gradle.kts          # App-level build configuration
├── analytics/                    # Analytics module
├── build.gradle.kts              # Project-level build configuration
├── settings.gradle.kts           # Project settings
└── gradle.properties             # Gradle properties
```

### Key Components

- **Activities**: Main UI screens and navigation
- **Fragments**: Reusable UI components
- **ViewModels**: Business logic and data management
- **Repositories**: Data access layer
- **Database**: Local data storage using Room
- **Network**: API communication using Retrofit

## Troubleshooting

### Common Issues

#### 1. Gradle Sync Failed
```bash
# Clean and rebuild
./gradlew clean
./gradlew build
```

#### 2. Java Version Issues
- Ensure Java 18 is installed and configured
- Check JAVA_HOME environment variable
- Verify Android Studio JDK settings

#### 3. Build Errors
```bash
# Clear Android Studio caches
File → Invalidate Caches and Restart
```

#### 4. Emulator Issues
- Ensure virtualization is enabled in BIOS
- Allocate sufficient RAM to emulator
- Use x86 system images for better performance

#### 5. Network Issues
- Verify server URLs are accessible
- Check internet connectivity
- Ensure backend services are running

---

**Note**: This application requires proper backend infrastructure and database setup. Ensure all prerequisites are met before attempting to run the application.
