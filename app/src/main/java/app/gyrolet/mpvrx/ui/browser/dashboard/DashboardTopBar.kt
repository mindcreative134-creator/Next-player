/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.browser.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gyrolet.mpvrx.R
import app.gyrolet.mpvrx.ui.icons.Icon
import app.gyrolet.mpvrx.ui.icons.Icons

/**
 * Modern Dynamic Dashboard Top Bar.
 * Displays Hamburger Menu, Title with subtitle, Search, Cast, and Settings.
 * Completely connects to Material 3 dynamic color scheme and dark/light modes.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardTopBar(
  title: String = "NextPlayer",
  subtitle: String = "All Format Video Player",
  onMenuClick: () -> Unit = {},
  onSearchClick: () -> Unit = {},
  onSortClick: (() -> Unit)? = null,
  onCastClick: () -> Unit = {},
  onSettingsClick: () -> Unit = {},
  onTitleLongPress: (() -> Unit)? = null,
  onTitleDoubleTap: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.background,
    contentColor = MaterialTheme.colorScheme.onSurface,
  ) {
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .statusBarsPadding()
          .padding(horizontal = 16.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      // Left: Menu Hamburger + Title & Subtitle
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f),
      ) {
        IconButton(
          onClick = onMenuClick,
          modifier =
            Modifier
              .size(40.dp)
              .clip(CircleShape),
        ) {
          Icon(
            imageVector = Icons.RoundedFilled.Menu,
            contentDescription = "Menu",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp),
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
          modifier =
            Modifier.combinedClickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null,
              onClick = {},
              onDoubleClick = onTitleDoubleTap,
              onLongClick = onTitleLongPress,
            ),
        ) {
          Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
              fontSize = 20.sp,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.onSurface,
              letterSpacing = (-0.3).sp,
            ),
          )
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall.copy(
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.primary,
            ),
          )
        }
      }

      // Right: Search, Sort (A-Z), Cast, Settings Actions
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        IconButton(
          onClick = onSearchClick,
          modifier =
            Modifier
              .size(38.dp)
              .clip(CircleShape),
        ) {
          Icon(
            imageVector = Icons.RoundedFilled.Search,
            contentDescription = stringResource(R.string.settings_search_title),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
          )
        }

        if (onSortClick != null) {
          IconButton(
            onClick = onSortClick,
            modifier =
              Modifier
                .size(38.dp)
                .clip(CircleShape),
          ) {
            Icon(
              imageVector = Icons.RoundedFilled.SortByAlpha,
              contentDescription = stringResource(R.string.sort),
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(22.dp),
            )
          }
        }

        IconButton(
          onClick = onCastClick,
          modifier =
            Modifier
              .size(38.dp)
              .clip(CircleShape),
        ) {
          Icon(
            imageVector = Icons.RoundedFilled.Cast,
            contentDescription = "Cast",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
          )
        }

        IconButton(
          onClick = onSettingsClick,
          modifier =
            Modifier
              .size(38.dp)
              .clip(CircleShape),
        ) {
          Icon(
            imageVector = Icons.RoundedFilled.Settings,
            contentDescription = stringResource(R.string.ui_settings),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
          )
        }
      }
    }
  }
}
