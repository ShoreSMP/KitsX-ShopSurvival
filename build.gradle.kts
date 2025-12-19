plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.1.2"
    `maven-publish`
}

group = "dev.darkxx"
version = "1.0.3"
description = "KitsX"
java.sourceCompatibility = JavaVersion.toVersion(23)
java.targetCompatibility = JavaVersion.toVersion(23)

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(23))
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
    implementation("org.projectlombok:lombok:1.18.30")

    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    compileOnly("com.github.SkriptLang:Skript:dev37c")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.9")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "dev.darkxx"
            artifactId = "KitsX"
            version = "1.0.2"
        }
    }
    repositories {
        maven {
            url = uri("file://${System.getProperty("user.home")}/.m2/repository")
        }
    }
}
