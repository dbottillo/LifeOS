package com.dbottillo.lifeos.feature.widgets

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.dbottillo.lifeos.R
import com.dbottillo.lifeos.feature.home.EntryContent
import com.dbottillo.lifeos.feature.home.mapNextWeek
import com.dbottillo.lifeos.feature.home.mapFocus
import com.dbottillo.lifeos.feature.home.mapInbox
import com.dbottillo.lifeos.feature.tasks.NextWeek
import com.dbottillo.lifeos.feature.tasks.Focus
import com.dbottillo.lifeos.feature.tasks.Inbox
import com.dbottillo.lifeos.feature.tasks.TasksRepository
import com.dbottillo.lifeos.network.RefreshWorker
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import androidx.core.net.toUri

class OverviewWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = OverviewWidget()
}

class OverviewWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface TaskRepositoryProviderEntryPoint {
        fun tasksRepository(): TasksRepository
    }

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appContext = context.applicationContext ?: throw IllegalStateException()
        val statisticsEntryPoint =
            EntryPointAccessors.fromApplication(
                appContext,
                TaskRepositoryProviderEntryPoint::class.java,
            )
        val tasksRepository = statisticsEntryPoint.tasksRepository()
        provideContent {
            val inbox = tasksRepository.inboxFlow.collectAsState(emptyList())
            val focus = tasksRepository.focusFlow.collectAsState(emptyList())
            val nextWeek = tasksRepository.nextWeekFlow.collectAsState(emptyList())
            GlanceTheme(colors = LifeOSAppWidgetGlanceColorScheme.colors) {
                Box(
                    modifier = GlanceModifier
                    .padding(8.dp)
                    .background(Color.Black)
                ) {
                    OverviewAppWidgetContent(
                        inbox = inbox.value,
                        focus = focus.value,
                        nextWeek = nextWeek.value
                    )
                }
            }
        }
    }

    @Composable
    fun OverviewAppWidgetContent(
        inbox: List<Inbox>,
        focus: List<Focus>,
        nextWeek: List<NextWeek>
    ) {
        LazyColumn(
            modifier = GlanceModifier
        ) {
            if (inbox.isNotEmpty()) {
                item {
                    Header("Inbox")
                }
                inbox.mapInbox().forEach { entry ->
                    item {
                        EntryWidget(content = entry)
                    }
                }
                item {
                    Spacer(GlanceModifier.fillMaxWidth().height(16.dp))
                }
            }
            if (focus.isNotEmpty()) {
                item {
                    Header("Focus")
                }
                focus.mapFocus().forEach { entry ->
                    item {
                        EntryWidget(content = entry)
                    }
                }
                item {
                    Spacer(GlanceModifier.fillMaxWidth().height(16.dp))
                }
            }
            item {
                Box(
                    modifier = GlanceModifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = actionRunCallback<RefreshClickAction>()
                    )
                    .height(40.dp)
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.baseline_refresh_24),
                        contentDescription = "refresh"
                    )
                }
            }
            if (nextWeek.isNotEmpty()) {
                item {
                    Header("Next week")
                }
                nextWeek.mapNextWeek().forEach { entry ->
                    item {
                        EntryWidget(content = entry)
                    }
                }
                item {
                    Spacer(GlanceModifier.fillMaxWidth().height(16.dp))
                }
            }
        }
    }
}

@Composable
fun Header(text: String) {
    Text(
        modifier = GlanceModifier.fillMaxWidth(),
        text = text,
        style = TextStyle(
            color = GlanceTheme.colors.tertiary,
            fontSize = 10.sp,
        ),
    )
}

@Composable
fun EntryWidget(
    content: EntryContent,
) {
    val textColor = GlanceTheme.colors.onSurface
    Column {
        Spacer(GlanceModifier.fillMaxWidth().height(2.dp))
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = content.url.toUri()
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(content.color)
                .cornerRadius(4.dp)
                .clickable(
                    onClick = actionStartActivity(intent)
                ),
        ) {
            Column(modifier = GlanceModifier.padding(all = 4.dp)) {
                Text(
                    text = content.title,
                    style = TextStyle(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = textColor
                    ),
                )
                if (content.subtitle?.isNotEmpty() == true) {
                    Text(
                        modifier = GlanceModifier.padding(top = 2.dp),
                        text = content.subtitle,
                        style = TextStyle(
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = textColor
                        ),
                    )
                }
                if (content.link?.isNotEmpty() == true) {
                    val intentLink = Intent(Intent.ACTION_VIEW)
                    intentLink.data = content.link.toUri()
                    Text(
                        modifier = GlanceModifier
                            .padding(top = 2.dp)
                            .clickable(
                                onClick = actionStartActivity(intentLink)
                            ),
                        text = content.link,
                        style = TextStyle(
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = textColor
                        ),
                        maxLines = 2,
                    )
                }
                if (content.parent?.isNotEmpty() == true) {
                    Text(
                        modifier = GlanceModifier
                            .padding(top = 2.dp),
                        text = content.parent,
                        style = TextStyle(
                            fontWeight = FontWeight.Normal,
                            fontSize = 10.sp,
                            color = textColor
                        ),
                        maxLines = 2,
                    )
                }
            }
        }
    }
}

class RefreshClickAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val immediateRequest = OneTimeWorkRequestBuilder<RefreshWorker>().build()
        WorkManager.getInstance(context).enqueue(immediateRequest)
    }
}
