import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.21"
    `maven-publish`
}

group = "com.asdacap"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.3.0-rc1")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.21")
    implementation("org.apache.commons:commons-csv:1.8")
    implementation("com.fasterxml.jackson.core:jackson-core:2.12.1")
    implementation("io.github.cdimascio:java-dotenv:5.2.2")

    runtimeOnly("org.jetbrains.kotlin:kotlin-main-kts:1.4.21")
    runtimeOnly("org.jetbrains.kotlin:kotlin-scripting-jsr223:1.4.21")

    testRuntimeOnly("org.jetbrains.kotlin:kotlin-main-kts:1.4.21")
    testRuntimeOnly("org.jetbrains.kotlin:kotlin-scripting-jsr223:1.4.21")

    testImplementation("org.junit.jupiter:junit-jupiter:5.4.2")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.ExperimentalStdlibApi"
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/asdacap/ea4k")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            this.from(components["java"])
        }
    }
}