package org.fossify.commons.samples.activities

import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toImmutableList
import org.fossify.commons.R
import org.fossify.commons.compose.alert_dialog.AlertDialogState
import org.fossify.commons.compose.alert_dialog.rememberAlertDialogState
import org.fossify.commons.compose.bottom_sheet.BottomSheetDialogState
import org.fossify.commons.compose.bottom_sheet.rememberBottomSheetDialogState
import org.fossify.commons.compose.extensions.config
import org.fossify.commons.compose.theme.AppThemeSurface
import org.fossify.commons.dialogs.AddOrEditBlockedNumberAlertDialog
import org.fossify.commons.dialogs.AppSideLoadedAlertDialog
import org.fossify.commons.dialogs.CallConfirmationAlertDialog
import org.fossify.commons.dialogs.ChangeDateTimeFormatAlertDialog
import org.fossify.commons.dialogs.ChangeViewTypeAlertDialog
import org.fossify.commons.dialogs.ChooserBottomSheetDialog
import org.fossify.commons.dialogs.ColorPickerAlertDialog
import org.fossify.commons.dialogs.ConfirmationAdvancedAlertDialog
import org.fossify.commons.dialogs.ConfirmationAlertDialog
import org.fossify.commons.dialogs.CreateNewFolderAlertDialog
import org.fossify.commons.dialogs.CustomIntervalPickerAlertDialog
import org.fossify.commons.dialogs.DonateAlertDialog
import org.fossify.commons.dialogs.EnterPasswordAlertDialog
import org.fossify.commons.dialogs.FileConflictAlertDialog
import org.fossify.commons.dialogs.FolderLockingNoticeAlertDialog
import org.fossify.commons.dialogs.LineColorPickerAlertDialog
import org.fossify.commons.dialogs.OpenDeviceSettingsAlertDialog
import org.fossify.commons.dialogs.PermissionRequiredAlertDialog
import org.fossify.commons.dialogs.PurchaseThankYouAlertDialog
import org.fossify.commons.dialogs.RadioGroupAlertDialog
import org.fossify.commons.dialogs.WhatsNewAlertDialog
import org.fossify.commons.dialogs.WritePermissionAlertDialog
import org.fossify.commons.dialogs.WritePermissionDialog
import org.fossify.commons.extensions.baseConfig
import org.fossify.commons.extensions.toHex
import org.fossify.commons.extensions.toast
import org.fossify.commons.models.FileDirItem
import org.fossify.commons.models.RadioItem
import org.fossify.commons.models.Release
import org.fossify.commons.models.SimpleListItem
import org.fossify.commons.models.asReadOnly

class TestDialogActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppThemeSurface {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.padding(top = 16.dp))
                    ShowButton(getAppSideLoadedDialogState(), text = "App side loaded")
                    ShowButton(getAddBlockedNumberDialogState(), text = "Add blocked number")
                    ShowButton(getConfirmationAlertDialogState(), text = "Confirmation normal")
                    ShowButton(getConfirmationAdvancedAlertDialogState(), text = "Confirmation advanced")
                    ShowButton(getPermissionRequiredAlertDialogState(), text = "Permission required")
                    ShowButton(getDonateAlertDialogState(), text = "Donate")
                    ShowButton(getPurchaseThankYouAlertDialogState(), text = "Purchase thank you")
                    ShowButton(getLineColorPickerAlertDialogState(), text = "Line color picker")
                    ShowButton(getOpenDeviceSettingsAlertDialogState(), text = "Open device settings")
                    ShowButton(getColorPickerAlertDialogState(), text = "Color picker")
                    ShowButton(getCallConfirmationAlertDialogState(), text = "Call confirmation")
                    ShowButton(getChangeDateTimeFormatAlertDialogState(), text = "Change date time")
                    ShowButton(getRadioGroupDialogAlertDialogState(), text = "Radio group")
                    ShowButton(getWhatsNewAlertDialogState(), text = "What's new")
                    ShowButton(getChangeViewTypeAlertDialogState(), text = "Change view type")
                    ShowButton(getWritePermissionAlertDialogState(), text = "Write permission")
                    ShowButton(getCreateNewFolderAlertDialogState(), text = "Create new folder")
                    ShowButton(getEnterPasswordAlertDialogState(), text = "Enter password")
                    ShowButton(getFolderLockingNoticeAlertDialogState(), text = "Folder locking notice")
                    ShowButton(getChooserBottomSheetDialogState(), text = "Bottom sheet chooser")
                    ShowButton(getFileConflictAlertDialogState(), text = "File conflict")
                    ShowButton(getCustomIntervalPickerAlertDialogState(), text = "Custom interval picker")
                    Spacer(modifier = Modifier.padding(bottom = 16.dp))
                }
            }
        }
    }

    @Composable
    private fun getCustomIntervalPickerAlertDialogState() = rememberAlertDialogState().apply {
        DialogMember {
            CustomIntervalPickerAlertDialog(alertDialogState = this, selectedSeconds = 3, showSeconds = true) {
                Log.d("CustomIntervalPickerAlertDialog", it.toString())
            }
        }
    }

    @Composable
    private fun getFileConflictAlertDialogState() = rememberAlertDialogState().apply {
        DialogMember {
            FileConflictAlertDialog(
                alertDialogState = this, fileDirItem = FileDirItem(
                    path = filesDir.path,
                    name = "Test", children = 2
                ).asReadOnly(), showApplyToAll = false
            ) { resolution, applyForAll ->
                baseConfig.apply {
                    lastConflictApplyToAll = applyForAll
                    lastConflictResolution = resolution
                }
            }
        }
    }

    @Composable
    private fun getChooserBottomSheetDialogState() = rememberBottomSheetDialogState().apply {
        BottomSheetContent {
            val list = remember {
                listOf(
                    SimpleListItem(1, R.string.record_video, R.drawable.ic_camera_vector),
                    SimpleListItem(2, R.string.record_audio, R.drawable.ic_microphone_vector, selected = true),
                    SimpleListItem(4, R.string.choose_contact, R.drawable.ic_add_person_vector)
                ).toImmutableList()
            }
            ChooserBottomSheetDialog(
                bottomSheetDialogState = this@apply,
                items = list
            ) {
                toast("Selected ${getString(it.textRes)}")
            }
        }
    }

    @Composable
    private fun getFolderLockingNoticeAlertDialogState() = rememberAlertDialogState().apply {
        DialogMember {
            FolderLockingNoticeAlertDialog(alertDialogState = this) {

            }
        }
    }

    @Composable
    private fun getEnterPasswordAlertDialogState() = rememberAlertDialogState().apply {
        DialogMember {
            EnterPasswordAlertDialog(alertDialogState = this, callback = {}, cancelCallback = {})
        }
    }

    @Composable
    private fun getCreateNewFolderAlertDialogState() = rememberAlertDialogState().apply {
        DialogMember {
            CreateNewFolderAlertDialog(this, path = Environment.getExternalStorageDirectory().toString()) {
                //todo create helper for private fun createFolder(path: String, alertDialog: AlertDialog) to extract bundled logic
            }
        }
    }

    @Composable
    private fun getWritePermissionAlertDialogState() = rememberAlertDialogState().apply {
        DialogMember {
            WritePermissionAlertDialog(
                alertDialogState = this,
                writePermissionDialogMode = WritePermissionDialog.WritePermissionDialogMode.OpenDocumentTreeSDK30("."),
                callback = {}
            ) {}
        }
    }

    @Composable
    private fun getChangeViewTypeAlertDialogState() = rememberAlertDialogState().apply {
        DialogMember {
            val currentViewType = config.viewType
            ChangeViewTypeAlertDialog(alertDialogState = this, selectedViewType = currentViewType) { type ->
                Log.d("ChangeViewTypeAlertDialog", type.toString())
                config.viewType = type
            }
        }
    }

    @Composable
    private fun getWhatsNewAlertDialogState(): AlertDialogState = rememberAlertDialogState().apply {
        DialogMember {
            val releases = remember {
                listOf(
                    Release(14, R.string.temporarily_show_excluded),
                    Release(3, R.string.temporarily_show_hidden)
                ).toImmutableList()
            }
            WhatsNewAlertDialog(
                alertDialogState = this, releases = releases
            )
        }
    }

    @Composable
    private fun getRadioGroupDialogAlertDialogState() = rememberAlertDialogState().apply {
        DialogMember {
            RadioGroupAlertDialog(
                alertDialogState = this,
                items = listOf(
                    RadioItem(1, "Test"),
                    RadioItem(2, "Test 2"),
                    RadioItem(3, "Test 3"),
                    RadioItem(4, "Test 4"),
                    RadioItem(5, "Test 5"),
                    RadioItem(6, "Test 6"),
                    RadioItem(6, "Test 7"),
                ).toImmutableList(),
                selectedItemId = 2,
                titleId = R.string.title,
                showOKButton = true,
                cancelCallback = {
                    Log.d("getRadioGroupDialogAlertDialogState", "cancelCallback")
                }
            ) {
                Log.d("getRadioGroupDialogAlertDialogState", "Selected $it")
            }
        }
    }

    @Composable
    private fun getChangeDateTimeFormatAlertDialogState() = rememberAlertDialogState().apply {
        DialogMember {
            ChangeDateTimeFormatAlertDialog(this, is24HourChecked = baseConfig.use24HourFormat) { selectedFormat, is24HourChecked ->
                baseConfig.dateFormat = selectedFormat
                baseConfig.use24HourFormat = is24HourChecked
            }
        }
    }

    @Composable
    private fun getLineColorPickerAlertDialogState() = rememberAlertDialogState().apply {
        DialogMember {
            LineColorPickerAlertDialog(
                alertDialogState = this,
                color = baseConfig.customPrimaryColor,
                isPrimaryColorPicker = true,
                onActiveColorChange = { color ->
                    Log.d("getLineColorPickerAlertDialogState", "onActiveColorChange=${color.toHex()}")
                }) { wasPositivePressed, color ->
                Log.d("getLineColorPickerAlertDialogState", "wasPositivePressed=$wasPositivePressed color=${color.toHex()}")
            }
        }
    }

    @Composable
    private fun getOpenDeviceSettingsAlertDialogState() = rememberAlertDialogState().apply {
        DialogMember {
            OpenDeviceSettingsAlertDialog(
                alertDialogState = this,
                message = "Test message"
            )
        }
    }

    @Composable
    private fun getColorPickerAlertDialogState() = rememberAlertDialogState().apply {
        DialogMember {
            ColorPickerAlertDialog(
                alertDialogState = this,
                color = config.customTextColor,
                removeDimmedBackground = true,
                onActiveColorChange = { color ->
                    Log.d("getColorPickerAlertDialogState", "onActiveColorChange=${color.toHex()}")
                }) { wasPositivePressed, color ->
                Log.d("getColorPickerAlertDialogState", "wasPositivePressed=$wasPositivePressed color=${color.toHex()}")
            }
        }
    }

    @Composable
    private fun getPurchaseThankYouAlertDialogState() = rememberAlertDialogState().apply {
        DialogMember {
            PurchaseThankYouAlertDialog(alertDialogState = this)
        }
    }

    @Composable
    private fun getCallConfirmationAlertDialogState() = rememberAlertDialogState().apply {
        DialogMember {
            CallConfirmationAlertDialog(alertDialogState = this, callee = "Fossify") {}
        }
    }

    @Composable
    private fun getDonateAlertDialogState() =
        rememberAlertDialogState().apply {
            DialogMember {
                DonateAlertDialog(alertDialogState = this)
            }
        }


    @Composable
    private fun getConfirmationAlertDialogState() = rememberAlertDialogState().apply {
        DialogMember {
            ConfirmationAlertDialog(alertDialogState = this, dialogTitle = "Some fancy title") {}
        }
    }

    @Composable
    private fun getAppSideLoadedDialogState() =
        rememberAlertDialogState().apply {
            DialogMember {
                AppSideLoadedAlertDialog(alertDialogState = this, onDownloadClick = {}) {}
            }
        }

    @Composable
    private fun getAddBlockedNumberDialogState() =
        rememberAlertDialogState().apply {
            DialogMember {
                AddOrEditBlockedNumberAlertDialog(alertDialogState = this, blockedNumber = null, deleteBlockedNumber = {}) {}
            }
        }

    @Composable
    private fun getConfirmationAdvancedAlertDialogState() =
        rememberAlertDialogState().apply {
            DialogMember {
                ConfirmationAdvancedAlertDialog(alertDialogState = this) {}
            }
        }

    @Composable
    private fun getPermissionRequiredAlertDialogState() =
        rememberAlertDialogState().apply {
            DialogMember {
                PermissionRequiredAlertDialog(
                    alertDialogState = this,
                    text = "Test permission"
                ) {}
            }
        }

    @Composable
    private fun ShowButton(alertDialogState: AlertDialogState, text: String) {
        Button(onClick = alertDialogState::show) {
            Text(
                text = text,
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }

    @Composable
    private fun ShowButton(appSideLoadedDialogState: BottomSheetDialogState, text: String) {
        Button(onClick = appSideLoadedDialogState::open) {
            Text(
                text = text,
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }


}
