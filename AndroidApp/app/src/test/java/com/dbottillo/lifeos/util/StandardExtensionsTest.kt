package com.dbottillo.lifeos.util

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class StandardExtensionsTest {

    @Test
    fun `guard returns value if not null`() {
        val value: String? = "hello"
        val result = value guard { throw IllegalStateException() }
        assertThat(result).isEqualTo("hello")
    }

    @Test
    fun `guard executes block if null`() {
        val value: String? = null
        assertThrows(IllegalStateException::class.java) {
            value guard { throw IllegalStateException() }
        }
    }

    @Test
    fun `letGuard executes block if not null`() {
        val value: String? = "hello"
        var result: String? = null
        value.letGuard("error", { }) {
            result = it
        }
        assertThat(result).isEqualTo("hello")
    }

    @Test
    fun `letGuard executes guard if null`() {
        val value: String? = null
        var guardCalled = false
        value.letGuard("error", { guardCalled = true }) {
            // should not be called
        }
        assertThat(guardCalled).isTrue()
    }
}
