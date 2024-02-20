package com.dbottillo.lifeos.feature.widgets

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.material3.ColorProviders
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.dbottillo.lifeos.db.BlockParagraph
import com.dbottillo.lifeos.feature.blocks.BlockRepository
import com.dbottillo.lifeos.feature.home.HomeActivity
import com.dbottillo.lifeos.ui.DarkColors
import com.dbottillo.lifeos.ui.LightColors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class GoalsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = GoalsAppWidget()
}

class GoalsAppWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BlockRepositoryProviderEntryPoint {
        fun blockRepository(): BlockRepository
    }

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appContext = context.applicationContext ?: throw IllegalStateException()
        val statisticsEntryPoint =
            EntryPointAccessors.fromApplication(
                appContext,
                BlockRepositoryProviderEntryPoint::class.java,
            )
        val blockRepository = statisticsEntryPoint.blockRepository()
        provideContent {
            val goals = blockRepository.goalsBlock().collectAsState(emptyList())
            GlanceTheme(colors = LifeOSAppWidgetGlanceColorScheme.colors) {
                Box(
                    modifier = GlanceModifier
                    .padding(8.dp)
                    .background(Color.DarkGray)
                ) {
                    GoalsAppWidgetContent(goals.value)
                }
            }
        }
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    fun GoalsAppWidgetContent(goals: List<BlockParagraph>) {
        LazyColumn {
            item {
                Text(
                    modifier = GlanceModifier.fillMaxWidth(),
                    text = "Goals",
                    style = TextStyle(
                        color = GlanceTheme.colors.tertiary,
                        fontSize = 10.sp,
                    ),
                )
            }
            goals.forEach { paragraph ->
                val text = if (paragraph.type == "numbered_list_item") {
                    "${paragraph.index}. ${paragraph.text}"
                } else {
                    paragraph.text
                }
                item {
                    Text(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(top = 2.dp)
                            .clickable {
                                actionStartActivity<HomeActivity>()
                            },
                        text = text,
                        style = TextStyle(
                            color = GlanceTheme.colors.onBackground,
                            fontSize = 12.sp,
                        ),
                    )
                }
            }
        }
    }
}

object LifeOSAppWidgetGlanceColorScheme {

    val colors = ColorProviders(
        light = LightColors,
        dark = DarkColors
    )
}
