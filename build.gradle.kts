plugins {
    kotlin("jvm") version "1.8.10"
    id("com.gradle.plugin-publish") version "1.2.0"
}

group = "org.araqnid.kotlin-nodejs-application"
version = "0.0.4"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.github.node-gradle:gradle-node-plugin:5.0.0")

    testImplementation(kotlin("test-junit"))
    testImplementation(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.7.3"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
}

gradlePlugin {
    vcsUrl.set("https://github.com/araqnid/gradle-kotlin-nodejs-application")
    website.set("https://github.com/araqnid/gradle-kotlin-nodejs-application")

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
