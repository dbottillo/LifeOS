package com.dbottillo.lifeos

import com.dbottillo.lifeos.helpers.MainDispatcherExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.extension.RegisterExtension

@ExperimentalCoroutinesApi
abstract class BaseViewModelTest {
    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()
}
