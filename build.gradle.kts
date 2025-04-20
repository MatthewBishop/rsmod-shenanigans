import java.util.zip.CRC32
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    kotlin("jvm") version "1.9.10"
    `maven-publish`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

group = "org.rsmod"

repositories {
    mavenCentral()
}

sourceSets {
    named("main") {
        withConvention(KotlinSourceSet::class) {
            kotlin.srcDirs(
                "upstream/api/combat-accuracy/src/main/kotlin",
                "upstream/api/combat-maxhit/src/main/kotlin",
                "upstream/engine/coroutine/src/main/kotlin",
                "upstream/engine/interact/src/main/kotlin",
                "upstream/engine/map/src/main/kotlin",
                "upstream/engine/routefinder/src/main/kotlin",
                "upstream/engine/utils-bits/src/main/kotlin"
            )
        }
        resources.srcDirs(
            "upstream/api/combat-accuracy/src/main/resources",
            "upstream/api/combat-maxhit/src/main/resources",
            "upstream/engine/map/src/main/resources",
            "upstream/engine/routefinder/src/main/resources"
        )
    }
}

val kotlinExt = extensions.getByType<KotlinJvmProjectExtension>()

tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(kotlinExt.sourceSets["main"].kotlin)
}

dependencies {
    implementation(kotlin("stdlib"))
}

tasks.register("computeSourceCrc") {
    group = "verification"
    description = "Computes a single CRC32 over all extra source files"
    doLast {
        val crc = CRC32()
        sourceSets["main"].allSource
            .filter { it.isFile }
            .sortedBy { it.absolutePath }
            .forEach { file ->
                val bytes = file.readBytes()
                crc.update(bytes)
            }
        val value = crc.value
        val outFile = layout.buildDirectory.file("source_crc.txt").get().asFile
        outFile.parentFile.mkdirs()
        outFile.writeText(value.toString())
        println("Source CRC: $value")
    }
}

publishing {
    publications {
        create<MavenPublication>("extra") {
            from(components["java"])
            artifact(tasks.named("sourcesJar").get())
            groupId    = project.group.toString()
            artifactId = "rsmod-extra"
            version    = project.version.toString()

            pom {
                name.set("RS Mod Modules")
                description.set("Combined modules from RS Mod")
                url.set("https://github.com/rsmod")
                inceptionYear.set("2022")

                organization {
                    name.set("RS Mod")
                    url.set("https://github.com/rsmod")
                }

                licenses {
                    license {
                        name.set("ISC License")
                        url.set("https://opensource.org/licenses/isc-license.txt")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/rsmod/rsmod.git")
                    developerConnection.set("scm:git:git@github.com:github.com/rsmod/rsmod.git")
                    url.set("https://github.com/rsmod/rsmod")
                }

                developers {
                    developer {
                        name.set("Tomm")
                        url.set("https://github.com/Tomm0017")
                    }
                }

                // Add properties for Java/Kotlin compatibility
                properties.set(mapOf(
                    "maven.compiler.source" to "1.8",
                    "maven.compiler.target" to "1.8",
                    "kotlin.compiler.jvmTarget" to "1.8"
                ))
            }
        }
    }
    repositories {
        maven {
            name = "localRepo"
            url  = uri("$projectDir/artifacts")
        }
    }
}