package org.fossify.commons.extensions

import android.app.Activity
import org.fossify.commons.R
import org.fossify.commons.dialogs.DonateDialog
import org.fossify.commons.dialogs.RateAppDialog
import org.fossify.commons.helpers.BaseConfig

private const val FIRST_PROMPT_MIN_LAUNCHES = 10
private const val NEXT_PROMPT_MIN_LAUNCHES = 5
private const val MAX_PROMPT_DISMISSALS = 3
private const val DAY_MILLIS = 24L * 60L * 60L * 1000L
private const val FIRST_PROMPT_DELAY_MILLIS = 7L * DAY_MILLIS
private const val NEXT_PROMPT_DELAY_MILLIS = 30L * DAY_MILLIS

private enum class SupportPromptType(val value: Int) {
    RateApp(0),
    ThankYou(1);

    fun alternate() = when (this) {
        RateApp -> ThankYou
        ThankYou -> RateApp
    }

    companion object {
        fun fromValue(value: Int) = entries.firstOrNull { it.value == value } ?: RateApp
    }
}

internal fun Activity.showAutomaticSupportPromptIfEligible() {
    val config = baseConfig
    val now = System.currentTimeMillis()
    if (config.supportPromptFirstLaunchTimestamp == 0L) {
        config.supportPromptFirstLaunchTimestamp = now
        return
    }

    val hasEnoughInitialUse = config.appRunCount >= FIRST_PROMPT_MIN_LAUNCHES
    val hasEnoughInitialTime = now - config.supportPromptFirstLaunchTimestamp >= FIRST_PROMPT_DELAY_MILLIS
    val isSupportPromptDisabled =
        resources.getBoolean(R.bool.hide_google_relations) ||
            config.supportPromptDismissalCount >= MAX_PROMPT_DISMISSALS
    val lacksInitialEligibility = !hasEnoughInitialUse || !hasEnoughInitialTime
    if (isSupportPromptDisabled || lacksInitialEligibility) {
        return
    }

    if (config.supportPromptLastShownTimestamp != 0L) {
        val hasEnoughFurtherUse =
            config.appRunCount - config.supportPromptLastShownAppRunCount >= NEXT_PROMPT_MIN_LAUNCHES
        val hasEnoughFurtherTime = now - config.supportPromptLastShownTimestamp >= NEXT_PROMPT_DELAY_MILLIS
        if (!hasEnoughFurtherUse || !hasEnoughFurtherTime) {
            return
        }
    }

    val promptType = selectSupportPrompt(config) ?: return
    config.supportPromptLastShownTimestamp = now
    config.supportPromptLastShownAppRunCount = config.appRunCount
    config.supportPromptNextType = promptType.alternate().value

    val onLater = {
        config.supportPromptDismissalCount =
            (config.supportPromptDismissalCount + 1).coerceAtMost(MAX_PROMPT_DISMISSALS)
    }

    when (promptType) {
        SupportPromptType.RateApp -> RateAppDialog(
            activity = this,
            onRate = {
                config.wasRatePromptAccepted = true
                launchAppRatingPage()
            },
            onLater = onLater,
        )

        SupportPromptType.ThankYou -> DonateDialog(
            activity = this,
            onPurchase = { launchPurchaseThankYouIntent() },
            onLater = onLater,
        )
    }
}

private fun Activity.selectSupportPrompt(config: BaseConfig): SupportPromptType? {
    val canShowRatePrompt = !config.wasRatePromptAccepted
    val canShowThankYouPrompt = !isOrWasThankYouInstalled()
    val preferredPrompt = SupportPromptType.fromValue(config.supportPromptNextType)

    return when {
        preferredPrompt == SupportPromptType.RateApp && canShowRatePrompt -> SupportPromptType.RateApp
        preferredPrompt == SupportPromptType.ThankYou && canShowThankYouPrompt -> SupportPromptType.ThankYou
        canShowRatePrompt -> SupportPromptType.RateApp
        canShowThankYouPrompt -> SupportPromptType.ThankYou
        else -> null
    }
}
