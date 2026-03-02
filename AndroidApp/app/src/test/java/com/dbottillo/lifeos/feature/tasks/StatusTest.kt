package com.dbottillo.lifeos.feature.tasks

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class StatusTest {

    @Test
    fun `toStatus maps correctly`() {
        assertThat("Inbox".toStatus()).isEqualTo(Status.Inbox)
        assertThat("Focus".toStatus()).isEqualTo(Status.Focus)
        assertThat("Backlog".toStatus()).isEqualTo(Status.Backlog)
        assertThat("Recurring".toStatus()).isEqualTo(Status.Recurring)
        assertThat("Next Week".toStatus()).isEqualTo(Status.NextWeek)
        assertThat("none".toStatus()).isEqualTo(Status.None)
        assertThat("Archive".toStatus()).isEqualTo(Status.Archive)
        assertThat("Done".toStatus()).isEqualTo(Status.Done)
        assertThat("something else".toStatus()).isEqualTo(Status.Unknown)
    }
}
