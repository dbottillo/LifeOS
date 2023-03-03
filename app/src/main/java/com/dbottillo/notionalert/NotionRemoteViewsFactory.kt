package com.dbottillo.notionalert

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.dbottillo.notionalert.feature.home.HomeStorage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class NotionRemoteViewsFactory(
    private val context: Context,
    intent: Intent,
    private val homeStorage: HomeStorage
) : RemoteViewsService.RemoteViewsFactory {

    private var dataList = mutableListOf<Pair<String, Int>>()

    override fun onCreate() {
        initData()
    }

    override fun onDataSetChanged() {
        initData()
    }

    @Suppress("EmptyFunctionBlock")
    override fun onDestroy() {
    }

    override fun getCount() = dataList.size

    override fun getViewAt(position: Int): RemoteViews {
        val view = RemoteViews(
            context.packageName,
            R.layout.widget_row
        )
        view.setTextViewText(R.id.widget_row_id, dataList[position].first)
        view.setInt(R.id.widget_row_id, "setBackgroundResource", dataList[position].second)
        /*val pendingIntent: PendingIntent = PendingIntent.getActivity(
            *//* context = *//* context,
            *//* requestCode = *//* 0,
            *//* intent = *//* Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.notion.so/TIH-Array-6aaf79f7251c4244849bd559256608e7")
            ),
            *//* flags = *//* PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        view.setOnClickPendingIntent(R.id.widget_row_id, pendingIntent)*/
        val fillInIntent = Intent().apply {
            Bundle().also { extras ->
                extras.putString(LINK_URL, "https://www.notion.so/TIH-Array-6aaf79f7251c4244849bd559256608e7")
                extras.putInt(EXTRA_ITEM, position)
                putExtras(extras)
            }
        }
        // Make it possible to distinguish the individual on-click
        // action of a given item.
        view.setOnClickFillInIntent(R.id.widget_row_id, fillInIntent)
        return view
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount() = 1

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds() = true

    private fun initData() {
        dataList.clear()
        runBlocking {
            homeStorage.nextActionsFlow.first().actionsList.forEach { entry ->
                dataList.add(entry.text to entry.color.split(",").first().toDrawable())
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
