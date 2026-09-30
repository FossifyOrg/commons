package org.fossify.commons.dialogs

import android.app.Activity
import org.fossify.commons.R
import org.fossify.commons.databinding.DialogMessageBinding
import org.fossify.commons.extensions.getAlertDialogBuilder
import org.fossify.commons.extensions.setupDialogStuff

internal class RateAppDialog(
    activity: Activity,
    onRate: () -> Unit,
    onShown: () -> Unit,
) {
    init {
        val view = DialogMessageBinding.inflate(activity.layoutInflater, null, false).apply {
            message.setText(R.string.rate_app_prompt)
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.rate_this_app) { _, _ -> onRate() }
            .setNegativeButton(R.string.later, null)
            .apply {
                activity.setupDialogStuff(
                    view = view.root,
                    dialog = this,
                    titleId = R.string.rate_this_app,
                ) { dialog ->
                    if (dialog.isShowing) {
                        onShown()
                    }
                }
            }
    }
}
