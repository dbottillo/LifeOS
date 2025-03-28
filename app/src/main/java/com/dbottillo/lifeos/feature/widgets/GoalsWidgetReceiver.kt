package com.dbottillo.lifeos.feature.widgets

import android.content.Context
import android.content.Intent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.material3.ColorProviders
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.dbottillo.lifeos.feature.blocks.GoalsRepository
import com.dbottillo.lifeos.feature.home.HomeActivity
import com.dbottillo.lifeos.feature.tasks.Goal
import com.dbottillo.lifeos.feature.tasks.Status
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
        fun blockRepository(): GoalsRepository
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
            val goals = blockRepository.goalsFlow.collectAsState(emptyList())
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
    fun GoalsAppWidgetContent(goals: List<Goal>) {
        LazyColumn(
            modifier = GlanceModifier
        ) {
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
            goals.filter { it.status is Status.Focus }.forEach { goal ->
                item {
                    Text(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(top = 2.dp)
                            .clickable(
                                onClick = actionRunCallback<OpenAppClickAction>()
                            ),
                        text = "\u2022\t" + goal.text,
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

class OpenAppClickAction : ActionCallback {
    @OptIn(ExperimentalMaterial3Api::class)
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val intent = Intent(context, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

object LifeOSAppWidgetGlanceColorScheme {
    val colors = ColorProviders(
        light = LightColors,
        dark = DarkColors
    )
}
