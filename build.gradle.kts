plugins {
    kotlin("jvm") version "1.8.10"
    id("com.gradle.plugin-publish") version "1.1.0"
}

group = "org.araqnid.kotlin-nodejs-application"
version = "0.0.1"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.github.node-gradle:gradle-node-plugin:3.5.1")
}

gradlePlugin {
    vcsUrl.set("https://github.com/araqnid/gradle-kotlin-nodejs-application")
    website.set("https://github.com/araqnid/gradle-kotlin-nodejs-application")

    plugins {
        create("nextjsSitePlugin") {
            id = "org.araqnid.kotlin-nodejs-application"
            displayName = "Kotlin/JS NodeJS application"
            description = "Bundle Kotlin/JS output and dependencies into a NodeJS application"
            implementationClass = "org.araqnid.gradle.kotlin.nodejsapplication.NodeJsApplicationPlugin"
            tags.add("nodejs")
            tags.add("kotlin")
            tags.add("kotlinjs")
        }
    }
}
