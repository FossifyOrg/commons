package org.fossify.commons.activities

import android.content.Context
import androidx.activity.ComponentActivity
import org.fossify.commons.extensions.baseConfig
import org.fossify.commons.helpers.MyContextWrapper
import org.fossify.commons.helpers.REQUEST_APP_UNLOCK
import org.fossify.commons.helpers.isTiramisuPlus

abstract class BaseComposeActivity : ComponentActivity() {

    override fun onResume() {
        super.onResume()
        maybeLaunchAppUnlockActivity(REQUEST_APP_UNLOCK)
    }

    override fun attachBaseContext(newBase: Context) {
        if (newBase.baseConfig.useEnglish && !isTiramisuPlus()) {
            super.attachBaseContext(MyContextWrapper(newBase).wrap(newBase, "en"))
        } else {
            super.attachBaseContext(newBase)
        }
    }
}
