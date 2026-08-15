/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package app.gyrolet.mpvrx.ads

import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView as GmsNativeAdView
import app.gyrolet.mpvrx.R

@Composable
fun NativeAdView(
    modifier: Modifier = Modifier,
    adUnitId: String = AdConfig.NATIVE_ADVANCED_AD_UNIT_ID,
    adId: String = "default_native"
) {
    if (!AdConfig.isAdsEnabled) return
    val context = LocalContext.current

    var nativeAd by remember(adId) { mutableStateOf<NativeAd?>(null) }
    
    LaunchedEffect(adId) {
        if (nativeAd == null) {
            AdManager.getNativeAd(context, adUnitId, adId) { ad ->
                nativeAd = ad
            }
        }
    }

    if (nativeAd == null) {
        return // Still loading or failed
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                val adView = LayoutInflater.from(ctx).inflate(R.layout.ad_native, null) as GmsNativeAdView
                
                // Populate the NativeAdView
                adView.headlineView = adView.findViewById(R.id.ad_headline)
                adView.bodyView = adView.findViewById(R.id.ad_body)
                adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
                adView.iconView = adView.findViewById(R.id.ad_app_icon)

                nativeAd?.let { ad ->
                    (adView.headlineView as TextView).text = ad.headline
                    
                    if (ad.body == null) {
                        adView.bodyView?.visibility = android.view.View.INVISIBLE
                    } else {
                        adView.bodyView?.visibility = android.view.View.VISIBLE
                        (adView.bodyView as TextView).text = ad.body
                    }

                    if (ad.callToAction == null) {
                        adView.callToActionView?.visibility = android.view.View.INVISIBLE
                    } else {
                        adView.callToActionView?.visibility = android.view.View.VISIBLE
                        (adView.callToActionView as Button).text = ad.callToAction
                    }

                    if (ad.icon == null) {
                        adView.iconView?.visibility = android.view.View.GONE
                    } else {
                        (adView.iconView as ImageView).setImageDrawable(ad.icon?.drawable)
                        adView.iconView?.visibility = android.view.View.VISIBLE
                    }

                    adView.setNativeAd(ad)
                }

                adView
            },
            update = { adView ->
                nativeAd?.let { ad -> adView.setNativeAd(ad) }
            }
        )
    }

    // We don't destroy nativeAd here because AdManager caches it for reuse across LazyColumn scrolls
}
