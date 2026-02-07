plugins {
    alias(libs.plugins.android).apply(false)
    alias(libs.plugins.ksp).apply(false)
    alias(libs.plugins.parcelize).apply(false)
    alias(libs.plugins.library).apply(false)
    alias(libs.plugins.kotlinSerialization).apply(false)
    alias(libs.plugins.detekt).apply(false)
}

tasks.register<Delete>("clean") {
    delete {
        layout.buildDirectory.asFile
    }
}
