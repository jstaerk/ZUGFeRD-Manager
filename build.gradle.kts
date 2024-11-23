import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.serialization) apply false

    // Versions Plugin
    // https://github.com/ben-manes/gradle-versions-plugin
    alias(libs.plugins.versions) apply false

    // BuildInfo Plugin
    // https://codeberg.org/h34tnet/buildinfo
    alias(libs.plugins.buildinfo) apply false

    // Conveyor Plugin
    // https://www.hydraulic.dev/
    // https://conveyor.hydraulic.dev/latest/
    // https://conveyor.hydraulic.dev/latest/tutorial/hare/jvm/
    //alias(libs.plugins.conveyor) apply false
}

tasks {
    register<Delete>("clean") {
        delete(project.layout.buildDirectory)
    }

    register("printVersion") {
        doLast {
            println(project(":ZUGFeRD-Manager").version)
        }
    }
}

subprojects {
    // Configure DependencyUpdatesTask, that checks dependencies for version updates.
    // https://github.com/ben-manes/gradle-versions-plugin
    tasks.withType<DependencyUpdatesTask>().configureEach {
        checkForGradleUpdate = false

        // Helper method to decide, if a dependency version is considered as stable or unstable.
        @Suppress("UNUSED_PARAMETER")
        fun isNonStable(version: String, group: String, module: String): Boolean {
            // println("CHECK ${group}:${module}:$version")

            if (group == "org.jetbrains.kotlin" || group.startsWith("org.jetbrains.kotlin.")) {
                // versions like 1.6.20-M1 are considered unstable
                if (version.matches("^.*-M\\d*$".toRegex())) {
                    return true
                }

                // versions like 1.6.20-RC2 are considered unstable
                if (version.matches("^.*-RC\\d*$".toRegex())) {
                    return true
                }
            }

            val betaAllowedForGroup = listOf(
                "org.jetbrains.compose",
                "org.jetbrains.compose.desktop",
                "org.jetbrains.compose.material",
                "org.jetbrains.compose.material3",
            )

            val suffixes = mutableListOf("alpha", "eap")

            if (!betaAllowedForGroup.contains(group)) {
                suffixes.add("beta")
            }

            return suffixes.any {
                version.contains(it, true)
            }
        }

        rejectVersionIf {
            isNonStable(this.candidate.version, this.candidate.group, this.candidate.module)
                    && !isNonStable(this.currentVersion, this.candidate.group, this.candidate.module)
        }
    }
}
