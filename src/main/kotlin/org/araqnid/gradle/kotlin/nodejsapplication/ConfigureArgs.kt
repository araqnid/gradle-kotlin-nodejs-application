package org.araqnid.gradle.kotlin.nodejsapplication

import org.gradle.api.provider.HasMultipleValues
import org.gradle.api.provider.Provider

internal inline fun <E : Any, P : Any> HasMultipleValues<in E>.addFrom(
    provider: Provider<out P>,
    crossinline convert: MutableList<E>.(P) -> Unit
) {
    addAll(provider.map { value ->
        mutableListOf<E>().apply { convert(value) }
    })
}

internal inline fun <E : Any, P1 : Any, P2 : Any> HasMultipleValues<in E>.addFrom(
    provider1: Provider<out P1>,
    provider2: Provider<out P2>,
    crossinline convert: MutableList<E>.(P1, P2) -> Unit
) {
    addAll(provider1.zip(provider2) { value1, value2 ->
        mutableListOf<E>().apply { convert(value1, value2) }
    })
}

internal inline fun <E : Any, P1 : Any, P2 : Any, P3 : Any> HasMultipleValues<in E>.addFrom(
    provider1: Provider<out P1>,
    provider2: Provider<out P2>,
    provider3: Provider<out P3>,
    crossinline convert: MutableList<E>.(P1, P2, P3) -> Unit
) {
    val provider12 = provider1.zip(provider2) { value1, value2 -> Pair(value1, value2) }
    addAll(provider12.zip(provider3) { (value1, value2), value3 ->
        mutableListOf<E>().apply { convert(value1, value2, value3) }
    })
}
