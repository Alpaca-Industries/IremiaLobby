plugins {
    kotlin("jvm") version "2.1.10"
}

group = "org.alpacaindustries"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("net.minestom:minestom-snapshots:39d445482f")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}