/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.player.controls.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gyrolet.mpvrx.ui.icons.Icon
import app.gyrolet.mpvrx.ui.icons.Icons

/**
 * Ultra-Modern Glowing Circular Buffering Indicator for Smooth Online Streaming and Video Playback.
 */
@Composable
fun GlowingCircularBufferIndicator(
  modifier: Modifier = Modifier,
  text: String = "BUFFERING...",
) {
  val infiniteTransition = rememberInfiniteTransition(label = "buffer_infinite")

  // Continuous rotation angle
  val rotationAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec =
      infiniteRepeatable(
        animation = tween(durationMillis = 1100, easing = LinearEasing),
        repeatMode = RepeatMode.Restart,
      ),
    label = "buffer_rotation",
  )

  // Pulsating glow scale/alpha
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.35f,
    targetValue = 0.85f,
    animationSpec =
      infiniteRepeatable(
        animation = tween(durationMillis = 800, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse,
      ),
    label = "buffer_pulse",
  )

  Surface(
    shape = RoundedCornerShape(22.dp),
    color = Color(0xFF0D0E15).copy(alpha = 0.82f),
    border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.45f)),
    modifier =
      modifier
        .shadow(16.dp, shape = RoundedCornerShape(22.dp), ambientColor = Color(0xFF8B5CF6))
        .padding(horizontal = 18.dp, vertical = 14.dp),
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
      // Circular Animated Glowing Spinner Ring
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(64.dp),
      ) {
        // Outer glowing shadow ring
        Canvas(
          modifier =
            Modifier
              .size(58.dp)
              .rotate(rotationAngle),
        ) {
          val strokeWidth = 4.dp.toPx()
          val sweepGradient =
            Brush.sweepGradient(
              listOf(
                Color(0xFF8B5CF6),
                Color(0xFF38BDF8),
                Color(0xFFEC4899),
                Color(0xFF8B5CF6).copy(alpha = 0.05f),
              ),
            )
          drawArc(
            brush = sweepGradient,
            startAngle = 0f,
            sweepAngle = 280f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
          )
        }

        // Inner glowing core
        Surface(
          shape = CircleShape,
          color = Color(0xFF1E103E).copy(alpha = pulseAlpha),
          border = BorderStroke(0.8.dp, Color(0xFF8B5CF6).copy(alpha = pulseAlpha)),
          modifier = Modifier.size(36.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.RoundedFilled.PlayArrow,
              contentDescription = null,
              tint = Color(0xFFA78BFA),
              modifier = Modifier.size(20.dp),
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
      ) {
        Surface(
          shape = CircleShape,
          color = Color(0xFF10B981).copy(alpha = pulseAlpha),
          modifier = Modifier.size(6.dp),
        ) {}

        Spacer(modifier = Modifier.width(6.dp))

        Text(
          text = text,
          style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFFE2E8F0),
            letterSpacing = 1.2.sp,
          ),
        )
      }
    }
  }
}
