package org.fossify.commons.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.fossify.commons.extensions.syncGlobalConfig
import org.fossify.commons.helpers.MyContentProvider

class FossifyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == MyContentProvider.ACTION_GLOBAL_CONFIG_UPDATED) {
            context?.let {
                val pendingResult = goAsync()
                it.syncGlobalConfig { pendingResult.finish() }
            }
        }
    }
}
