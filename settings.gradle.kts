pluginManagement {
    val kotlinVersion: String by settings
    val axionVersion: String by settings
    val coverallsVersion: String by settings
    plugins {
        kotlin("jvm") version kotlinVersion
        id("pl.allegro.tech.build.axion-release") version axionVersion
        id("com.github.kt3k.coveralls") version coverallsVersion
    }
}

rootProject.name = "pretty-string"
