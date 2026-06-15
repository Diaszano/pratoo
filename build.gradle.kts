plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktlint)
}

ktlint {
    outputToConsole.set(true)
    ignoreFailures.set(false)

    filter {
        exclude("**/build/**")
        exclude("**/generated/**")
    }
}

tasks.register("qualityCheck") {
    group = "verification"
    description = "Runs Kotlin formatting and Android lint checks used by pre-commit."
    dependsOn("ktlintCheck", ":app:ktlintCheck", ":app:lintDebug")
}

tasks.register("qualityFormat") {
    group = "formatting"
    description = "Formats Kotlin sources and Gradle Kotlin scripts with ktlint."
    dependsOn("ktlintFormat", ":app:ktlintFormat")
}
