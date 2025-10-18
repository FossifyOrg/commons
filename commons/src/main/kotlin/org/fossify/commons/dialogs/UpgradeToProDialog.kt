package org.fossify.commons.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import org.fossify.commons.R
import org.fossify.commons.compose.alert_dialog.*
import org.fossify.commons.compose.extensions.MyDevices
import org.fossify.commons.compose.extensions.andThen
import org.fossify.commons.compose.theme.AppThemeSurface
import org.fossify.commons.compose.theme.SimpleTheme
import org.fossify.commons.databinding.DialogUpgradeToProBinding
import org.fossify.commons.extensions.getAlertDialogBuilder
import org.fossify.commons.extensions.launchUpgradeToProIntent
import org.fossify.commons.extensions.launchViewIntent
import org.fossify.commons.extensions.setupDialogStuff

class UpgradeToProDialog(val activity: Activity) {

    init {
        val view = DialogUpgradeToProBinding.inflate(activity.layoutInflater, null, false).apply {
            upgradeToPro.text = activity.getString(R.string.upgrade_to_pro_long)
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.upgrade) { _, _ -> upgradeApp() }
            .setNeutralButton(R.string.more_info, null)     // do not dismiss the dialog on pressing More Info
            .setNegativeButton(R.string.later, null)
            .apply {
                activity.setupDialogStuff(view.root, this, R.string.upgrade_to_pro, cancelOnTouchOutside = false) { alertDialog ->
                    alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                        moreInfo()
                    }
                }
            }
    }

    private fun upgradeApp() {
        activity.launchUpgradeToProIntent()
    }

    private fun moreInfo() {
        activity.launchViewIntent("https://fossify.org/upgrade_to_pro")
    }
}

@Composable
fun UpgradeToProAlertDialog(
    alertDialogState: AlertDialogState,
    modifier: Modifier = Modifier,
    onMoreInfoClick: () -> Unit,
    onUpgradeClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        text = {
            Text(
                text = stringResource(id = R.string.upgrade_to_pro_long),
                color = dialogTextColor,
                fontSize = 16.sp,
            )
        },
        title = {
            Text(
                text = stringResource(id = R.string.upgrade_to_pro),
                color = dialogTextColor,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
            )
        },
        tonalElevation = dialogElevation,
        shape = dialogShape,
        containerColor = dialogContainerColor,
        confirmButton = {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onMoreInfoClick, // do not dismiss the dialog on pressing More Info
                    modifier = Modifier.weight(0.2f)
                ) {
                    Text(text = stringResource(id = R.string.more_info))
                }
                TextButton(
                    onClick = alertDialogState::hide,
                    modifier = Modifier.padding(horizontal = SimpleTheme.dimens.padding.medium)
                ) {
                    Text(text = stringResource(id = R.string.later))
                }
                TextButton(
                    onClick = onUpgradeClick andThen alertDialogState::hide,
                ) {
                    Text(text = stringResource(id = R.string.upgrade))
                }
            }
        },
        modifier = modifier.dialogBorder(),
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    )
}

@MyDevices
@Composable
private fun UpgradeToProAlertDialogPreview() {
    AppThemeSurface {
        UpgradeToProAlertDialog(
            alertDialogState = rememberAlertDialogState(),
            onMoreInfoClick = {},
            onUpgradeClick = {},
        )
    }
}
