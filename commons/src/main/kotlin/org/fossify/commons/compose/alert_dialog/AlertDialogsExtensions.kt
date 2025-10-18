package org.fossify.commons.compose.alert_dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.android.awaitFrame
import org.fossify.commons.R
import org.fossify.commons.compose.theme.LocalTheme
import org.fossify.commons.compose.theme.Shapes
import org.fossify.commons.compose.theme.SimpleTheme
import org.fossify.commons.compose.theme.light_grey_stroke
import org.fossify.commons.compose.theme.model.Theme
import org.fossify.commons.extensions.baseConfig
import org.fossify.commons.helpers.isSPlus

val dialogContainerColor
    @ReadOnlyComposable
    @Composable get() = when (LocalTheme.current) {
        is Theme.BlackAndWhite -> Color.Black
        is Theme.SystemDefaultMaterialYou -> if (isSPlus()) colorResource(R.color.you_dialog_background_color) else SimpleTheme.colorScheme.surface
        else -> {
            val context = LocalContext.current
            Color(context.baseConfig.backgroundColor)
        }
    }

@Composable
@ReadOnlyComposable
fun Modifier.dialogBackgroundShapeAndBorder(): Modifier = this
    .fillMaxWidth()
    .background(dialogContainerColor, dialogShape)
    .dialogBorder()

val dialogShape = Shapes.extraLarge

val dialogElevation = 0.dp

val dialogTextColor @Composable @ReadOnlyComposable get() = SimpleTheme.colorScheme.onSurface

@Composable
@ReadOnlyComposable
fun Modifier.dialogBorder(): Modifier =
    when (LocalTheme.current) {
        is Theme.BlackAndWhite -> this.border(1.dp, light_grey_stroke, dialogShape)
        else -> this
    }

@Composable
fun DialogSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .dialogBorder(),
        shape = dialogShape,
        color = dialogContainerColor,
        tonalElevation = dialogElevation,
    ) {
        content()
    }
}

@Composable
fun ShowKeyboardWhenDialogIsOpenedAndRequestFocus(
    keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current,
    focusRequester: FocusRequester?
) {
    LaunchedEffect(Unit) {
        //await two frames to render the scrim and the dialog
        awaitFrame()
        awaitFrame()
        keyboardController?.show()
        focusRequester?.requestFocus()
    }
}
