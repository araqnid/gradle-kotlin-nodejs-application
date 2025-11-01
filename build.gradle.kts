plugins {
    kotlin("jvm") version "2.2.21"
    id("com.gradle.plugin-publish") version "2.0.0"
    id("com.adarshr.test-logger") version "4.0.0"
}

group = "org.araqnid.kotlin-nodejs-application"
version = "0.1.0"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.github.node-gradle:gradle-node-plugin:7.1.0")

    testImplementation(kotlin("test-junit"))
    testImplementation(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.10.2"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
}

gradlePlugin {
    vcsUrl = "https://github.com/araqnid/gradle-kotlin-nodejs-application"
    website = "https://github.com/araqnid/gradle-kotlin-nodejs-application"

    plugins {
        create("nodejsApplicationPlugin") {
            id = "org.araqnid.kotlin-nodejs-application"
            displayName = "Kotlin/JS NodeJS application"
            description = "Bundle Kotlin/JS output and dependencies into a NodeJS application"
            implementationClass = "org.araqnid.gradle.kotlin.nodejsapplication.NodeJsApplicationPlugin"
            tags.add("nodejs")
            tags.add("kotlin")
            tags.add("kotlinjs")
        }
        create("githubActionPlugin") {
            id = "org.araqnid.kotlin-github-action"
            displayName = "Kotlin/JS GitHub Action"
            description = "Bundle Kotlin/JS output and dependencies into a GitHub Action"
            implementationClass = "org.araqnid.gradle.kotlin.nodejsapplication.PackageGithubActionPlugin"
            tags.add("nodejs")
            tags.add("github")
            tags.add("kotlin")
            tags.add("kotlinjs")
        }
    }
}
