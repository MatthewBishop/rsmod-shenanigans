plugins {
    kotlin("jvm") version "1.9.10"
    `maven-publish`
}

group = "com.rsmod"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    // add other RSMod dependencies if needed (e.g., engine-core)
}

sourceSets {
    create("extra") {
        // Include Kotlin sources from upstream RSMod modules
        withConvention(org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet::class) {
            kotlin.srcDirs(
                "upstream/engine/map/src/main/kotlin",
                "upstream/engine/routefinder/src/main/kotlin"
            )
        }
        // Include any resources if present
        resources.srcDirs(
            "upstream/engine/map/src/main/resources",
            "upstream/engine/routefinder/src/main/resources"
        )
    }
}

tasks.register<Jar>("extraJar") {
    archiveClassifier.set("extra")
    from(sourceSets["extra"].output)
}

publishing {
    publications {
        create<MavenPublication>("extra") {
            artifact(tasks.named("extraJar").get())
            groupId    = project.group.toString()
            artifactId = "rsmod-extra"
            version    = project.version.toString()
        }
    }
    repositories {
        maven {
            name = "localRepo"
            url  = uri("$projectDir/local-maven")
        }
    }
}