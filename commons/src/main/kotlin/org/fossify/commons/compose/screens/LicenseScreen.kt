package org.fossify.commons.compose.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.fossify.commons.R
import org.fossify.commons.compose.extensions.MyDevices
import org.fossify.commons.compose.lists.SimpleLazyListScaffold
import org.fossify.commons.compose.settings.SettingsHorizontalDivider
import org.fossify.commons.compose.theme.AppThemeSurface
import org.fossify.commons.compose.theme.SimpleTheme
import org.fossify.commons.helpers.*
import org.fossify.commons.models.License

@Composable
internal fun LicenseScreen(
    goBack: () -> Unit,
    thirdPartyLicenses: ImmutableList<License>,
    onLicenseClick: (urlId: Int) -> Unit,
) {
    SimpleLazyListScaffold(
        title = stringResource(id = R.string.third_party_licences),
        goBack = goBack
    ) {
        itemsIndexed(thirdPartyLicenses) { index, license ->
            Column {
                LicenseItem(license, onLicenseClick)
                if (index != thirdPartyLicenses.lastIndex) {
                    SettingsHorizontalDivider(modifier = Modifier.padding(bottom = SimpleTheme.dimens.padding.small))
                }
            }
        }
    }
}

@Composable
private fun LicenseItem(
    license: License,
    onLicenseClick: (urlId: Int) -> Unit
) {
    ListItem(headlineContent = {
        Text(
            text = stringResource(id = license.titleId),
            modifier = Modifier
                .clickable {
                    onLicenseClick(license.urlId)
                }
        )
    }, supportingContent = {
        Text(
            text = stringResource(id = license.textId),
            modifier = Modifier.padding(top = SimpleTheme.dimens.padding.extraSmall),
        )
    }, colors = ListItemDefaults.colors(headlineColor = SimpleTheme.colorScheme.primary, supportingColor = SimpleTheme.colorScheme.onSurface))
}

@Composable
@MyDevices
private fun LicenseScreenPreview() {
    AppThemeSurface {
        LicenseScreen(
            goBack = {},
            thirdPartyLicenses = listOf(
                License(LICENSE_KOTLIN, R.string.kotlin_title, R.string.kotlin_text, R.string.kotlin_url),
                License(LICENSE_SUBSAMPLING, R.string.subsampling_title, R.string.subsampling_text, R.string.subsampling_url),
                License(LICENSE_GLIDE, R.string.glide_title, R.string.glide_text, R.string.glide_url),
                License(LICENSE_CROPPER, R.string.cropper_title, R.string.cropper_text, R.string.cropper_url),
                License(LICENSE_RTL, R.string.rtl_viewpager_title, R.string.rtl_viewpager_text, R.string.rtl_viewpager_url),
                License(LICENSE_JODA, R.string.joda_title, R.string.joda_text, R.string.joda_url),
                License(LICENSE_STETHO, R.string.stetho_title, R.string.stetho_text, R.string.stetho_url),
                License(LICENSE_OTTO, R.string.otto_title, R.string.otto_text, R.string.otto_url),
                License(LICENSE_PHOTOVIEW, R.string.photoview_title, R.string.photoview_text, R.string.photoview_url),
                License(LICENSE_PICASSO, R.string.picasso_title, R.string.picasso_text, R.string.picasso_url),
                License(LICENSE_PATTERN, R.string.pattern_title, R.string.pattern_text, R.string.pattern_url),
                License(LICENSE_REPRINT, R.string.reprint_title, R.string.reprint_text, R.string.reprint_url),
                License(LICENSE_GIF_DRAWABLE, R.string.gif_drawable_title, R.string.gif_drawable_text, R.string.gif_drawable_url),
                License(LICENSE_AUTOFITTEXTVIEW, R.string.autofittextview_title, R.string.autofittextview_text, R.string.autofittextview_url),
                License(LICENSE_ROBOLECTRIC, R.string.robolectric_title, R.string.robolectric_text, R.string.robolectric_url),
                License(LICENSE_ESPRESSO, R.string.espresso_title, R.string.espresso_text, R.string.espresso_url),
                License(LICENSE_GSON, R.string.gson_title, R.string.gson_text, R.string.gson_url),
                License(LICENSE_LEAK_CANARY, R.string.leak_canary_title, R.string.leakcanary_text, R.string.leakcanary_url),
                License(LICENSE_NUMBER_PICKER, R.string.number_picker_title, R.string.number_picker_text, R.string.number_picker_url),
                License(LICENSE_EXOPLAYER, R.string.exoplayer_title, R.string.exoplayer_text, R.string.exoplayer_url),
                License(LICENSE_SANSELAN, R.string.sanselan_title, R.string.sanselan_text, R.string.sanselan_url),
                License(LICENSE_FILTERS, R.string.filters_title, R.string.filters_text, R.string.filters_url),
                License(LICENSE_GESTURE_VIEWS, R.string.gesture_views_title, R.string.gesture_views_text, R.string.gesture_views_url),
                License(
                    LICENSE_INDICATOR_FAST_SCROLL,
                    R.string.indicator_fast_scroll_title,
                    R.string.indicator_fast_scroll_text,
                    R.string.indicator_fast_scroll_url
                ),
                License(LICENSE_EVENT_BUS, R.string.event_bus_title, R.string.event_bus_text, R.string.event_bus_url),
                License(LICENSE_AUDIO_RECORD_VIEW, R.string.audio_record_view_title, R.string.audio_record_view_text, R.string.audio_record_view_url),
                License(LICENSE_SMS_MMS, R.string.sms_mms_title, R.string.sms_mms_text, R.string.sms_mms_url),
                License(LICENSE_APNG, R.string.apng_title, R.string.apng_text, R.string.apng_url),
                License(LICENSE_PDF_VIEW_PAGER, R.string.pdf_view_pager_title, R.string.pdf_view_pager_text, R.string.pdf_view_pager_url),
                License(LICENSE_M3U_PARSER, R.string.m3u_parser_title, R.string.m3u_parser_text, R.string.m3u_parser_url),
                License(LICENSE_ANDROID_LAME, R.string.android_lame_title, R.string.android_lame_text, R.string.android_lame_url),
                License(LICENSE_PDF_VIEWER, R.string.pdf_viewer_title, R.string.pdf_viewer_text, R.string.pdf_viewer_url),
                License(LICENSE_ZIP4J, R.string.zip4j_title, R.string.zip4j_text, R.string.zip4j_url),
                License(LICENSE_EVALEX, R.string.evalex_title, R.string.evalex_text, R.string.evalex_url)
            ).toImmutableList()
        ) {

        }
    }
}

