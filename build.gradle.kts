plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.1.2"
    `maven-publish`
}

group = "dev.darkxx"
version = "1.0.3"
description = "KitsX"
java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(24))
    }
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.skriptlang.org/releases")
    maven("https://maven.enginehub.org/repo/")

}

dependencies {
    implementation("org.projectlombok:lombok:1.18.38")
    implementation("me.notzorba:shore-scheduler:1.0.0-SNAPSHOT")

    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("com.github.SkriptLang:Skript:dev37c")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.9")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
    options.compilerArgs.add("-parameters")
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("plugin.yml") {
        expand(mapOf("version" to project.version))
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "KitsX"
        }
    }
}
