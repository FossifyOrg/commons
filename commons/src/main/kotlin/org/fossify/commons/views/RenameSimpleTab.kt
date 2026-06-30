package org.fossify.commons.views

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.util.AttributeSet
import android.widget.RelativeLayout
import org.fossify.commons.R
import org.fossify.commons.activities.BaseSimpleActivity
import org.fossify.commons.databinding.TabRenameSimpleBinding
import org.fossify.commons.extensions.*
import org.fossify.commons.interfaces.RenameTab
import org.fossify.commons.models.Android30RenameFormat
import org.fossify.commons.models.FileDirItem
import java.io.File

class RenameSimpleTab(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs), RenameTab {
    var ignoreClicks = false
    var stopLooping = false     // we should request the permission on Android 30+ for all uris at once, not one by one
    var activity: BaseSimpleActivity? = null
    var paths = ArrayList<String>()

    private lateinit var binding: TabRenameSimpleBinding

    override fun onFinishInflate() {
        super.onFinishInflate()
        binding = TabRenameSimpleBinding.bind(this)
        context.updateTextColors(binding.renameSimpleHolder)
    }

    override fun initTab(activity: BaseSimpleActivity, paths: ArrayList<String>) {
        this.activity = activity
        this.paths = paths
    }

    override fun dialogConfirmed(useMediaFileExtension: Boolean, callback: (success: Boolean) -> Unit) {
        stopLooping = false
        val valueToAdd = binding.renameSimpleValue.text.toString()
        val append = binding.renameSimpleRadioGroup.checkedRadioButtonId == binding.renameSimpleRadioAppend.id

        if (valueToAdd.isEmpty()) {
            callback(false)
            return
        }

        if (!valueToAdd.isAValidFilename()) {
            activity?.toast(R.string.invalid_name)
            return
        }

        val validPaths = paths.filter { activity?.getDoesFilePathExist(it) == true }
        val firstPath = validPaths.firstOrNull()
        val sdFilePath = validPaths.firstOrNull { activity?.isPathOnSD(it) == true } ?: firstPath
        if (firstPath == null || sdFilePath == null) {
            activity?.toast(R.string.unknown_error_occurred)
            return
        }

        activity?.handleSAFDialog(sdFilePath) {
            if (!it) {
                return@handleSAFDialog
            }

            activity?.checkManageMediaOrHandleSAFDialogSdk30(firstPath) {
                if (!it) {
                    return@checkManageMediaOrHandleSAFDialogSdk30
                }

                ignoreClicks = true
                var pathsCnt = validPaths.size
                for (path in validPaths) {
                    if (stopLooping) {
                        return@checkManageMediaOrHandleSAFDialogSdk30
                    }

                    val fullName = path.getFilenameFromPath()
                    var dotAt = fullName.lastIndexOf(".")
                    if (dotAt == -1) {
                        dotAt = fullName.length
                    }

                    val name = fullName.substring(0, dotAt)
                    val extension = if (fullName.contains(".")) ".${fullName.getFilenameExtension()}" else ""

                    val newName = if (append) {
                        "$name$valueToAdd$extension"
                    } else {
                        "$valueToAdd$fullName"
                    }

                    val newPath = "${path.getParentPath()}/$newName"

                    if (activity?.getDoesFilePathExist(newPath) == true) {
                        continue
                    }

                    activity?.renameFile(path, newPath, true) { success, android30Format ->
                        if (success) {
                            pathsCnt--
                            if (pathsCnt == 0) {
                                callback(true)
                            }
                        } else {
                            ignoreClicks = false
                            if (android30Format != Android30RenameFormat.NONE) {
                                stopLooping = true
                                renameAllFiles(validPaths, append, valueToAdd, android30Format, callback)
                            }
                        }
                    }
                }
                stopLooping = false
            }
        }
    }

    private fun getNewFileName(path: String, appendString: Boolean, stringToAdd: String): String {
        val fullName = path.getFilenameFromPath()
        val dotAt = fullName.lastIndexOf(".").takeIf { it != -1 } ?: fullName.length
        val name = fullName.substring(0, dotAt)
        val extension = if (fullName.contains(".")) ".${fullName.getFilenameExtension()}" else ""

        return if (appendString) {
            "$name$stringToAdd$extension"
        } else {
            "$stringToAdd$fullName"
        }
    }

    private fun renameAllFiles(
        paths: List<String>,
        appendString: Boolean,
        stringToAdd: String,
        android30Format: Android30RenameFormat,
        callback: (success: Boolean) -> Unit
    ) {
        val fileDirItems = paths.map { File(it).toFileDirItem(context) }
        val activity = activity ?: return
        context.resolveMediaStoreUris(fileDirItems) { resolution ->
            if (resolution.unresolved.isNotEmpty()) {
                activity.toast(R.string.unknown_error_occurred)
                callback(false)
                return@resolveMediaStoreUris
            }

            val resolved = resolution.resolved
            activity.updateSDK30Uris(resolution.uris) { success ->
                if (!success) {
                    callback(false)
                    return@updateSDK30Uris
                }

                try {
                    resolved.forEach { resolvedUri ->
                        val path = resolvedUri.fileDirItem.path
                        val uri = resolvedUri.uri
                        val newName = getNewFileName(path, appendString, stringToAdd)

                        when (android30Format) {
                            Android30RenameFormat.SAF -> {
                                val sourceFile = File(path).toFileDirItem(activity)
                                val newPath = "${path.getParentPath()}/$newName"
                                val destinationFile = FileDirItem(
                                    newPath,
                                    newName,
                                    sourceFile.isDirectory,
                                    sourceFile.children,
                                    sourceFile.size,
                                    sourceFile.modified
                                )
                                if (activity.copySingleFileSdk30(sourceFile, destinationFile)) {
                                    if (!activity.baseConfig.keepLastModified) {
                                        File(newPath).setLastModified(System.currentTimeMillis())
                                    }
                                    activity.contentResolver.delete(uri, null)
                                    activity.updateInMediaStore(path, newPath)
                                    activity.scanPathsRecursively(arrayListOf(newPath))
                                }
                            }

                            Android30RenameFormat.CONTENT_RESOLVER -> {
                                val values = ContentValues().apply {
                                    put(MediaStore.Images.Media.DISPLAY_NAME, newName)
                                }
                                context.contentResolver.update(uri, values, null, null)
                            }

                            Android30RenameFormat.NONE -> {
                                activity.runOnUiThread {
                                    callback(true)
                                }
                                return@forEach
                            }
                        }
                    }
                    activity.runOnUiThread {
                        callback(true)
                    }
                } catch (e: Exception) {
                    activity.runOnUiThread {
                        activity.showErrorToast(e)
                        callback(false)
                    }
                }
            }
        }
    }
}
