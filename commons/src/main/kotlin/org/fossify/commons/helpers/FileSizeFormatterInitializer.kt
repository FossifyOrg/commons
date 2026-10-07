package org.fossify.commons.helpers

import android.content.Context
import androidx.startup.Initializer

class FileSizeFormatterInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        FileSizeFormatter.initialize(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
