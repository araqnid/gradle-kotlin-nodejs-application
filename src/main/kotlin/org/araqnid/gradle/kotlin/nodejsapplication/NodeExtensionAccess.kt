package org.araqnid.gradle.kotlin.nodejsapplication

import com.github.gradle.node.NodeExtension
import org.gradle.api.Project
import org.gradle.api.provider.Provider

val Project.nodeExtension: NodeExtension
    get() = extensions.getByType(NodeExtension::class.java)

val NodeExtension.versionIfDownloaded: Provider<String>
    get() = version.zip(download) { versionValue, downloadValue ->
        if (downloadValue) versionValue else ""
    }
