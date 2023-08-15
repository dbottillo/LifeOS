package com.dbottillo.notionalert

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.dbottillo.notionalert.feature.home.HomeStorage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@Suppress("UNUSED_PARAMETER")
class NotionRemoteViewsFactory(
    private val context: Context,
    intent: Intent,
    private val homeStorage: HomeStorage
) : RemoteViewsService.RemoteViewsFactory {

    private var dataList = mutableListOf<Pair<String, Int>>()
    private var urls = mutableMapOf<Int, String>()

    override fun onCreate() {
        initData()
    }

    override fun onDataSetChanged() {
        initData()
    }

    @Suppress("EmptyFunctionBlock")
    override fun onDestroy() {
    }

    override fun getCount() = dataList.size + 1

    override fun getViewAt(position: Int): RemoteViews {
        if (position == dataList.size) {
            // last entry
            val buttonView = RemoteViews(
                context.packageName,
                R.layout.notion_widget_add
            )
            val refreshIntent = Intent().apply {
                Bundle().also { extras ->
                    extras.putString(LINK_URL, "refresh")
                    putExtras(extras)
                }
            }
            buttonView.setOnClickFillInIntent(R.id.notion_widget_add_image_button, refreshIntent)
            return buttonView
        }
        val view = RemoteViews(
            context.packageName,
            R.layout.widget_row
        )
        view.setTextViewText(R.id.widget_row_id, dataList[position].first)
        view.setInt(R.id.widget_row_id, "setBackgroundResource", dataList[position].second)
        val fillInIntent = Intent().apply {
            Bundle().also { extras ->
                extras.putString(LINK_URL, urls.getOrDefault(position, ""))
                extras.putInt(EXTRA_ITEM, position)
                putExtras(extras)
            }
        }
        // Make it possible to distinguish the individual on-click action of a given item.
        view.setOnClickFillInIntent(R.id.widget_row_id, fillInIntent)
        return view
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount() = 2

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds() = true

    private fun initData() {
        dataList.clear()
        runBlocking {
            homeStorage.nextActionsFlow.first().actions.forEachIndexed { index, entry ->
                dataList.add(entry.text to entry.color.split(",").first().toDrawable())
                urls[index] = entry.url
            }
        }
    }
}

private fun String.toDrawable(): Int {
    return when (this) {
        "gray" -> R.drawable.widget_row_background_gray
        "orange" -> R.drawable.widget_row_background_orange
        "green" -> R.drawable.widget_row_background_green
        "blue" -> R.drawable.widget_row_background_blue
        "red" -> R.drawable.widget_row_background_red
        "purple" -> R.drawable.widget_row_background_purple
        "pink" -> R.drawable.widget_row_background_pink
        "yellow" -> R.drawable.widget_row_background_yellow
        else -> R.drawable.widget_row_background
    }
}
