package de.mcella.spring.learntool.common

import java.util.Optional

fun <T : Any> Optional<T>.toNullable(): T? = this.orElse(null)
