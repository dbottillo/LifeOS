package com.dbottillo.lifeos.feature.widgets

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.dbottillo.lifeos.R
import com.dbottillo.lifeos.feature.tasks.Ongoing
import com.dbottillo.lifeos.feature.tasks.Idea
import com.dbottillo.lifeos.feature.tasks.NextAction
import com.dbottillo.lifeos.feature.tasks.TasksRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.lang.UnsupportedOperationException
import java.time.LocalDate
import java.time.ZoneId

@Suppress("UNUSED_PARAMETER")
class NextActionsRemoteViewsFactory(
    private val context: Context,
    intent: Intent,
    private val tasksRepository: TasksRepository
) : RemoteViewsService.RemoteViewsFactory {

    private val data = mutableMapOf<Int, WidgetEntry>()

    /*
    0 -> task
    1 -> task
    2 -> title
    3 -> task
    4 -> task
    5 -> add

    0 -> title
    1 -> task
    2 -> task
    3 -> task
    5 -> add
     */

    override fun onCreate() {
        initData()
    }

    override fun onDataSetChanged() {
        initData()
    }

    @Suppress("EmptyFunctionBlock")
    override fun onDestroy() {
    }

    override fun getCount() = data.size

    @Suppress("LongMethod", "CyclomaticComplexMethod")
    override fun getViewAt(position: Int): RemoteViews {
        return when (val entry = data[position]) {
            WidgetEntry.Focus -> RemoteViews(
                context.packageName,
                R.layout.notion_widget_focus
            )

            WidgetEntry.Ideas -> RemoteViews(
                context.packageName,
                R.layout.notion_widget_ideas
            )

            WidgetEntry.Ongoing -> RemoteViews(
                context.packageName,
                R.layout.notion_widget_ongoing
            )

            WidgetEntry.Footer -> {
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
                buttonView.setOnClickFillInIntent(
                    R.id.notion_widget_add_image_button,
                    refreshIntent
                )
                buttonView
            }

            is WidgetEntry.Entry -> {
                val view = RemoteViews(
                    context.packageName,
                    R.layout.widget_row
                )
                view.setTextViewText(R.id.widget_row_title_id, entry.text)
                view.setTextViewText(R.id.widget_row_due_id, entry.due ?: "")
                view.setTextViewText(R.id.widget_row_parent_id, entry.parent ?: "")
                view.setViewVisibility(
                    R.id.widget_row_due_id,
                    if (entry.due?.isNotEmpty() == true) View.VISIBLE else View.GONE
                )
                view.setViewVisibility(
                    R.id.widget_row_parent_id,
                    if (entry.parent?.isNotEmpty() == true) View.VISIBLE else View.GONE
                )
                view.setInt(R.id.widget_row_id, "setBackgroundResource", entry.color)
                val fillInIntent = Intent().apply {
                    Bundle().also { extras ->
                        extras.putString(LINK_URL, entry.url)
                        extras.putInt(EXTRA_ITEM, position)
                        putExtras(extras)
                    }
                }
                // Make it possible to distinguish the individual on-click action of a given item.
                view.setOnClickFillInIntent(R.id.widget_row_id, fillInIntent)
                view
            }

            else -> throw UnsupportedOperationException("illegal entry")
        }
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount() = 5

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds() = true

    private fun initData() {
        data.clear()
        runBlocking {
            val nextActions = tasksRepository.nextActionsFlow.first()
            val today = LocalDate.now()
            val (ongoingInbox, ongoing) = tasksRepository.ongoingFlow.first().partition { ongoing ->
                if (ongoing.due != null) {
                    val date = ongoing.due.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                    date == today || date.isBefore(today)
                } else {
                    false
                }
            }
            val ideas = tasksRepository.ideasFlow.first().take(10)
            val (inbox, others) = nextActions.partition { it.isInbox }
            val (withDue, withoutDue) = others.partition { it.due != null }
            var index = 0
            inbox.forEach { action ->
                data[index] = action.toWidgetEntry()
                index++
            }
            ongoingInbox.forEach { entry ->
                data[index] = entry.toWidgetEntry()
                index++
            }
            withDue.forEach { action ->
                data[index] = action.toWidgetEntry()
                index++
            }
            data[index] = WidgetEntry.Focus
            index++
            withoutDue.forEach { action ->
                data[index] = action.toWidgetEntry()
                index++
            }
            data[index] = WidgetEntry.Footer
            index++
            if (ongoing.isNotEmpty()) {
                data[index] = WidgetEntry.Ongoing
                index++
                ongoing.forEach { entry ->
                    data[index] = entry.toWidgetEntry()
                    index++
                }
            }
            data[index] = WidgetEntry.Ideas
            index++
            ideas.forEach { idea ->
                data[index] = idea.toWidgetEntry()
                index++
            }
        }
    }
}

private fun NextAction.toWidgetEntry(): WidgetEntry {
    return WidgetEntry.Entry(
        text = text,
        url = url,
        due = dueFormatted,
        color = color.split(",").first().toDrawable(),
        parent = parent?.title
    )
}

private fun Ongoing.toWidgetEntry(): WidgetEntry {
    return WidgetEntry.Entry(
        text = text,
        url = url,
        due = dueFormatted,
        color = "red".toDrawable(),
        parent = parent?.title
    )
}

private fun Idea.toWidgetEntry(): WidgetEntry {
    return WidgetEntry.Entry(
        text = text,
        url = url,
        due = null,
        color = "orange".toDrawable(),
        parent = parent?.title
    )
}

sealed class WidgetEntry {
    data object Focus : WidgetEntry()
    data object Footer : WidgetEntry()
    data object Ideas : WidgetEntry()
    data object Ongoing : WidgetEntry()
    data class Entry(
        val text: String,
        val color: Int,
        val url: String,
        val due: String?,
        val parent: String?
    ) : WidgetEntry()
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
