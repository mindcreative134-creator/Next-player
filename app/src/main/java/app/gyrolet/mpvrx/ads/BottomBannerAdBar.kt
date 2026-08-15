/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package app.gyrolet.mpvrx.ads

import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp

/**
 * Anchored banner ad with safe visual margins and layout boundaries to prevent accidental clicks.
 *
 * @param isPlayerActive When true the banner is hidden (slide out).
 */
@Composable
fun BottomBannerAdBar(
  modifier: Modifier = Modifier,
  isPlayerActive: Boolean = false,
) {
  if (!AdConfig.isAdsEnabled) return

  val context = LocalContext.current
  val configuration = LocalConfiguration.current
  val widthDp = configuration.screenWidthDp

  val adView = remember(widthDp) {
    AdManager.getAdaptiveBannerAd(
      context = context,
      adUnitId = AdConfig.BANNER_AD_UNIT_ID,
      id = "bottom_anchored_banner",
      adWidthDp = widthDp,
    )
  }

  AnimatedVisibility(
    visible = !isPlayerActive,
    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    modifier = modifier,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .padding(bottom = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .wrapContentHeight(),
        contentAlignment = Alignment.Center,
      ) {
        AndroidView(
          modifier = Modifier.fillMaxWidth(),
          factory = { ctx ->
            (adView.parent as? android.view.ViewGroup)?.removeView(adView)
            FrameLayout(ctx).apply {
              addView(adView)
            }
          },
          update = { frame ->
            (adView.parent as? android.view.ViewGroup)?.let { parent ->
              if (parent !== frame) {
                parent.removeView(adView)
                frame.addView(adView)
              }
            }
          }
        )
      }
    }
  }
}
