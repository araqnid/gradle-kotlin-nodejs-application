package org.araqnid.gradle.kotlin.nodejsapplication

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider

fun <E, P> ListProperty<in E>.addFrom(provider: Provider<out P>, convert: suspend SequenceScope<E>.(P) -> Unit) {
    addAll(provider.map { value ->
        val seq = sequence {
            convert(value)
        }
        seq.toList()
    })
}
