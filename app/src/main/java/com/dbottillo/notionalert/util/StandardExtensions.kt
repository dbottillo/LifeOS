package com.dbottillo.notionalert.util

inline infix fun <T> T?.guard(block: () -> Nothing): T {
    return this ?: block()
}

inline fun <T> T.letGuard(message: String, guard: (String) -> Unit, block: (T) -> Unit) {
    if (this == null) {
        guard(message)
    }
    return block(this)
}
