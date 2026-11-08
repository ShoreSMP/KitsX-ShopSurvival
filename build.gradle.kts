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

// The version plus the commit the jar was built from, e.g. 1.0.3-a07b227, with -dirty when the build had
// uncommitted changes (untracked files don't count). Outside a git checkout the commit is "unknown".
// Only plugin.yml and the plugin jar use it; the published Maven version stays as above.
val pluginVersion: String = "$version-" + providers.exec {
    commandLine("git", "describe", "--always", "--dirty", "--abbrev=7", "--match=no-tag-matches-this")
    isIgnoreExitValue = true
}.standardOutput.asText.get().trim().ifEmpty { "unknown" }

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

// The plugin jar, e.g. KitsX-1.0.3-a07b227-all.jar
tasks.shadowJar {
    archiveVersion.set(pluginVersion)
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
    inputs.property("version", pluginVersion)
    filesMatching("plugin.yml") {
        expand(mapOf("version" to pluginVersion))
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
