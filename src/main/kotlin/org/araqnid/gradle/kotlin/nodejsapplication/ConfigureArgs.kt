package org.araqnid.gradle.kotlin.nodejsapplication

import org.gradle.api.provider.HasMultipleValues
import org.gradle.api.provider.Provider

internal inline fun <E : Any, P> HasMultipleValues<in E>.addFrom(
    provider: Provider<out P>,
    crossinline convert: MutableList<E>.(P) -> Unit
) {
    addAll(provider.map { value ->
        mutableListOf<E>().apply { convert(value) }
    })
}

internal inline fun <E : Any, P1, P2> HasMultipleValues<in E>.addFrom(
    provider1: Provider<out P1>,
    provider2: Provider<out P2>,
    crossinline convert: MutableList<E>.(P1, P2) -> Unit
) {
    addAll(provider1.zip(provider2) { value1, value2 ->
        mutableListOf<E>().apply { convert(value1, value2) }
    })
}
