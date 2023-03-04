package org.araqnid.gradle.kotlin.nodejsapplication

import org.gradle.api.Project

internal val Project.jsBuildOutput
    get() = rootProject.layout.buildDirectory.dir("js")
