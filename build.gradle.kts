plugins {
    kotlin("jvm") version "2.1.10"
    id("java")
    id("com.gradleup.shadow") version "8.3.0"
}

group = "org.alpacaindustries"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("net.minestom:minestom-snapshots:1_21_4-6490538291")
    implementation("io.github.juliarn:npc-lib-minestom:3.0.0-beta11")
    implementation("com.google.code.gson:gson:2.12.1")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.5")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21)) // Minestom has a minimum Java version of 21
    }
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "org.alpacaindustries.MinestormServer" // Change this to your main class
        }
    }

    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        mergeServiceFiles()
        archiveClassifier.set("") // Prevent the -all suffix on the shadowjar file.
    }
}