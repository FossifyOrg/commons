package org.fossify.commons.compose.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.stringResource
import org.fossify.commons.R
import org.fossify.commons.compose.extensions.MyDevices
import org.fossify.commons.compose.extensions.onResumeEventValue
import org.fossify.commons.compose.theme.AppThemeSurface
import org.fossify.commons.extensions.findActivity
import org.fossify.commons.extensions.hasThankYouUnlock
import org.fossify.commons.extensions.launchPurchaseThankYouIntent
import org.fossify.commons.extensions.toast

@Composable
fun PurchaseThankYouPreference(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    if (!booleanResource(R.bool.is_google_play_build) && !isPreview) return

    val hasThankYouUnlock = if (isPreview) {
        false
    } else {
        onResumeEventValue(context) { context.hasThankYouUnlock() }
    }
    if (hasThankYouUnlock) return

    SettingsPreferenceComponent(
        modifier = modifier,
        label = stringResource(R.string.purchase_simple_thank_you),
        value = stringResource(R.string.enables_more_customization),
        doOnPreferenceClick = {
            if (!isPreview) {
                val activity = context.findActivity()
                if (activity != null) {
                    activity.launchPurchaseThankYouIntent()
                } else {
                    context.toast(R.string.unknown_error_occurred)
                }
            }
        },
    )
}

@MyDevices
@Composable
@Suppress("UnusedPrivateMember")
private fun PurchaseThankYouPreferencePreview() {
    AppThemeSurface {
        PurchaseThankYouPreference()
    }
}
