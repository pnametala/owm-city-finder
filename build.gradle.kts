import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

plugins {
    kotlin("jvm") version "1.3.61"
    id("com.jfrog.bintray") version "1.8.3"
    `maven-publish`
    id("org.jetbrains.dokka") version "0.9.17"
}

defaultTasks("clean", "build")

allprojects {
    group = "com.gitlab.mvysny.owmcityfinder"
    version = "0.3-SNAPSHOT"
    repositories {
        jcenter()
    }
}

subprojects {
    apply {
        plugin("maven-publish")
        plugin("kotlin")
        plugin("com.jfrog.bintray")
        plugin("org.jetbrains.dokka")
    }

    // creates a reusable function which configures proper deployment to Bintray
    ext["configureBintray"] = { artifactId: String ->

        val local = Properties()
        val localProperties: File = rootProject.file("local.properties")
        if (localProperties.exists()) {
            localProperties.inputStream().use { local.load(it) }
        }

        val sourceJar = task("sourceJar", Jar::class) {
            dependsOn(tasks["classes"])
            classifier = "sources"
            from(sourceSets.main.get().allSource)
        }

        val javadocJar = task("javadocJar", Jar::class) {
            val javadoc = tasks["dokka"] as DokkaTask
            javadoc.outputFormat = "javadoc"
            javadoc.outputDirectory = "$buildDir/javadoc"
            dependsOn(javadoc)
            classifier = "javadoc"
            from(javadoc.outputDirectory)
        }

        publishing {
            publications {
                create("mavenJava", MavenPublication::class.java).apply {
                    groupId = project.group.toString()
                    this.artifactId = artifactId
                    version = project.version.toString()
                    pom {
                        description.set("OpenWeatherMap City Finder and Resolver")
                        name.set(artifactId)
                        url.set("https://gitlab.com/mvysny/owm-city-finder")
                        licenses {
                            license {
                                name.set("The Apache Software License, Version 2.0")
                                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                                distribution.set("repo")
                            }
                        }
                        developers {
                            developer {
                                id.set("mavi")
                                name.set("Martin Vysny")
                                email.set("martin@vysny.me")
                            }
                        }
                        scm {
                            url.set("https://gitlab.com/mvysny/owm-city-finder")
                        }
                    }

                    from(components["java"])
                    artifact(sourceJar)
                    artifact(javadocJar)
                }
            }
        }

        bintray {
            user = local.getProperty("bintray.user")
            key = local.getProperty("bintray.key")
            pkg(closureOf<BintrayExtension.PackageConfig> {
                repo = "gitlab"
                name = "com.gitlab.mvysny.owmcityfinder"
                setLicenses("Apache-2.0")
                vcsUrl = "https://gitlab.com/mvysny/owm-city-finder"
                publish = true
                setPublications("mavenJava")
                version(closureOf<BintrayExtension.VersionConfig> {
                    this.name = project.version.toString()
                    released = Date().toString()
                })
            })
        }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            // to see the exceptions of failed tests in Travis-CI console.
            exceptionFormat = TestExceptionFormat.FULL
        }
    }
}

// to release:
// $ ./gradlew clean build upload
val upload by tasks.creating {
    dependsOn("clean", "build", "bintrayUpload", "owm-city-finder-server:jib")
}

