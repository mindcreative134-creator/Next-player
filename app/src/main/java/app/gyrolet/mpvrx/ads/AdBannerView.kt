/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package app.gyrolet.mpvrx.ads

import android.widget.FrameLayout
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

@Composable
fun AdBannerView(
  modifier: Modifier = Modifier,
  adUnitId: String = AdConfig.BANNER_AD_UNIT_ID,
  adId: String = "default_banner"
) {
  if (!AdConfig.isAdsEnabled) return
  val context = LocalContext.current
  val configuration = LocalConfiguration.current
  val widthDp = configuration.screenWidthDp

  val adView = remember(adId, widthDp) {
    AdManager.getAdaptiveBannerAd(context, adUnitId, adId, widthDp)
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .wrapContentHeight(),
    contentAlignment = Alignment.Center,
  ) {
    AndroidView(
      modifier = Modifier.fillMaxWidth(),
      factory = {
        (adView.parent as? android.view.ViewGroup)?.removeView(adView)
        FrameLayout(it).apply {
          addView(adView)
        }
      },
      update = {
        (adView.parent as? android.view.ViewGroup)?.let { parent ->
          if (parent !== it) {
            parent.removeView(adView)
            it.addView(adView)
          }
        }
      }
    )
  }
}
