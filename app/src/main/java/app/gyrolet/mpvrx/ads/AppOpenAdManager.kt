/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package app.gyrolet.mpvrx.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.Date

/**
 * Manages App Open Ads.
 */
class AppOpenAdManager(private val application: Application) : Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var isShowingAd = false
    private var isColdStart = true
    private var loadTime: Long = 0
    private var currentActivity: Activity? = null
    private var lastShowTime: Long = 0

    companion object {
        private const val TAG = "AppOpenAdManager"
        private const val APP_OPEN_COOLDOWN_MS = 300_000L // 5 minutes cooldown
    }

    init {
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        if (isColdStart) {
            // On cold launch, pre-load ad instead of immediately blocking first UI render (AdMob Policy)
            isColdStart = false
            loadAd()
            return
        }
        showAdIfAvailable()
    }

    /**
     * Loads an app open ad.
     */
    fun loadAd() {
        if (!AdConfig.isAdsEnabled || isLoadingAd || isAdAvailable()) {
            return
        }

        isLoadingAd = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            application,
            AdConfig.APP_OPEN_AD_UNIT_ID,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = Date().time
                    Log.d(TAG, "App Open Ad Loaded")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
                    Log.e(TAG, "App Open Ad Failed to Load: ${loadAdError.message}")
                }
            }
        )
    }

    /**
     * Checks if ad exists and can be shown.
     */
    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }

    /**
     * Shows the ad if one isn't already showing.
     */
    private fun showAdIfAvailable() {
        if (!AdConfig.isAdsEnabled) return
        if (isShowingAd) return
        val activity = currentActivity ?: return
        if (activity.isFinishing || activity.isDestroyed) return
        
        // Check cooldown
        val now = System.currentTimeMillis()
        if (now - lastShowTime < APP_OPEN_COOLDOWN_MS) {
            Log.d(TAG, "App Open Ad skipped due to cooldown")
            return
        }

        val activityName = activity.javaClass.simpleName
        if (app.gyrolet.mpvrx.ui.player.PlayerActivity.isActive || activityName.contains("PlayerActivity", ignoreCase = true)) {
            Log.d(TAG, "App Open Ad skipped: User is in Player screen")
            return
        }

        if (!isAdAvailable()) {
            Log.d(TAG, "App Open Ad not available, loading one...")
            loadAd()
            return
        }

        isShowingAd = true
        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAd = false
                lastShowTime = System.currentTimeMillis()
                Log.d(TAG, "App Open Ad Dismissed")
                loadAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd = null
                isShowingAd = false
                Log.e(TAG, "App Open Ad Failed to Show: ${adError.message}")
                loadAd()
            }
        }

        try {
            appOpenAd?.show(activity)
        } catch (e: Exception) {
            Log.e(TAG, "Exception showing App Open Ad", e)
            isShowingAd = false
            appOpenAd = null
        }
    }

    // Activity Lifecycle Methods
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }
    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }
}
