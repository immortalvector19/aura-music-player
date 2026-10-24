package com.aura.music.feature.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

class AuraGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetContent()
            }
        }
    }

    @Composable
    private fun WidgetContent() {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .cornerRadius(16.dp)
                .padding(16.dp)
        ) {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album Art Placeholder
                Box(
                    modifier = GlanceModifier
                        .size(56.dp)
                        .background(GlanceTheme.colors.primaryContainer)
                        .cornerRadius(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "♫",
                        style = TextStyle(
                            fontSize = 24.sp,
                            color = GlanceTheme.colors.onPrimaryContainer
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.width(12.dp))

                // Track Info & Controls
                Column(
                    modifier = GlanceModifier.defaultWeight()
                ) {
                    Text(
                        text = "Aura Music",
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.onSurface
                        ),
                        maxLines = 1
                    )
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    Text(
                        text = "Touch to play",
                        style = TextStyle(
                            fontSize = 13.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        ),
                        maxLines = 1
                    )

                    Spacer(modifier = GlanceModifier.height(8.dp))

                    // Transport Actions
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WidgetControlButton(
                            text = "⏮",
                            action = WidgetAction.PREVIOUS
                        )
                        Spacer(modifier = GlanceModifier.width(16.dp))
                        WidgetControlButton(
                            text = "⏯",
                            action = WidgetAction.PLAY_PAUSE
                        )
                        Spacer(modifier = GlanceModifier.width(16.dp))
                        WidgetControlButton(
                            text = "⏭",
                            action = WidgetAction.NEXT
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun WidgetControlButton(
        text: String,
        action: WidgetAction
    ) {
        val params = actionParametersOf(ActionKey to action.name)
        Box(
            modifier = GlanceModifier
                .size(36.dp)
                .background(GlanceTheme.colors.secondaryContainer)
                .cornerRadius(18.dp)
                .clickable(actionRunCallback<WidgetActionCallback>(params)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 16.sp,
                    color = GlanceTheme.colors.onSecondaryContainer
                )
            )
        }
    }

    companion object {
        val ActionKey = ActionParameters.Key<String>("action_name")
    }
}

enum class WidgetAction {
    PLAY_PAUSE,
    PREVIOUS,
    NEXT
}

class WidgetActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val action = parameters[AuraGlanceWidget.ActionKey] ?: return
        // Send intent to AuraMediaService or broadcast receiver
    }
}
