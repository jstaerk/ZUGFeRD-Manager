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
}

tasks {
    register<Delete>("clean") {
        delete(project.layout.buildDirectory)
    }
}

subprojects {
    //
    // Configure DependencyUpdatesTask for all subprojects,
    // that checks dependencies for version updates.
    //
    // see https://github.com/ben-manes/gradle-versions-plugin
    //
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

            val suffixes = mutableListOf("alpha", "eap")

            val betaAllowedForGroup = group.startsWith("org.jetbrains.compose")
            if (!betaAllowedForGroup) {
                suffixes.add("beta")
            }

            val rcAllowedForGroup = group.startsWith("org.jetbrains.compose")
            if (!rcAllowedForGroup) {
                suffixes.add("rc")
            }

            return suffixes.any {
                version.contains(it, true)
            }
        }

        rejectVersionIf {
            //
            // Handle version numbers for jcef-natives-* dependencies separately.
            //
            // For example extract 127.3.1 from version strings like
            // jcef-99c2f7a+cef-127.3.1+g6cbb30e+chromium-127.0.6533.100
            // and comparing these against each other.
            //
            if (candidate.module.startsWith("jcef-natives-")) {
                val currentVersion = libs.versions.jcef.natives.get().split("+")[1].split("-")[1]
                val candidateVersion = this.candidate.version.split("+")[1].split("-")[1]
                //println("${this.candidate.module} => $currentVersion == $candidateVersion")

                val currentVersionNumbers = currentVersion.split(".").map { it.toInt() }
                val candidateVersionNumbers = candidateVersion.split(".").map { it.toInt() }

                var candidateIsNewer: Boolean? = null
                currentVersionNumbers.forEachIndexed { index, currentNumber ->
                    if (candidateIsNewer != null) {
                        return@forEachIndexed
                    }

                    val candidateNumber = candidateVersionNumbers.getOrNull(index) ?: 0
                    candidateIsNewer = candidateNumber > currentNumber
                }
                //println("${this.candidate.module} => $currentVersion == $candidateVersion => $candidateIsNewer")

                // Returning true to reject a candidate version.
                return@rejectVersionIf !(candidateIsNewer ?: false)
            }


            isNonStable(this.candidate.version, this.candidate.group, this.candidate.module)
                    && !isNonStable(this.currentVersion, this.candidate.group, this.candidate.module)
        }
    }
}
