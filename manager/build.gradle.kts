@file:Suppress("SpellCheckingInspection")

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.org.apache.commons.lang3.SystemUtils

project.group = "de.openindex.zugferd"
project.version = libs.versions.application.version.get()

val isLinux = SystemUtils.IS_OS_LINUX
val isMac = SystemUtils.IS_OS_MAC
val isWindows = SystemUtils.IS_OS_WINDOWS

val isAmd64 = listOf("amd64", "x86_64")
    .contains(SystemUtils.OS_ARCH.lowercase())
val isArm64 = listOf("aarch64", "arm64")
    .contains(SystemUtils.OS_ARCH.lowercase())

//
// Build Info
// https://codeberg.org/h34tnet/buildinfo#configuration
//
buildInfo {
    className = "AppInfo"
    packageName = "de.openindex.zugferd.manager"
    path = project.layout.buildDirectory.file("generated/buildInfo").get().asFile
    addSourceSet = false

    hostNetwork = false
    hostOs = false
    java = false
    kotlin = false
    gradle = false
    guessCiProvider = false
    gitlabCi = false

    customFields = mapOf(
        "VENDOR" to "OpenIndex",
    )
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.serialization)

    // Versions Plugin
    // https://github.com/ben-manes/gradle-versions-plugin
    alias(libs.plugins.versions)

    // BuildInfo Plugin
    // https://codeberg.org/h34tnet/buildinfo
    alias(libs.plugins.buildinfo)

    // Conveyor Plugin
    // https://www.hydraulic.dev/
    // https://conveyor.hydraulic.dev/latest/
    // https://conveyor.hydraulic.dev/latest/tutorial/hare/jvm/
    //alias(libs.plugins.conveyor)
}

kotlin {
    jvmToolchain(21)
    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain {
            kotlin.srcDirs(
                project.layout.buildDirectory.file("generated/buildInfo").get().asFile
            )
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            //implementation(compose.material)
            implementation(compose.materialIconsExtended)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)

            // FileKit
            // https://github.com/vinceglb/FileKit
            implementation(libs.filekit.core)
            implementation(libs.filekit.compose)

            // Kotlinx-DateTime
            // https://github.com/Kotlin/kotlinx-datetime
            implementation(libs.kotlinx.datetime)

            // Kotlinx-Serialization
            // https://github.com/Kotlin/kotlinx.serialization
            implementation(libs.kotlinx.serialization.json)

            // WebView for JetBrains Compose Multiplatform
            // https://github.com/KevinnZou/compose-webview-multiplatform
            // https://github.com/KevinnZou/compose-webview-multiplatform#multiplatform
            //api(libs.webview.multiplatform)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)

            // AppDirs
            // https://github.com/harawata/appdirs
            implementation(libs.appdirs)

            // Commons-Lang
            // https://commons.apache.org/proper/commons-lang/
            implementation(libs.commons.lang)

            // IcePDF
            // https://github.com/pcorless/icepdf
            //implementation(libs.icepdf.core)
            //implementation(libs.icepdf.viewer)

            // JCEF-Maven
            // https://github.com/jcefmaven/jcefmaven
            implementation(libs.jcef.maven)
            if (isLinux) {
                if (isAmd64) {
                    runtimeOnly(libs.jcef.natives.linux.amd64)
                }
                if (isArm64) {
                    runtimeOnly(libs.jcef.natives.linux.arm64)
                }
            }
            if (isMac) {
                if (isAmd64) {
                    runtimeOnly(libs.jcef.natives.macosx.amd64)
                }
                if (isArm64) {
                    runtimeOnly(libs.jcef.natives.macosx.arm64)
                }
            }
            if (isWindows) {
                if (isAmd64) {
                    runtimeOnly(libs.jcef.natives.windows.amd64)
                }
            }

            // Logback
            // https://commons.apache.org/proper/commons-lang/
            implementation(libs.commons.lang)

            // Kotlinx-DateTime
            // https://logback.qos.ch/
            implementation(libs.logback.classic)

            // Mustang
            // https://www.mustangproject.org/
            // https://github.com/ZUGFeRD/mustangproject/
            implementation(libs.mustang.library)
            implementation(libs.mustang.validator)
            //implementation(variantOf(libs.mustang.validator) { classifier("shaded") })

            // Conveyor API: Manage automatic updates.
            // https://www.hydraulic.dev/
            // https://conveyor.hydraulic.dev/latest/
            // https://conveyor.hydraulic.dev/latest/tutorial/hare/jvm/
            //implementation(libs.conveyor.control)
        }
    }
}

/*
dependencies {
    // Conveyor
    // Use the configurations created by the Conveyor plugin
    // to tell Gradle/Conveyor where to find the artifacts for each platform.
    linuxAmd64(compose.desktop.linux_x64)
    macAmd64(compose.desktop.macos_x64)
    macAarch64(compose.desktop.macos_arm64)
    windowsAmd64(compose.desktop.windows_x64)

    // Native dependencies for JCEF.
    linuxAmd64(libs.jcef.natives.linux.amd64)
    macAmd64(libs.jcef.natives.macosx.amd64)
    macAarch64(libs.jcef.natives.macosx.arm64)
    windowsAmd64(libs.jcef.natives.windows.amd64)
}
*/

/*
// Conveyor
// Work around temporary Compose bugs.
// https://github.com/JetBrains/compose-jb/issues/1404#issuecomment-1146894731
// https://youtrack.jetbrains.com/issue/CMP-6112
configurations {
    all {
        attributes {
            attribute(Attribute.of("ui", String::class.java), "awt")
        }
    }
}
*/

compose.desktop {
    application {
        mainClass = "de.openindex.zugferd.manager.MainKt"

        jvmArgs("-Xmx1G")
        jvmArgs("-Dfile.encoding=UTF-8")

        //
        // Required by JCEF.
        //
        jvmArgs("--add-opens", "java.desktop/sun.awt=ALL-UNNAMED")
        jvmArgs("--add-opens", "java.desktop/java.awt.peer=ALL-UNNAMED") // recommended but not necessary
        if (isMac) {
            jvmArgs("--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED")
            jvmArgs("--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED")
        }

        buildTypes {
            release {
                proguard {
                    isEnabled = false
                }
            }
        }

        nativeDistributions {
            packageName = "ZUGFeRD-Manager"
            //packageVersion = project.version.toString()
            vendor = "OpenIndex"
            copyright = "© 2024 OpenIndex. All rights reserved."
            description = "A desktop application for creating and validating e-invoices."
            licenseFile = rootProject.file("LICENSE.txt")

            //outputBaseDir.set(rootProject.layout.buildDirectory.dir("distributions"))
            if (isLinux) {
                targetFormats(TargetFormat.Deb, TargetFormat.Rpm)
            }
            if (isMac) {
                targetFormats(TargetFormat.Dmg)
            }
            if (isWindows) {
                targetFormats(TargetFormat.Exe)
            }

            includeAllModules = true

            //
            // TODO: Further investigate module requirements.
            //

            //modules.add("java.base")
            //modules.add("java.datatransfer")
            //modules.add("java.desktop")
            //modules.add("java.logging")
            //modules.add("java.management")
            //modules.add("java.naming")
            //modules.add("java.net.http")
            //modules.add("java.prefs")
            //modules.add("java.security.jgss")
            //modules.add("java.security.sasl")
            //modules.add("java.sql")
            //modules.add("java.transaction.xa")
            //modules.add("java.xml")
            //modules.add("jdk.accessibility")
            //modules.add("jdk.crypto.ec")
            //modules.add("jdk.localedata")
            //modules.add("jdk.net")
            //modules.add("jdk.security.auth")
            //modules.add("jdk.xml.dom")

            //if (isLinux) {
            //    // Required by FileKit
            //    // https://github.com/vinceglb/FileKit#-installation
            //    modules.add("jdk.security.auth")
            //}

            linux {
                appCategory = "misc"
                appRelease = libs.versions.application.revision.get()
                menuGroup = "OpenIndex-ZUGFeRD"
                installationPath = "/opt/OpenIndex-ZUGFeRD-Manager"
                debMaintainer = "andy@openindex.de"
                rpmLicenseType = "Apache-2.0"
                packageVersion = project.version.toString()
            }

            macOS {
                bundleID = "de.openindex.zugferd.manager"
                dockName = "ZUGFeRD-Manager"
                packageVersion = project.version.toString()
                packageBuildVersion = project.version.toString()
                appStore = true
                appCategory = "public.app-category.business"

                //infoPlist {
                //    extraKeysRawXml = """
                //        <key>CFBundleDevelopmentRegion</key>
                //        <string>German</string>
                //    """
                //}
            }

            windows {
                packageVersion = project.version.toString()
                menuGroup = "OpenIndex-ZUGFeRD"
                upgradeUuid = "c8ce1e91-7f6f-45a6-a1c5-14020bc7c5e3"
                console = false
                dirChooser = true
                perUserInstall = true
            }
        }
    }
}

tasks {
    //getByName("desktopProcessResources") {
    getByName("compileKotlinDesktop") {
        dependsOn("buildInfo")
    }

    register("bundle") {
        group = "OpenIndex"
        description = "Create application bundle for current operating system."

        if (isLinux) {
            dependsOn("bundleLinuxArchive")
            dependsOn("bundleLinuxDeb")
            dependsOn("bundleLinuxRpm")
        }
        if (isMac) {
            dependsOn("bundleMacDmg")
        }
        if (isWindows) {
            dependsOn("bundleWindowsExe")
        }
    }

    if (isLinux) {
        register<Tar>("bundleLinuxArchive") {
            group = "OpenIndex"
            description = "Create application archive for Linux."
            dependsOn("createReleaseDistributable")

            archiveExtension = "tar.gz"
            archiveVersion = libs.versions.application.pkg.get()
            archiveClassifier = if (isArm64) "linux-arm64" else "linux-x64"
            compression = Compression.GZIP
            destinationDirectory = rootProject.layout.buildDirectory
            from(project.layout.buildDirectory.dir("compose/binaries/main-release/app"))
            from(rootProject.layout.projectDirectory.file("LICENSE.txt")) {
                into(project.name)
            }
        }

        register<Copy>("bundleLinuxDeb") {
            group = "OpenIndex"
            description = "Create deb installer for Linux."
            dependsOn("packageReleaseDistributionForCurrentOS")

            into(rootProject.layout.buildDirectory)
            from(project.layout.buildDirectory.dir("compose/binaries/main-release/deb"))
            rename { name ->
                if (!name.endsWith(".deb")) {
                    return@rename name
                }

                val arch = if (isArm64) "arm64" else "x64"
                "${project.name}-${libs.versions.application.pkg.get()}-linux-${arch}.deb"
            }
        }

        register<Copy>("bundleLinuxRpm") {
            group = "OpenIndex"
            description = "Create rpm installer for Linux."
            dependsOn("packageReleaseDistributionForCurrentOS")

            into(rootProject.layout.buildDirectory)
            from(project.layout.buildDirectory.dir("compose/binaries/main-release/rpm"))
            rename { name ->
                if (!name.endsWith(".rpm")) {
                    return@rename name
                }

                val arch = if (isArm64) "arm64" else "x64"
                "${project.name}-${libs.versions.application.pkg.get()}-linux-${arch}.rpm"
            }
        }
    }

    if (isMac) {
        register<Copy>("bundleMacDmg") {
            group = "OpenIndex"
            description = "Create dmg installer for MacOS."
            dependsOn("packageReleaseDistributionForCurrentOS")

            into(rootProject.layout.buildDirectory)
            from(project.layout.buildDirectory.dir("compose/binaries/main-release/dmg"))
            rename { name ->
                if (!name.endsWith(".dmg")) {
                    return@rename name
                }

                val arch = if (isArm64) "arm64" else "x64"
                "${project.name}-${libs.versions.application.pkg.get()}-macos-${arch}.dmg"
            }
        }
    }

    if (isWindows) {
        register<Copy>("bundleWindowsExe") {
            group = "OpenIndex"
            description = "Create exe installer for Windows."
            dependsOn("packageReleaseDistributionForCurrentOS")

            into(rootProject.layout.buildDirectory)
            from(project.layout.buildDirectory.dir("compose/binaries/main-release/exe"))
            rename { name ->
                if (!name.endsWith(".exe")) {
                    return@rename name
                }

                val arch = if (isArm64) "arm64" else "x64"
                "${project.name}-${libs.versions.application.pkg.get()}-windows-${arch}.exe"
            }
        }
    }
}
