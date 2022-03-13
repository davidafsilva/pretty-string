import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import pl.allegro.tech.build.axion.release.domain.ChecksConfig
import pl.allegro.tech.build.axion.release.domain.RepositoryConfig
import pl.allegro.tech.build.axion.release.domain.TagNameSerializationConfig

// register repositories for both buildscript and application
buildscript.repositories.registerRepositories()
repositories.registerRepositories()

plugins {
    kotlin("jvm")
    id("pl.allegro.tech.build.axion-release")
    `maven-publish`
    signing
}

group = "pt.davidafsilva.jvm.kotlin"
scmVersion {
    tag(closureOf<TagNameSerializationConfig> {
        prefix = "v"
        versionSeparator = ""
    })
    checks(closureOf<ChecksConfig> {
        isUncommittedChanges = false
    })
    repository(closureOf<RepositoryConfig> {
        pushTagsOnly = true
    })
}
version = scmVersion.version

dependencies {
    api(kotlin("stdlib"))
    api(kotlin("reflect"))

    val kotestVersion: String by project
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
}

val javaVersion = JavaVersion.VERSION_1_8
configure<JavaPluginExtension> {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    withSourcesJar()
    withJavadocJar()
}

configure<PublishingExtension> {
    repositories {
        maven {
            name = "Sonatype"
            val repository = when {
                version.toString().endsWith("-SNAPSHOT") -> "/content/repositories/snapshots/"
                else -> "/service/local/staging/deploy/maven2/"
            }
            setUrl("https://s01.oss.sonatype.org/$repository")
            credentials {
                username = System.getenv("OSSRH_USER")
                password = System.getenv("OSSRH_TOKEN")
            }
        }
    }

    publications {
        create<MavenPublication>("artifacts") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            pom {
                val githubRepoUrl = "https://github.com/davidafsilva/pretty-string"

                name.set(project.name)
                description.set("https://github.com/davidafsilva/pretty-string")
                url.set("https://github.com/davidafsilva/pretty-string")
                inceptionYear.set("2022")
                licenses {
                    license {
                        name.set("BSD 3-Clause")
                        url.set("https://opensource.org/licenses/BSD-3-Clause")
                    }
                }
                developers {
                    developer {
                        id.set("davidafsilva")
                        name.set("David Silva")
                        url.set("https://github.com/davidafsilva")
                    }
                }
                scm {
                    val githubRepoCheckoutUrl = "$githubRepoUrl.git"

                    connection.set(githubRepoCheckoutUrl)
                    developerConnection.set(githubRepoCheckoutUrl)
                    url.set(githubRepoUrl)
                }
            }
        }
    }
}

configure<SigningExtension> {
    val signingGpgKey: String? by project
    val signingGpgKeyId: String? by project
    val signingGpgKeyPassword: String? by project
    if (signingGpgKey != null && signingGpgKeyId != null && signingGpgKeyPassword != null) {
        useInMemoryPgpKeys(signingGpgKeyId, signingGpgKey, signingGpgKeyPassword)
    }

    sign(publishing.publications.getByName("artifacts"))
}

tasks {
    withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = javaVersion.toString()
        }
    }

    withType<Test> {
        useJUnitPlatform()
    }

    withType<Jar> {
        from("${projectDir}/LICENSE") {
            rename("LICENSE", "META-INF/LICENSE.txt")
        }
    }
}

fun RepositoryHandler.registerRepositories() {
    mavenLocal()
    mavenCentral()
}
