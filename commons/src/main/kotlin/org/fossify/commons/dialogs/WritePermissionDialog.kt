package org.fossify.commons.dialogs

import android.app.Activity
import android.text.Html
import android.text.Spanned
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import org.fossify.commons.R
import org.fossify.commons.activities.BaseSimpleActivity
import org.fossify.commons.compose.alert_dialog.AlertDialogState
import org.fossify.commons.compose.alert_dialog.DialogSurface
import org.fossify.commons.compose.alert_dialog.dialogTextColor
import org.fossify.commons.compose.alert_dialog.rememberAlertDialogState
import org.fossify.commons.compose.components.LinkifyTextComponent
import org.fossify.commons.compose.extensions.MyDevices
import org.fossify.commons.compose.extensions.andThen
import org.fossify.commons.compose.theme.AppThemeSurface
import org.fossify.commons.compose.theme.SimpleTheme
import org.fossify.commons.databinding.DialogWritePermissionBinding
import org.fossify.commons.databinding.DialogWritePermissionOtgBinding
import org.fossify.commons.extensions.fromHtml
import org.fossify.commons.extensions.getAlertDialogBuilder
import org.fossify.commons.extensions.humanizePath
import org.fossify.commons.extensions.setupDialogStuff

class WritePermissionDialog(activity: Activity, val writePermissionDialogMode: WritePermissionDialogMode, val callback: () -> Unit) {

    @Immutable
    sealed class WritePermissionDialogMode {
        @Immutable
        data object Otg : WritePermissionDialogMode()

        @Immutable
        data object SdCard : WritePermissionDialogMode()

        @Immutable
        data class OpenDocumentTreeSDK30(val path: String) : WritePermissionDialogMode()

        @Immutable
        data object CreateDocumentSDK30 : WritePermissionDialogMode()
    }

    private var dialog: AlertDialog? = null

    init {
        val sdCardView = DialogWritePermissionBinding.inflate(activity.layoutInflater, null, false)
        val otgView = DialogWritePermissionOtgBinding.inflate(
            activity.layoutInflater,
            null,
            false
        )

        var dialogTitle = R.string.confirm_storage_access_title

        val glide = Glide.with(activity)
        val crossFade = DrawableTransitionOptions.withCrossFade()
        when (writePermissionDialogMode) {
            WritePermissionDialogMode.Otg -> {
                otgView.writePermissionsDialogOtgText.setText(R.string.confirm_usb_storage_access_text)
                glide.load(R.drawable.img_write_storage_otg).transition(crossFade).into(otgView.writePermissionsDialogOtgImage)
            }

            WritePermissionDialogMode.SdCard -> {
                glide.load(R.drawable.img_write_storage).transition(crossFade).into(sdCardView.writePermissionsDialogImage)
                glide.load(R.drawable.img_write_storage_sd).transition(crossFade).into(sdCardView.writePermissionsDialogImageSd)
            }

            is WritePermissionDialogMode.OpenDocumentTreeSDK30 -> {
                dialogTitle = R.string.confirm_folder_access_title
                val humanizedPath = activity.humanizePath(writePermissionDialogMode.path)
                otgView.writePermissionsDialogOtgText.text =
                    Html.fromHtml(activity.getString(R.string.confirm_storage_access_android_text_specific, humanizedPath))
                glide.load(R.drawable.img_write_storage_sdk_30).transition(crossFade).into(otgView.writePermissionsDialogOtgImage)

                otgView.writePermissionsDialogOtgImage.setOnClickListener {
                    dialogConfirmed()
                }
            }

            WritePermissionDialogMode.CreateDocumentSDK30 -> {
                dialogTitle = R.string.confirm_folder_access_title
                otgView.writePermissionsDialogOtgText.text = Html.fromHtml(activity.getString(R.string.confirm_create_doc_for_new_folder_text))
                glide.load(R.drawable.img_write_storage_create_doc_sdk_30).transition(crossFade).into(otgView.writePermissionsDialogOtgImage)

                otgView.writePermissionsDialogOtgImage.setOnClickListener {
                    dialogConfirmed()
                }
            }
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { _, _ -> dialogConfirmed() }
            .setOnCancelListener {
                BaseSimpleActivity.funAfterSAFPermission?.invoke(false)
                BaseSimpleActivity.funAfterSAFPermission = null
            }
            .apply {
                activity.setupDialogStuff(
                    if (writePermissionDialogMode == WritePermissionDialogMode.SdCard) sdCardView.root else otgView.root,
                    this,
                    dialogTitle
                ) { alertDialog ->
                    dialog = alertDialog
                }
            }
    }

    private fun dialogConfirmed() {
        dialog?.dismiss()
        callback()
    }
}

@Composable
fun WritePermissionAlertDialog(
    alertDialogState: AlertDialogState,
    writePermissionDialogMode: WritePermissionDialog.WritePermissionDialogMode,
    modifier: Modifier = Modifier,
    callback: () -> Unit,
    onCancelCallback: () -> Unit
) {
    val dialogTitle = remember {
        adjustDialogTitle(
            writePermissionDialogMode = writePermissionDialogMode,
            dialogTitle = R.string.confirm_storage_access_title
        )
    }
    AlertDialog(
        onDismissRequest = alertDialogState::hide andThen onCancelCallback,
        modifier = modifier
            .fillMaxWidth(0.9f),
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        DialogSurface {
            Box {
                Column(
                    modifier = modifier
                        .padding(bottom = 64.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = stringResource(id = dialogTitle),
                        color = dialogTextColor,
                        modifier = Modifier
                            .padding(horizontal = SimpleTheme.dimens.padding.extraLarge.plus(SimpleTheme.dimens.padding.large))
                            .padding(top = SimpleTheme.dimens.padding.extraLarge.plus(SimpleTheme.dimens.padding.small)),
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    when (writePermissionDialogMode) {
                        WritePermissionDialog.WritePermissionDialogMode.CreateDocumentSDK30 -> CreateDocumentSDK30(
                            onImageClick = alertDialogState::hide andThen callback
                        )

                        is WritePermissionDialog.WritePermissionDialogMode.OpenDocumentTreeSDK30 -> OpenDocumentTreeSDK30(
                            onImageClick = alertDialogState::hide andThen callback,
                            path = writePermissionDialogMode.path
                        )

                        WritePermissionDialog.WritePermissionDialogMode.Otg -> OTG()
                        WritePermissionDialog.WritePermissionDialogMode.SdCard -> SDCard()
                    }
                    Spacer(Modifier.padding(vertical = SimpleTheme.dimens.padding.extraLarge))
                }

                TextButton(
                    onClick = alertDialogState::hide andThen callback,
                    modifier = Modifier
                        .padding(
                            top = SimpleTheme.dimens.padding.extraLarge,
                            bottom = SimpleTheme.dimens.padding.extraLarge,
                            end = SimpleTheme.dimens.padding.extraLarge
                        )
                        .align(Alignment.BottomEnd)
                ) {
                    Text(text = stringResource(id = R.string.ok))
                }
            }
        }
    }
}

@Composable
private fun CreateDocumentSDK30(onImageClick: () -> Unit) {
    WritePermissionText(stringResource(R.string.confirm_create_doc_for_new_folder_text).fromHtml())
    WritePermissionImage(
        drawable = R.drawable.img_write_storage_create_doc_sdk_30,
        modifier = Modifier.clickable(onClick = onImageClick)
    )
}

@Composable
private fun OpenDocumentTreeSDK30(onImageClick: () -> Unit, path: String) {
    val context = LocalContext.current
    val view = LocalView.current

    val humanizedPath = remember { if (!view.isInEditMode) context.humanizePath(path) else "" }
    WritePermissionText(stringResource(R.string.confirm_storage_access_android_text_specific, humanizedPath).fromHtml())
    WritePermissionImage(
        drawable = R.drawable.img_write_storage_sdk_30,
        modifier = Modifier.clickable(onClick = onImageClick)
    )
}

@Composable
private fun SDCard() {
    WritePermissionText(R.string.confirm_storage_access_text)
    WritePermissionImage(drawable = R.drawable.img_write_storage)
    WritePermissionText(R.string.confirm_storage_access_text_sd)
    WritePermissionImage(drawable = R.drawable.img_write_storage_sd)
}

@Composable
private fun OTG() {
    WritePermissionText(R.string.confirm_usb_storage_access_text)
    WritePermissionImage(drawable = R.drawable.img_write_storage_otg)
}

@Composable
private fun WritePermissionImage(
    modifier: Modifier = Modifier,
    @DrawableRes drawable: Int
) {
    Image(
        modifier = modifier
            .padding(horizontal = SimpleTheme.dimens.padding.extraLarge.plus(SimpleTheme.dimens.padding.large)),
        painter = painterResource(id = drawable),
        contentDescription = null,
        contentScale = ContentScale.Fit,
    )
}

@Composable
private fun WritePermissionText(@StringRes text: Int) {
    Text(
        text = stringResource(id = text),
        color = dialogTextColor,
        modifier = Modifier
            .padding(horizontal = SimpleTheme.dimens.padding.extraLarge.plus(SimpleTheme.dimens.padding.medium))
            .padding(vertical = SimpleTheme.dimens.padding.extraLarge),
    )
}

@Composable
private fun WritePermissionText(text: Spanned) {
    LinkifyTextComponent(
        modifier = Modifier
            .padding(horizontal = SimpleTheme.dimens.padding.extraLarge.plus(SimpleTheme.dimens.padding.medium))
            .padding(vertical = SimpleTheme.dimens.padding.extraLarge),
    ) {
        text
    }
}

private fun adjustDialogTitle(
    writePermissionDialogMode: WritePermissionDialog.WritePermissionDialogMode,
    dialogTitle: Int
): Int =
    when (writePermissionDialogMode) {
        WritePermissionDialog.WritePermissionDialogMode.CreateDocumentSDK30 -> R.string.confirm_folder_access_title
        is WritePermissionDialog.WritePermissionDialogMode.OpenDocumentTreeSDK30 -> R.string.confirm_folder_access_title
        else -> dialogTitle
    }

private class WritePermissionDialogModePreviewParameter : PreviewParameterProvider<WritePermissionDialog.WritePermissionDialogMode> {
    override val values: Sequence<WritePermissionDialog.WritePermissionDialogMode>
        get() = sequenceOf(
            WritePermissionDialog.WritePermissionDialogMode.SdCard,
            WritePermissionDialog.WritePermissionDialogMode.Otg,
            WritePermissionDialog.WritePermissionDialogMode.CreateDocumentSDK30,
            WritePermissionDialog.WritePermissionDialogMode.OpenDocumentTreeSDK30(""),
        )
}

@Composable
@MyDevices
private fun WritePermissionAlertDialogPreview(@PreviewParameter(WritePermissionDialogModePreviewParameter::class) mode: WritePermissionDialog.WritePermissionDialogMode) {
    AppThemeSurface {
        WritePermissionAlertDialog(
            alertDialogState = rememberAlertDialogState(),
            writePermissionDialogMode = WritePermissionDialog.WritePermissionDialogMode.OpenDocumentTreeSDK30("."),
            callback = {}
        ) {}
    }
}
