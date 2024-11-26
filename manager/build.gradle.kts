@file:Suppress("SpellCheckingInspection")

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.org.apache.commons.lang3.SystemUtils
import java.nio.file.LinkOption
import java.util.Properties
import kotlin.io.path.isExecutable
import kotlin.io.path.isRegularFile
import kotlin.io.path.name

project.group = "de.openindex.zugferd"
project.version = libs.versions.application.version.get()

val isLinux = SystemUtils.IS_OS_LINUX
val isMac = SystemUtils.IS_OS_MAC
val isWindows = SystemUtils.IS_OS_WINDOWS

val isAmd64 = listOf("amd64", "x86_64")
    .contains(SystemUtils.OS_ARCH.lowercase())
val isArm64 = listOf("aarch64", "arm64")
    .contains(SystemUtils.OS_ARCH.lowercase())

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.isFile) {
        rootProject.file("local.properties").reader().use(::load)
    }
}

val appleBundleId: String = localProperties["APPLE_BUNDLE_ID"]?.toString()?.trim() ?: "${project.group}.manager"
val appleKeyChain: String = (localProperties["APPLE_KEYCHAIN"]?.toString()?.trim() ?: "").let {
    // Resolve relative keychain path to project root directory.
    if (it.startsWith("./")) {
        rootProject.layout.projectDirectory.asFile.resolve(it.substring(2)).absolutePath
    } else {
        it
    }
}

val appleSign: Boolean = localProperties["APPLE_SIGN"]?.toString()?.trim() == "1"
val appleSignKey: String = localProperties["APPLE_SIGN_KEY"]?.toString()?.trim() ?: ""

val appleNotarization: Boolean = localProperties["APPLE_NOTARIZATION"]?.toString()?.trim() == "1"
val appleNotarizationAppleId: String = localProperties["APPLE_NOTARIZATION_APPLE_ID"]?.toString()?.trim() ?: ""
val appleNotarizationTeamId: String = localProperties["APPLE_NOTARIZATION_TEAM_ID"]?.toString()?.trim() ?: ""
val appleNotarizationPassword: String = localProperties["APPLE_NOTARIZATION_PASSWORD"]?.toString()?.trim() ?: ""

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
}

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
        "CHROME_VERSION" to libs.versions.jcef.natives.get(),
    )
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
            implementation(libs.logback.classic)

            // Kotlinx-DateTime
            // https://logback.qos.ch/
            implementation(libs.kotlinx.datetime)

            // Mustang
            // https://www.mustangproject.org/
            // https://github.com/ZUGFeRD/mustangproject/
            implementation(libs.mustang.library)
            implementation(libs.mustang.validator)
            //implementation(variantOf(libs.mustang.validator) { classifier("shaded") })

            // SLF4J
            // https://www.slf4j.org/
            // https://www.slf4j.org/legacy.html#jul-to-slf4j
            implementation(libs.slf4j.jul)
        }
    }
}

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

            if (isLinux) {
                targetFormats(TargetFormat.Deb, TargetFormat.Rpm)
            }
            if (isMac) {
                targetFormats(TargetFormat.Dmg)
            }
            if (isWindows) {
                targetFormats(TargetFormat.Exe)
            }

            //
            // TODO: Further investigate module requirements.
            //

            includeAllModules = true
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
                bundleID = appleBundleId
                dockName = "ZUGFeRD-Manager"
                packageVersion = project.version.toString()
                packageBuildVersion = project.version.toString()
                appCategory = "public.app-category.business"

                // Currently not published to the App Store.
                appStore = false

                // Default entitlements are used for application and runtime.
                // https://github.com/JetBrains/compose-multiplatform/blob/master/gradle-plugins/compose/src/main/resources/default-entitlements.plist
                entitlementsFile.set(
                    rootProject.layout.projectDirectory
                        .dir("share").dir("apple").file("default.entitlements.plist")
                )
                runtimeEntitlementsFile.set(
                    rootProject.layout.projectDirectory
                        .dir("share").dir("apple").file("default.entitlements.plist")
                )

                //
                // We're using the default signing mechanism to provide a valid runtime.
                // As further native libraries are added to the application bundle,
                // the bundle needs to be signed again in a later step.
                //
                signing {
                    sign.set(appleSign)
                    identity.set(appleSignKey)
                    keychain.set(appleKeyChain)
                }

                //
                // Currently we're not using the default notarization mechanism.
                //
                //notarization {
                //    appleID.set(appleNotarizationAppleId)
                //    password.set(appleNotarizationPassword)
                //    teamID.set(appleNotarizationTeamId)
                //}

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
        // The jcef-natives dependency.
        // It contains the Chrome libraries,
        // that are extracted into the application bundle.
        val jcefNativesDependency = if (isArm64)
            libs.jcef.natives.macosx.arm64
        else
            libs.jcef.natives.macosx.amd64

        // Where the jcef-natives dependency is processed.
        val jcefNativesDir = project.layout.buildDirectory
            .dir("natives/jcef").get()

        // Where the jcef-natives dependency is copied to.
        val jcefNativesJarDir = jcefNativesDir.dir("jar")

        // Where the jcef-natives dependency is extracted to.
        val jcefNativesTempDir = jcefNativesDir.dir("temp")

        // Where the embedded jcef-natives tar.gz archive is extracted to.
        val jcefNativesLibraryDir = jcefNativesDir.dir("lib")

        // Name of the jcef-natives jar archive.
        val jcefNativesDependencyFileName =
            "${jcefNativesDependency.get().name}-${libs.versions.jcef.natives.get()}.jar"

        // Name of the jcef-natives tar.gz archive.
        val jcefNativesArchiveFileName =
            "${jcefNativesDependency.get().name}-${libs.versions.jcef.natives.get()}.tar.gz"

        /**
         * Test, if a file should be signed.
         */
        fun File.isSignableFile(): Boolean {
            val path = toPath()
            if (!path.isRegularFile(LinkOption.NOFOLLOW_LINKS)) {
                return false
            }
            if (path.isExecutable()) {
                return true
            }
            if (path.name.endsWith(".dylib")) {
                return true
            }
            if (path.name.endsWith(".jnilib")) {
                return true
            }
            return false
        }

        /**
         * Unsign a file or directory.
         */
        fun Project.macUnsign(fileToUnsign: File) {
            exec {
                workingDir = fileToUnsign.parentFile
                executable = "codesign"
                args = listOf(
                    "--remove-signature",
                    fileToUnsign.name,
                )
            }
        }

        /**
         * Sign a file or directory.
         */
        fun Project.macSign(
            fileToSign: File,
            entitlements: File? = null,
            unsignBefore: Boolean = true,
            runtime: Boolean = false,
        ) {
            //println("signing ${fileToSign.name}")
            logger.info("signing ${fileToSign.name}")

            if (unsignBefore) {
                macUnsign(fileToSign)
            }

            exec {
                workingDir = fileToSign.parentFile
                executable = "codesign"
                args = buildList {
                    add("--force")
                    add("--timestamp")
                    add("--sign")
                    add(appleSignKey)
                    add("--keychain")
                    add(appleKeyChain)
                    add("--identifier")
                    if (runtime) {
                        add("com.oracle.java.${appleBundleId}")
                    } else {
                        add(appleBundleId)
                    }
                    add("--options")
                    add("runtime")
                    if (entitlements != null) {
                        add("--entitlements")
                        add(entitlements.absolutePath)
                    }
                    add(fileToSign.name)
                }
            }
        }

        register<Copy>("bundleMacDmg") {
            group = "OpenIndex"
            description = "Create dmg installer for MacOS."

            // We currently don't use automatic notarization by the CMP Gradle plugin.
            //dependsOn("notarizeDmg")

            dependsOn("packageReleaseDistributionForCurrentOS")

            val arch = if (isArm64) "arm64" else "x64"
            val dmgTargetFileName = "${project.name}-${libs.versions.application.pkg.get()}-macos-${arch}.dmg"

            into(rootProject.layout.buildDirectory)
            from(project.layout.buildDirectory.dir("compose/binaries/main-release/dmg"))
            rename { name ->
                if (!name.endsWith(".dmg")) {
                    return@rename name
                }
                dmgTargetFileName
            }

            // Sign, notarize and staple the generated DMG file.
            doLast {
                // DMG is not automatically signed.
                // Therefore, we're doing this manually.
                if (appleSign) {
                    macSign(
                        fileToSign = rootProject.layout.buildDirectory
                            .file(dmgTargetFileName).get().asFile,
                        unsignBefore = false,
                    )
                }

                if (appleNotarization) {
                    // Notarize final DMG.
                    exec {
                        workingDir = rootProject.layout.buildDirectory.get().asFile
                        executable = "xcrun"
                        args = listOf(
                            "notarytool",
                            "submit",
                            //"--verbose",
                            "--apple-id", appleNotarizationAppleId,
                            "--team-id", appleNotarizationTeamId,
                            "--password", appleNotarizationPassword,
                            "--wait",
                            dmgTargetFileName,
                        )
                    }

                    // Staple notarized DMG.
                    exec {
                        workingDir = rootProject.layout.buildDirectory.get().asFile
                        executable = "xcrun"
                        args = listOf(
                            "stapler",
                            "staple",
                            dmgTargetFileName,
                        )
                    }
                }
            }
        }

        // Extract and sign native libraries before the Mac distributable is created.
        // And finally sign the modified application bundle again.
        whenTaskAdded {
            if (name == "createDistributable" || name == "createReleaseDistributable") {
                val releaseType = if (name == "createReleaseDistributable")
                    "main-release"
                else
                    "main"

                val appBundleDir = project.layout.buildDirectory
                    .dir("compose/binaries/${releaseType}/app/${project.name}.app")
                    .get()

                //val runtimeBundleDir = appBundleDir.dir("Contents/runtime")
                val chromeBundleDir = appBundleDir.dir("Contents/chrome")

                val defaultEntitlements = rootProject.layout.projectDirectory
                    .dir("share").dir("apple").file("default.entitlements.plist")

                // Extract native libraries.
                dependsOn("extractNativeLibrariesForMacOS")

                // Copy native libraries into the application bundle and sign the bundle again.
                // https://github.com/JetBrains/compose-multiplatform/blob/master/gradle-plugins/compose/src/main/kotlin/org/jetbrains/compose/desktop/application/internal/MacSigner.kt
                // https://github.com/JetBrains/compose-multiplatform/blob/master/gradle-plugins/compose/src/main/kotlin/org/jetbrains/compose/desktop/application/internal/MacSigningHelper.kt
                doLast {
                    copy {
                        from(jcefNativesLibraryDir)
                        into(chromeBundleDir)
                        eachFile {
                            val notExecutableExtensions = listOf(
                                "plist",
                                "json",
                                "pak",
                                "bin",
                                "dat",
                            )
                            if (notExecutableExtensions.contains(name.lowercase().substringAfterLast("."))) {
                                permissions {
                                    user { execute = false }
                                    group { execute = false }
                                    other { execute = false }
                                }
                            }
                        }
                    }

                    if (appleSign) {
                        chromeBundleDir.asFile.walkBottomUp()
                            .filter { it.isSignableFile() }
                            .forEach {
                                macSign(
                                    fileToSign = it,
                                    entitlements = defaultEntitlements.asFile,
                                )
                            }

                        chromeBundleDir.asFile.listFiles()
                            ?.filter { it.isDirectory }
                            ?.forEach {
                                macSign(
                                    fileToSign = it,
                                    entitlements = defaultEntitlements.asFile,
                                )
                            }

                        //
                        // Runtime is already properly signed.
                        //
                        //runtimeBundleDir.asFile.walkBottomUp()
                        //    .filter { it.isSignableFile() }
                        //    .forEach {
                        //        //macSign(fileToSign = it)
                        //        macSign(
                        //            fileToSign = it,
                        //            entitlements = runtimeEntitlements.asFile,
                        //            runtime = true,
                        //        )
                        //    }
                        //
                        //macSign(
                        //    fileToSign = runtimeBundleDir.asFile,
                        //    entitlements = runtimeEntitlements.asFile,
                        //    runtime = true,
                        //)

                        macSign(
                            fileToSign = appBundleDir.asFile,
                            entitlements = defaultEntitlements.asFile,
                        )
                    }
                }
            }
        }

        // Extract native libraries into macOS distributable.
        // Otherwise, these libraries are not propery signed.
        // see https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Signing_and_notarization_on_macOS/README.md#testflight
        register("extractNativeLibrariesForMacOS") {
            group = "OpenIndex"
            description = "Extracting jcef-natives library for MacOS bundling."

            doFirst {
                // Copy jcef-natives dependency to build directory.
                configurations.forEach {
                    if (!it.isCanBeResolved) {
                        return@forEach
                    }
                    copy {
                        from(it)
                        into(jcefNativesJarDir)
                        include(jcefNativesDependencyFileName)
                        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                    }
                }

                // Extract jcef-natives dependency to temp directory.
                copy {
                    from(zipTree(jcefNativesJarDir.file(jcefNativesDependencyFileName)))
                    into(jcefNativesTempDir)
                    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                }

                // Extract tar.gz from within jcef-natives dependency.
                copy {
                    from(tarTree(jcefNativesTempDir.file(jcefNativesArchiveFileName)))
                    into(jcefNativesLibraryDir)
                    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                }

                // Remove Apple quarantine attributes.
                // Not sure if necessary, but it doesn't hurt.
                exec {
                    workingDir = jcefNativesLibraryDir.asFile.parentFile
                    executable = "xattr"
                    args = listOf(
                        "-r",
                        "-d",
                        "com.apple.quarantine",
                        jcefNativesLibraryDir.asFile.name,
                    )
                }
            }

            // Remove jcef-native dependency from runtime classpath
            // to avoid bundling into the macOS application bundle.
            configurations.named("desktopRuntimeClasspath") {
                //println(this.name)

                if (isAmd64) {
                    exclude(
                        group = libs.jcef.natives.macosx.amd64.get().group,
                        module = libs.jcef.natives.macosx.amd64.get().name,
                    )
                }
                if (isArm64) {
                    exclude(
                        group = libs.jcef.natives.macosx.arm64.get().group,
                        module = libs.jcef.natives.macosx.arm64.get().name,
                    )
                }
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
