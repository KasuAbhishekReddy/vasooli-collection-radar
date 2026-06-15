plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}

// Build outputs go OUTSIDE the OneDrive-synced project folder to avoid
// Windows file-lock / AccessDenied errors during resource merging.
val externalBuildRoot = File(System.getProperty("user.home"), "VasooliBuild")
subprojects {
    layout.buildDirectory.set(File(externalBuildRoot, name))
}
