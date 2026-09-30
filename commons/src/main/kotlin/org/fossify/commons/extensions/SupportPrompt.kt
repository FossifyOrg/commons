package org.fossify.commons.extensions

import android.app.Activity
import org.fossify.commons.R
import org.fossify.commons.dialogs.DonateDialog
import org.fossify.commons.dialogs.RateAppDialog

private const val DAY_MILLIS = 24L * 60L * 60L * 1000L
private const val RATING_DELAY_MILLIS = 5L * DAY_MILLIS
private const val DONATION_DELAY_MILLIS = 10L * DAY_MILLIS

internal fun Activity.showAutomaticSupportPromptIfEligible() {
    if (resources.getBoolean(R.bool.is_paid_app)) {
        return
    }

    val config = baseConfig
    val isSupporter = hasThankYouUnlock()
    val now = System.currentTimeMillis()
    if (config.supportPromptFirstLaunchTimestamp == 0L) {
        config.supportPromptFirstLaunchTimestamp = now
        return
    }

    val elapsed = now - config.supportPromptFirstLaunchTimestamp
    if (
        resources.getBoolean(R.bool.is_google_play_build) &&
        !config.wasRatePromptShown && elapsed >= RATING_DELAY_MILLIS
    ) {
        RateAppDialog(this, onRate = { launchAppRatingPage() }) {
            config.wasRatePromptShown = true
        }
    } else if (!config.wasDonationPromptShown && elapsed >= DONATION_DELAY_MILLIS && !isSupporter) {
        DonateDialog(this) {
            config.wasDonationPromptShown = true
        }
    }
}
