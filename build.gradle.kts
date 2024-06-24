plugins {
    id("java")
    kotlin("jvm") version "1.9.23"
    id("org.jetbrains.intellij") version "1.10.1"
}

group = "com.github.nizienko"
version = "0.0.7"

repositories {
    mavenCentral()
}
dependencies {
    implementation("org.assertj:assertj-swing-junit:3.17.1")
}


kotlin {
    jvmToolchain(17)
}

intellij {
    version.set("2024.1")
    type.set("IC")
}

tasks {
    patchPluginXml {
        sinceBuild.set("233.1")
        untilBuild.set("242.*")
    }
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
}