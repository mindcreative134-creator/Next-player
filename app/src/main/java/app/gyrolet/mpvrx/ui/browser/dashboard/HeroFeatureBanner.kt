/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.browser.dashboard

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gyrolet.mpvrx.ui.icons.Icon
import app.gyrolet.mpvrx.ui.icons.Icons

/**
 * Hero Feature Banner connecting directly to the active Theme / Appearance preferences.
 * Displays glowing themed gradient, headline, "Play Now" CTA button, and 3D Player graphic with format chips.
 */
@Composable
fun HeroFeatureBanner(
  onPlayNowClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "hero_anim")
  val floatAnim by infiniteTransition.animateFloat(
    initialValue = -3f,
    targetValue = 3f,
    animationSpec =
      infiniteRepeatable(
        animation = tween(2200, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse,
      ),
    label = "floating_box",
  )

  val pulseGlow by infiniteTransition.animateFloat(
    initialValue = 0.2f,
    targetValue = 0.5f,
    animationSpec =
      infiniteRepeatable(
        animation = tween(2600, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse,
      ),
    label = "pulse_glow",
  )

  Card(
    modifier =
      modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
  ) {
    Box(
      modifier =
        Modifier
          .fillMaxWidth()
          .background(
            Brush.linearGradient(
              colors =
                listOf(
                  MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                  MaterialTheme.colorScheme.surfaceContainerHigh,
                  MaterialTheme.colorScheme.surfaceContainerHighest,
                ),
            ),
          ).padding(16.dp),
    ) {
      // Dynamic themed glow circle in background
      Box(
        modifier =
          Modifier
            .size(140.dp)
            .align(Alignment.TopEnd)
            .graphicsLayer {
              alpha = pulseGlow
              translationX = 30f
              translationY = -20f
            }.background(
              Brush.radialGradient(
                colors =
                  listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                    Color.Transparent,
                  ),
              ),
              shape = CircleShape,
            ),
      )

      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          // Left: Headline, Subtitle, Play Now Button
          Column(
            modifier = Modifier.weight(1.15f),
          ) {
            Text(
              text = "Powerful Player",
              style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.2).sp,
              ),
            )

            Text(
              text = "For Every Format",
              style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = (-0.2).sp,
              ),
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = "Ultra HD video playback with smooth experience.",
              style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp,
              ),
              maxLines = 2,
            )

            Spacer(modifier = Modifier.height(14.dp))

            // "▶ Play Now" Pill Action Button
            Surface(
              modifier =
                Modifier
                  .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = MaterialTheme.colorScheme.primary,
                    spotColor = MaterialTheme.colorScheme.primary,
                  ).clip(RoundedCornerShape(24.dp))
                  .clickable(onClick = onPlayNowClick),
              color = Color.Transparent,
              shape = RoundedCornerShape(24.dp),
            ) {
              Box(
                modifier =
                  Modifier
                    .background(
                      Brush.horizontalGradient(
                        colors =
                          listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary.takeIf { it != MaterialTheme.colorScheme.primary }
                              ?: MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                          ),
                      ),
                    ).padding(horizontal = 14.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center,
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center,
                ) {
                  Icon(
                    imageVector = Icons.RoundedFilled.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp),
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Play Now",
                    style = MaterialTheme.typography.labelMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onPrimary,
                      fontSize = 12.sp,
                    ),
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          // Right: 3D Format Graphic with Floating Format Badges
          Box(
            modifier =
              Modifier
                .size(110.dp)
                .graphicsLayer { translationY = floatAnim },
            contentAlignment = Alignment.Center,
          ) {
            // Central glowing 3D Player Cube
            Surface(
              modifier = Modifier.size(54.dp),
              shape = RoundedCornerShape(14.dp),
              color = MaterialTheme.colorScheme.surfaceContainerHighest,
              border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
              shadowElevation = 8.dp,
            ) {
              Box(
                modifier =
                  Modifier
                    .fillMaxSize()
                    .background(
                      Brush.radialGradient(
                        listOf(
                          MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                          MaterialTheme.colorScheme.surfaceContainerHighest,
                        ),
                      ),
                    ),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = Icons.RoundedFilled.Videocam,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(28.dp),
                )
              }
            }

            // Floating Format Badges around cube
            FormatBadge(
              text = "MP4",
              modifier = Modifier.align(Alignment.TopStart).offset(x = 0.dp, y = (-2).dp),
              accentColor = MaterialTheme.colorScheme.primary,
            )
            FormatBadge(
              text = "MKV",
              modifier = Modifier.align(Alignment.TopEnd).offset(x = 0.dp, y = (-4).dp),
              accentColor = MaterialTheme.colorScheme.secondary,
            )
            FormatBadge(
              text = "AVI",
              modifier = Modifier.align(Alignment.CenterStart).offset(x = (-4).dp, y = 8.dp),
              accentColor = MaterialTheme.colorScheme.tertiary,
            )
            FormatBadge(
              text = "MOV",
              modifier = Modifier.align(Alignment.CenterEnd).offset(x = 4.dp, y = 8.dp),
              accentColor = MaterialTheme.colorScheme.primary,
            )
            FormatBadge(
              text = "FLV",
              modifier = Modifier.align(Alignment.BottomStart).offset(x = 8.dp, y = 0.dp),
              accentColor = MaterialTheme.colorScheme.secondary,
            )
            FormatBadge(
              text = "WEBM",
              modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-6).dp, y = 2.dp),
              accentColor = MaterialTheme.colorScheme.tertiary,
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Carousel Dot Indicators
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          // Active dot (elongated themed pill)
          Box(
            modifier =
              Modifier
                .width(18.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary),
          )
          Spacer(modifier = Modifier.width(5.dp))
          // Inactive dots
          Box(
            modifier =
              Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)),
          )
          Spacer(modifier = Modifier.width(5.dp))
          Box(
            modifier =
              Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)),
          )
        }
      }
    }
  }
}

@Composable
private fun FormatBadge(
  text: String,
  accentColor: Color,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(6.dp),
    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f),
    border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.6f)),
    shadowElevation = 3.dp,
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelSmall.copy(
        fontSize = 8.5.sp,
        fontWeight = FontWeight.Bold,
        color = accentColor,
      ),
      modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
    )
  }
}
