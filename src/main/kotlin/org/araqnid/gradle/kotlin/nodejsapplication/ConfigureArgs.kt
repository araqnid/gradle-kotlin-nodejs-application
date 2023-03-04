package org.araqnid.gradle.kotlin.nodejsapplication

import org.gradle.api.provider.HasMultipleValues
import org.gradle.api.provider.Provider

internal fun <E, P> HasMultipleValues<in E>.addFrom(
    provider: Provider<out P>,
    convert: suspend SequenceScope<E>.(P) -> Unit
) {
    addAll(provider.map { value ->
        val seq = sequence {
            convert(value)
        }
        seq.toList()
    })
}

internal fun <E, P1, P2> HasMultipleValues<in E>.addFrom(
    provider1: Provider<out P1>,
    provider2: Provider<out P2>,
    convert: suspend SequenceScope<E>.(P1, P2) -> Unit
) {
    addAll(provider1.zip(provider2) { value1, value2 ->
        val seq = sequence {
            convert(value1, value2)
        }
        seq.toList()
    })
}
