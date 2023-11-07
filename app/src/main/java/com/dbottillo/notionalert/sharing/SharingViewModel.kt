package com.dbottillo.notionalert.sharing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dbottillo.notionalert.AddPageNotionBodyRequest
import com.dbottillo.notionalert.AddPageNotionBodyRequestParent
import com.dbottillo.notionalert.AddPageNotionProperty
import com.dbottillo.notionalert.AddPageNotionPropertyText
import com.dbottillo.notionalert.AddPageNotionPropertyTitle
import com.dbottillo.notionalert.ApiInterface
import com.dbottillo.notionalert.feature.home.GTD_ONE_DATABASE_ID
import com.dbottillo.notionalert.feature.home.POCKET_DATABASE_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharingViewModel @Inject constructor(
    private val api: ApiInterface,
) : ViewModel() {

    val events: Channel<Boolean> = Channel()

    fun saveArticle(url: String, title: String?) {
        callApi(POCKET_DATABASE_ID, url, title)
    }

    fun saveLifeOs(url: String, title: String?) {
        callApi(GTD_ONE_DATABASE_ID, url, title)
    }

    private fun callApi(databaseId: String, url: String, title: String?) {
        viewModelScope.launch {
            api.addPage(
                body = AddPageNotionBodyRequest(
                    parent = AddPageNotionBodyRequestParent(
                        type = "database_id",
                        databaseId = databaseId
                    ),
                    properties = mapOf(
                        "Name" to AddPageNotionProperty(
                            title = listOf(
                                AddPageNotionPropertyTitle(
                                    AddPageNotionPropertyText(content = title)
                                )
                            )
                        ),
                        "URL" to AddPageNotionProperty(
                            url = url
                        )
                    )
                )
            )
            events.send(true)
        }
    }
}
