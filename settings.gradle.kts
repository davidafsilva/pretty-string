pluginManagement {
    val kotlinVersion: String by settings
    val axionVersion: String by settings
    plugins {
        kotlin("jvm") version kotlinVersion
        id("pl.allegro.tech.build.axion-release") version axionVersion
    }
}

rootProject.name = "pretty-string"
