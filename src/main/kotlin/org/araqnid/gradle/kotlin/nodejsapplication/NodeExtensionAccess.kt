package org.araqnid.gradle.kotlin.nodejsapplication

import com.github.gradle.node.NodeExtension
import org.gradle.api.Project

val Project.nodeExtension: NodeExtension
    get() = extensions.getByType(NodeExtension::class.java)
