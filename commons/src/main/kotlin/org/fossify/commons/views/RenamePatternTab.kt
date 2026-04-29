package org.fossify.commons.views

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.text.format.DateFormat
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.exifinterface.media.ExifInterface
import org.fossify.commons.R
import org.fossify.commons.activities.BaseSimpleActivity
import org.fossify.commons.databinding.DialogRenameItemsPatternBinding
import org.fossify.commons.extensions.*
import org.fossify.commons.helpers.isNougatPlus
import org.fossify.commons.interfaces.RenameTab
import org.fossify.commons.models.Android30RenameFormat
import org.fossify.commons.models.FileDirItem
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RenamePatternTab(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs), RenameTab {
    var ignoreClicks = false
    var stopLooping = false     // we should request the permission on Android 30+ for all uris at once, not one by one
    var currentIncrementalNumber = 1
    var numbersCnt = 0
    var activity: BaseSimpleActivity? = null
    var paths = ArrayList<String>()

    private lateinit var binding: DialogRenameItemsPatternBinding

    override fun onFinishInflate() {
        super.onFinishInflate()
        binding = DialogRenameItemsPatternBinding.bind(this)
        context.updateTextColors(binding.renameItemsHolder)
    }

    override fun initTab(activity: BaseSimpleActivity, paths: ArrayList<String>) {
        this.activity = activity
        this.paths = paths
        binding.renameItemsValue.setText(activity.baseConfig.lastRenamePatternUsed)
    }

    override fun dialogConfirmed(useMediaFileExtension: Boolean, callback: (success: Boolean) -> Unit) {
        stopLooping = false
        if (ignoreClicks) {
            return
        }

        val newNameRaw = binding.renameItemsValue.value
        if (newNameRaw.isEmpty()) {
            callback(false)
            return
        }

        if (!newNameRaw.isAValidFilename()) {
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

        activity?.baseConfig?.lastRenamePatternUsed = binding.renameItemsValue.value
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
                numbersCnt = pathsCnt.toString().length
                for (path in validPaths) {
                    if (stopLooping) {
                        return@checkManageMediaOrHandleSAFDialogSdk30
                    }

                    try {
                        val newPath = getNewPath(path, useMediaFileExtension) ?: continue
                        activity?.renameFile(path, newPath, true) { success, android30Format ->
                            if (success) {
                                pathsCnt--
                                if (pathsCnt == 0) {
                                    callback(true)
                                }
                            } else {
                                ignoreClicks = false
                                if (android30Format != Android30RenameFormat.NONE) {
                                    currentIncrementalNumber = 1
                                    stopLooping = true
                                    renameAllFiles(validPaths, useMediaFileExtension, android30Format, callback)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        activity?.showErrorToast(e)
                    }
                }
                stopLooping = false
            }
        }
    }

    private fun getNewPath(path: String, useMediaFileExtension: Boolean): String? {
        try {
            val exif = ExifInterface(path)
            var dateTime = if (isNougatPlus()) {
                exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL) ?: exif.getAttribute(ExifInterface.TAG_DATETIME)
            } else {
                exif.getAttribute(ExifInterface.TAG_DATETIME)
            }

            if (dateTime == null) {
                val calendar = Calendar.getInstance(Locale.ENGLISH)
                calendar.timeInMillis = File(path).lastModified()
                dateTime = DateFormat.format("yyyy:MM:dd kk:mm:ss", calendar).toString()
            }

            val pattern = if (dateTime.substring(4, 5) == "-") "yyyy-MM-dd kk:mm:ss" else "yyyy:MM:dd kk:mm:ss"
            val simpleDateFormat = SimpleDateFormat(pattern, Locale.ENGLISH)

            val dt = simpleDateFormat.parse(dateTime.replace("T", " "))
            val cal = Calendar.getInstance()
            cal.time = dt
            val year = cal.get(Calendar.YEAR).toString()
            val month = (cal.get(Calendar.MONTH) + 1).ensureTwoDigits()
            val day = (cal.get(Calendar.DAY_OF_MONTH)).ensureTwoDigits()
            val hours = (cal.get(Calendar.HOUR_OF_DAY)).ensureTwoDigits()
            val minutes = (cal.get(Calendar.MINUTE)).ensureTwoDigits()
            val seconds = (cal.get(Calendar.SECOND)).ensureTwoDigits()

            var newName = binding.renameItemsValue.value
                .replace("%Y", year, false)
                .replace("%M", month, false)
                .replace("%D", day, false)
                .replace("%h", hours, false)
                .replace("%m", minutes, false)
                .replace("%s", seconds, false)
                .replace("%i", String.format("%0${numbersCnt}d", currentIncrementalNumber))

            if (newName.isEmpty()) {
                return null
            }

            currentIncrementalNumber++
            if ((!newName.contains(".") && path.contains(".")) || (useMediaFileExtension && !".${newName.substringAfterLast(".")}".isMediaFile())) {
                val extension = path.substringAfterLast(".")
                newName += ".$extension"
            }

            var newPath = "${path.getParentPath()}/$newName"

            var currentIndex = 0
            while (activity?.getDoesFilePathExist(newPath) == true) {
                currentIndex++
                var extension = ""
                val name = if (newName.contains(".")) {
                    extension = ".${newName.substringAfterLast(".")}"
                    newName.substringBeforeLast(".")
                } else {
                    newName
                }

                newPath = "${path.getParentPath()}/$name~$currentIndex$extension"
            }

            return newPath
        } catch (e: Exception) {
            return null
        }
    }

    private fun renameAllFiles(
        paths: List<String>,
        useMediaFileExtension: Boolean,
        android30Format: Android30RenameFormat,
        callback: (success: Boolean) -> Unit,
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
                if (success) {
                    try {
                        resolved.forEach { resolvedUri ->
                            val path = resolvedUri.fileDirItem.path
                            val uri = resolvedUri.uri
                            val newFileName = getNewPath(path, useMediaFileExtension)?.getFilenameFromPath() ?: return@forEach
                            when (android30Format) {
                                Android30RenameFormat.SAF -> {
                                    val sourceFile = File(path).toFileDirItem(context)
                                    val newPath = "${path.getParentPath()}/$newFileName"
                                    val destinationFile = FileDirItem(
                                        newPath,
                                        newFileName,
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
                                        put(MediaStore.Images.Media.DISPLAY_NAME, newFileName)
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
                } else {
                    callback(false)
                }
            }
        }
    }
}
