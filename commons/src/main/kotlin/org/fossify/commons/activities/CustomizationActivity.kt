package org.fossify.commons.activities

import android.content.ContentValues
import android.graphics.Color
import android.os.Bundle
import org.fossify.commons.R
import org.fossify.commons.databinding.ActivityCustomizationBinding
import org.fossify.commons.dialogs.ColorPickerDialog
import org.fossify.commons.dialogs.ConfirmationAdvancedDialog
import org.fossify.commons.dialogs.ConfirmationDialog
import org.fossify.commons.dialogs.LineColorPickerDialog
import org.fossify.commons.dialogs.PurchaseThankYouDialog
import org.fossify.commons.dialogs.RadioGroupDialog
import org.fossify.commons.extensions.baseConfig
import org.fossify.commons.extensions.beVisibleIf
import org.fossify.commons.extensions.canAccessGlobalConfig
import org.fossify.commons.extensions.checkAppIconColor
import org.fossify.commons.extensions.getColoredMaterialStatusBarColor
import org.fossify.commons.extensions.getContrastColor
import org.fossify.commons.extensions.getProperPrimaryColor
import org.fossify.commons.extensions.getProperTextColor
import org.fossify.commons.extensions.getThemeId
import org.fossify.commons.extensions.isDynamicTheme
import org.fossify.commons.extensions.isSystemInDarkMode
import org.fossify.commons.extensions.isThankYouInstalled
import org.fossify.commons.extensions.setFillWithStroke
import org.fossify.commons.extensions.toast
import org.fossify.commons.extensions.updateGlobalConfig
import org.fossify.commons.extensions.value
import org.fossify.commons.extensions.viewBinding
import org.fossify.commons.extensions.withGlobalConfig
import org.fossify.commons.helpers.APP_ICON_IDS
import org.fossify.commons.helpers.APP_LAUNCHER_NAME
import org.fossify.commons.helpers.DARK_GREY
import org.fossify.commons.helpers.MyContentProvider.COL_ACCENT_COLOR
import org.fossify.commons.helpers.MyContentProvider.COL_APP_ICON_COLOR
import org.fossify.commons.helpers.MyContentProvider.COL_BACKGROUND_COLOR
import org.fossify.commons.helpers.MyContentProvider.COL_PRIMARY_COLOR
import org.fossify.commons.helpers.MyContentProvider.COL_TEXT_COLOR
import org.fossify.commons.helpers.MyContentProvider.COL_THEME_TYPE
import org.fossify.commons.helpers.MyContentProvider.GLOBAL_THEME_CUSTOM
import org.fossify.commons.helpers.MyContentProvider.GLOBAL_THEME_DISABLED
import org.fossify.commons.helpers.MyContentProvider.GLOBAL_THEME_SYSTEM
import org.fossify.commons.helpers.NavigationIcon
import org.fossify.commons.helpers.SAVE_DISCARD_PROMPT_INTERVAL
import org.fossify.commons.helpers.isSPlus
import org.fossify.commons.models.GlobalConfig
import org.fossify.commons.models.MyTheme
import org.fossify.commons.models.RadioItem
import org.fossify.commons.models.isGlobalThemingEnabled
import kotlin.math.abs

class CustomizationActivity : BaseSimpleActivity() {
    companion object {
        private const val THEME_LIGHT = 0
        private const val THEME_DARK = 1
        private const val THEME_SOLARIZED = 2
        private const val THEME_DARK_RED = 3
        private const val THEME_BLACK_WHITE = 4
        private const val THEME_CUSTOM = 5
        private const val THEME_WHITE = 6
        private const val THEME_SYSTEM = 7
    }

    private var curTextColor = 0
    private var curBackgroundColor = 0
    private var curPrimaryColor = 0
    private var curAccentColor = 0
    private var curAppIconColor = 0
    private var curSelectedThemeId = 0
    private var originalAppIconColor = 0
    private var lastSavePromptTS = 0L
    private var hasUnsavedChanges = false
    private val predefinedThemes = LinkedHashMap<Int, MyTheme>()
    private var curPrimaryLineColorPicker: LineColorPickerDialog? = null
    private var globalConfig: GlobalConfig? = null

    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    override fun getRepositoryName() = null

    private val binding by viewBinding(ActivityCustomizationBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupOptionsMenu()
        refreshMenuItems()
        updateMaterialActivityViews(
            mainCoordinatorLayout = binding.customizationCoordinator,
            nestedView = binding.customizationHolder,
            useTransparentNavigation = true,
            useTopSearchMenu = false
        )

        initColorVariables()
        if (canAccessGlobalConfig()) {
            withGlobalConfig {
                globalConfig = it
                baseConfig.isGlobalThemeEnabled = it.isGlobalThemingEnabled()
                runOnUiThread {
                    setupThemes()
                    showOrHideThankYouFeatures()
                }
            }
        } else {
            setupThemes()
            baseConfig.isGlobalThemeEnabled = false
        }

        showOrHideThankYouFeatures()
        originalAppIconColor = baseConfig.appIconColor
        updateLabelColors()
        updateHeaderColors()
    }

    override fun onResume() {
        super.onResume()
        setTheme(getThemeId(getCurrentPrimaryColor()))

        if (!isDynamicTheme()) {
            updateBackgroundColor(getCurrentBackgroundColor())
            updateActionbarColor(getCurrentTopBarColor())
        }

        curPrimaryLineColorPicker?.getSpecificColor()?.apply {
            updateActionbarColor(this)
            setTheme(getThemeId(this))
        }

        setupToolbar(
            toolbar = binding.customizationToolbar,
            toolbarNavigationIcon = NavigationIcon.Cross,
            statusBarColor = getColoredMaterialStatusBarColor()
        )
        showOrHideThankYouFeatures()
        updateApplyToAllColors()
    }

    private fun refreshMenuItems() {
        binding.customizationToolbar.menu.findItem(R.id.save).isVisible = hasUnsavedChanges
    }

    private fun setupOptionsMenu() {
        binding.customizationToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.save -> {
                    saveChanges(true)
                    true
                }

                else -> false
            }
        }
    }

    override fun onBackPressed() {
        if (hasUnsavedChanges && System.currentTimeMillis() - lastSavePromptTS > SAVE_DISCARD_PROMPT_INTERVAL) {
            promptSaveDiscard()
        } else {
            super.onBackPressed()
        }
    }

    private fun setupThemes() {
        predefinedThemes.apply {
            put(
                THEME_SYSTEM,
                if (isSPlus()) {
                    getSystemThemeColors()
                } else {
                    getAutoThemeColors()
                }
            )
            put(
                THEME_LIGHT,
                MyTheme(
                    labelId = R.string.light_theme,
                    textColorId = R.color.theme_light_text_color,
                    backgroundColorId = R.color.theme_light_background_color,
                    primaryColorId = R.color.color_primary,
                    appIconColorId = R.color.color_primary
                )
            )
            put(
                THEME_DARK,
                MyTheme(
                    labelId = R.string.dark_theme,
                    textColorId = R.color.theme_dark_text_color,
                    backgroundColorId = R.color.theme_dark_background_color,
                    primaryColorId = R.color.color_primary,
                    appIconColorId = R.color.color_primary
                )
            )
            put(
                THEME_DARK_RED,
                MyTheme(
                    labelId = R.string.dark_red,
                    textColorId = R.color.theme_dark_text_color,
                    backgroundColorId = R.color.theme_dark_background_color,
                    primaryColorId = R.color.theme_dark_red_primary_color,
                    appIconColorId = R.color.md_red_700
                )
            )
            put(
                THEME_WHITE,
                MyTheme(
                    labelId = R.string.white,
                    textColorId = R.color.dark_grey,
                    backgroundColorId = android.R.color.white,
                    primaryColorId = android.R.color.white,
                    appIconColorId = R.color.color_primary
                )
            )
            put(
                THEME_BLACK_WHITE,
                MyTheme(
                    labelId = R.string.black_white,
                    textColorId = android.R.color.white,
                    backgroundColorId = android.R.color.black,
                    primaryColorId = android.R.color.black,
                    appIconColorId = R.color.md_grey_black
                )
            )
            put(THEME_CUSTOM, MyTheme(R.string.custom, 0, 0, 0, 0))
        }

        setupThemePicker()
        setupColorsPickers()
    }

    private fun setupThemePicker() {
        curSelectedThemeId = getCurrentThemeId()
        binding.customizationTheme.text = getThemeText()
        updateAutoThemeFields()
        handleAccentColorLayout()
        binding.customizationThemeHolder.setOnClickListener {
            if (baseConfig.wasAppIconCustomizationWarningShown) {
                themePickerClicked()
            } else {
                ConfirmationDialog(
                    activity = this,
                    message = "",
                    messageId = R.string.app_icon_color_warning,
                    positive = R.string.ok,
                    negative = 0
                ) {
                    baseConfig.wasAppIconCustomizationWarningShown = true
                    themePickerClicked()
                }
            }
        }
    }

    private fun themePickerClicked() {
        val items = arrayListOf<RadioItem>()
        for ((key, value) in predefinedThemes) {
            items.add(RadioItem(key, getString(value.labelId)))
        }

        RadioGroupDialog(this@CustomizationActivity, items, curSelectedThemeId) {
            updateColorTheme(it as Int, true)
            if (
                it != THEME_CUSTOM
                && it != THEME_SYSTEM
                && !baseConfig.wasCustomThemeSwitchDescriptionShown
            ) {
                baseConfig.wasCustomThemeSwitchDescriptionShown = true
                toast(R.string.changing_color_description)
            }

            updateMenuItemColors(binding.customizationToolbar.menu, getCurrentTopBarColor())
            setupToolbar(
                toolbar = binding.customizationToolbar,
                toolbarNavigationIcon = NavigationIcon.Cross,
                statusBarColor = getCurrentTopBarColor()
            )
        }
    }

    private fun updateColorTheme(themeId: Int, useStored: Boolean = false) {
        curSelectedThemeId = themeId
        binding.customizationTheme.text = getThemeText()

        if (curSelectedThemeId == THEME_CUSTOM) {
            if (useStored) {
                curTextColor = baseConfig.customTextColor
                curBackgroundColor = baseConfig.customBackgroundColor
                curPrimaryColor = baseConfig.customPrimaryColor
                curAccentColor = baseConfig.customAccentColor
                curAppIconColor = baseConfig.customAppIconColor
                setTheme(getThemeId(curPrimaryColor))
                updateMenuItemColors(binding.customizationToolbar.menu, curPrimaryColor)
                setupToolbar(binding.customizationToolbar, NavigationIcon.Cross, curPrimaryColor)
                setupColorsPickers()
            } else {
                baseConfig.customPrimaryColor = curPrimaryColor
                baseConfig.customAccentColor = curAccentColor
                baseConfig.customBackgroundColor = curBackgroundColor
                baseConfig.customTextColor = curTextColor
                baseConfig.customAppIconColor = curAppIconColor
            }
        } else {
            val theme = predefinedThemes[curSelectedThemeId]!!
            curTextColor = getColor(theme.textColorId)
            curBackgroundColor = getColor(theme.backgroundColorId)

            if (curSelectedThemeId != THEME_SYSTEM) {
                curPrimaryColor = getColor(theme.primaryColorId)
                curAppIconColor = getColor(theme.appIconColorId)
                if (curAccentColor == 0) {
                    curAccentColor = getColor(R.color.color_primary)
                }
            }

            setTheme(getThemeId(getCurrentPrimaryColor()))
            colorChanged()
            updateMenuItemColors(binding.customizationToolbar.menu, getCurrentTopBarColor())
            setupToolbar(
                toolbar = binding.customizationToolbar,
                toolbarNavigationIcon = NavigationIcon.Cross,
                statusBarColor = getCurrentTopBarColor()
            )
        }

        hasUnsavedChanges = true
        refreshMenuItems()
        updateLabelColors(getCurrentTextColor())
        updateHeaderColors(getCurrentAccentOrPrimaryColor())
        updateBackgroundColor(getCurrentBackgroundColor())
        updateActionbarColor(getCurrentTopBarColor())
        updateAutoThemeFields()
        updateApplyToAllColors()
        handleAccentColorLayout()
    }

    private fun getAutoThemeColors(): MyTheme {
        val isDarkTheme = isSystemInDarkMode()
        val textColor = if (isDarkTheme) {
            R.color.theme_dark_text_color
        } else {
            R.color.theme_light_text_color
        }
        val backgroundColor = if (isDarkTheme) {
            R.color.theme_dark_background_color
        } else {
            R.color.theme_light_background_color
        }
        return MyTheme(
            labelId = R.string.auto_light_dark_theme,
            textColorId = textColor,
            backgroundColorId = backgroundColor,
            primaryColorId = R.color.color_primary,
            appIconColorId = R.color.color_primary
        )
    }

    // doesn't really matter what colors we use here, everything will be taken from the system.
    // Use the default dark theme values here.
    private fun getSystemThemeColors(): MyTheme {
        return MyTheme(
            labelId = R.string.system_default,
            textColorId = R.color.theme_dark_text_color,
            backgroundColorId = R.color.theme_dark_background_color,
            primaryColorId = R.color.color_primary,
            appIconColorId = R.color.color_primary
        )
    }

    private fun getCurrentThemeId(): Int {
        if (
            (baseConfig.isSystemThemeEnabled && !hasUnsavedChanges)
            || curSelectedThemeId == THEME_SYSTEM
        ) {
            return THEME_SYSTEM
        }

        var themeId = THEME_CUSTOM
        resources.apply {
            for ((key, value) in predefinedThemes
                .filter { it.key != THEME_CUSTOM && it.key != THEME_SYSTEM }) {
                if (curTextColor == getColor(value.textColorId) &&
                    curBackgroundColor == getColor(value.backgroundColorId) &&
                    curPrimaryColor == getColor(value.primaryColorId) &&
                    curAppIconColor == getColor(value.appIconColorId)
                ) {
                    themeId = key
                }
            }
        }

        return themeId
    }

    private fun getThemeText(): String {
        var label = R.string.custom
        for ((key, value) in predefinedThemes) {
            if (key == curSelectedThemeId) {
                label = value.labelId
            }
        }
        return getString(label)
    }

    private fun updateAutoThemeFields() {
        arrayOf(
            binding.customizationTextColorHolder,
            binding.customizationBackgroundColorHolder
        ).forEach {
            it.beVisibleIf(curSelectedThemeId != THEME_SYSTEM)
        }

        binding.customizationPrimaryColorHolder.beVisibleIf(
            beVisible = curSelectedThemeId != THEME_SYSTEM || !isSPlus()
        )
    }

    private fun promptSaveDiscard() {
        lastSavePromptTS = System.currentTimeMillis()
        ConfirmationAdvancedDialog(
            activity = this,
            message = "",
            messageId = R.string.save_before_closing,
            positive = R.string.save,
            negative = R.string.discard
        ) {
            if (it) {
                saveChanges(true)
            } else {
                resetColors()
                finish()
            }
        }
    }

    private fun saveChanges(finishAfterSave: Boolean) {
        val didAppIconColorChange = curAppIconColor != originalAppIconColor
        baseConfig.apply {
            textColor = curTextColor
            backgroundColor = curBackgroundColor
            primaryColor = curPrimaryColor
            accentColor = curAccentColor
            appIconColor = curAppIconColor
        }

        if (didAppIconColorChange) {
            checkAppIconColor()
        }

        baseConfig.isGlobalThemeEnabled = binding.applyToAllSwitch.isChecked
        baseConfig.isSystemThemeEnabled = curSelectedThemeId == THEME_SYSTEM

        if (isThankYouInstalled()) {
            val globalThemeType = when {
                baseConfig.isGlobalThemeEnabled.not() -> GLOBAL_THEME_DISABLED
                baseConfig.isSystemThemeEnabled -> GLOBAL_THEME_SYSTEM
                else -> GLOBAL_THEME_CUSTOM
            }

            updateGlobalConfig(
                ContentValues().apply {
                    put(COL_THEME_TYPE, globalThemeType)
                    put(COL_TEXT_COLOR, curTextColor)
                    put(COL_BACKGROUND_COLOR, curBackgroundColor)
                    put(COL_PRIMARY_COLOR, curPrimaryColor)
                    put(COL_ACCENT_COLOR, curAccentColor)
                    put(COL_APP_ICON_COLOR, curAppIconColor)
                }
            )
        }

        hasUnsavedChanges = false
        if (finishAfterSave) {
            finish()
        } else {
            refreshMenuItems()
        }
    }

    private fun resetColors() {
        hasUnsavedChanges = false
        initColorVariables()
        setupColorsPickers()
        updateBackgroundColor()
        updateActionbarColor()
        refreshMenuItems()
        updateLabelColors(getCurrentTextColor())
        updateHeaderColors(getCurrentAccentOrPrimaryColor())
        updateApplyToAllColors()
    }

    private fun initColorVariables() {
        curTextColor = baseConfig.textColor
        curBackgroundColor = baseConfig.backgroundColor
        curPrimaryColor = baseConfig.primaryColor
        curAccentColor = baseConfig.accentColor
        curAppIconColor = baseConfig.appIconColor
    }

    private fun setupColorsPickers() {
        val textColor = getCurrentTextColor()
        val backgroundColor = getCurrentBackgroundColor()
        val primaryColor = getCurrentPrimaryColor()
        binding.customizationTextColor.setFillWithStroke(textColor, backgroundColor)
        binding.customizationPrimaryColor.setFillWithStroke(primaryColor, backgroundColor)
        binding.customizationAccentColor.setFillWithStroke(curAccentColor, backgroundColor)
        binding.customizationBackgroundColor.setFillWithStroke(backgroundColor, backgroundColor)
        binding.customizationAppIconColor.setFillWithStroke(curAppIconColor, backgroundColor)
        binding.applyToAllSwitch.setTextColor(primaryColor.getContrastColor())

        binding.customizationTextColorHolder.setOnClickListener { pickTextColor() }
        binding.customizationBackgroundColorHolder.setOnClickListener { pickBackgroundColor() }
        binding.customizationPrimaryColorHolder.setOnClickListener { pickPrimaryColor() }
        binding.customizationAccentColorHolder.setOnClickListener { pickAccentColor() }

        handleAccentColorLayout()
        binding.applyToAllHolder.setOnClickListener { applyToAll() }
        binding.customizationAppIconColorHolder.setOnClickListener {
            if (baseConfig.wasAppIconCustomizationWarningShown) {
                pickAppIconColor()
            } else {
                ConfirmationDialog(
                    activity = this,
                    message = "",
                    messageId = R.string.app_icon_color_warning,
                    positive = R.string.ok,
                    negative = 0
                ) {
                    baseConfig.wasAppIconCustomizationWarningShown = true
                    pickAppIconColor()
                }
            }
        }
    }

    private fun hasColorChanged(old: Int, new: Int) = abs(old - new) > 1

    private fun colorChanged() {
        hasUnsavedChanges = true
        setupColorsPickers()
        refreshMenuItems()
    }

    private fun setCurrentTextColor(color: Int) {
        curTextColor = color
        updateLabelColors(color)
        updateApplyToAllColors()
    }

    private fun setCurrentBackgroundColor(color: Int) {
        curBackgroundColor = color
        updateBackgroundColor(color)
        updateApplyToAllColors()
    }

    private fun setCurrentPrimaryColor(color: Int) {
        curPrimaryColor = color
        updateActionbarColor(color)
        updateApplyToAllColors()
        updateHeaderColors(color)
    }

    private fun handleAccentColorLayout() {
        binding.customizationAccentColorHolder.beVisibleIf(
            beVisible = curSelectedThemeId == THEME_WHITE
                    || isCurrentWhiteTheme()
                    || curSelectedThemeId == THEME_BLACK_WHITE
                    || isCurrentBlackAndWhiteTheme()
        )
        binding.customizationAccentColorLabel.text = getString(
            if (curSelectedThemeId == THEME_WHITE || isCurrentWhiteTheme()) {
                R.string.accent_color_white
            } else {
                R.string.accent_color_black_and_white
            }
        )
    }

    private fun isCurrentWhiteTheme(): Boolean {
        return curTextColor == DARK_GREY
                && curPrimaryColor == Color.WHITE
                && curBackgroundColor == Color.WHITE
    }

    private fun isCurrentBlackAndWhiteTheme(): Boolean {
        return curTextColor == Color.WHITE
                && curPrimaryColor == Color.BLACK
                && curBackgroundColor == Color.BLACK
    }

    private fun pickTextColor() {
        ColorPickerDialog(this, curTextColor) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                if (hasColorChanged(curTextColor, color)) {
                    setCurrentTextColor(color)
                    colorChanged()
                    updateColorTheme(getCurrentThemeId())
                }
            }
        }
    }

    private fun pickBackgroundColor() {
        ColorPickerDialog(this, curBackgroundColor) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                if (hasColorChanged(curBackgroundColor, color)) {
                    setCurrentBackgroundColor(color)
                    colorChanged()
                    updateColorTheme(getCurrentThemeId())
                }
            }
        }
    }

    private fun pickPrimaryColor() {
        if (!packageName.startsWith("org.fossify.", true) && baseConfig.appRunCount > 50) {
            finish()
            return
        }

        curPrimaryLineColorPicker = LineColorPickerDialog(
            activity = this,
            color = curPrimaryColor,
            isPrimaryColorPicker = true,
            toolbar = binding.customizationToolbar
        ) { wasPositivePressed, color ->
            curPrimaryLineColorPicker = null
            if (wasPositivePressed) {
                if (hasColorChanged(curPrimaryColor, color)) {
                    setCurrentPrimaryColor(color)
                    colorChanged()
                    updateColorTheme(getCurrentThemeId())
                    setTheme(getThemeId(color))
                }
                updateMenuItemColors(binding.customizationToolbar.menu, color)
                setupToolbar(binding.customizationToolbar, NavigationIcon.Cross, color)
            } else {
                updateActionbarColor(curPrimaryColor)
                setTheme(getThemeId(curPrimaryColor))
                updateMenuItemColors(binding.customizationToolbar.menu, curPrimaryColor)
                setupToolbar(binding.customizationToolbar, NavigationIcon.Cross, curPrimaryColor)
                updateTopBarColors(binding.customizationToolbar, curPrimaryColor)
            }
        }
    }

    private fun pickAccentColor() {
        ColorPickerDialog(this, curAccentColor) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                if (hasColorChanged(curAccentColor, color)) {
                    curAccentColor = color
                    colorChanged()
                    updateApplyToAllColors()
                    updateHeaderColors(curAccentColor)
                    updateActionbarColor(getCurrentTopBarColor())
                    updateTopBarColors(binding.customizationToolbar, getCurrentTopBarColor())
                }
            }
        }
    }

    private fun pickAppIconColor() {
        LineColorPickerDialog(
            activity = this,
            color = curAppIconColor,
            isPrimaryColorPicker = false,
            primaryColors = R.array.md_app_icon_colors,
            appIconIDs = getAppIconIDs()
        ) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                if (hasColorChanged(curAppIconColor, color)) {
                    curAppIconColor = color
                    colorChanged()
                    updateColorTheme(getCurrentThemeId())
                }
            }
        }
    }

    private fun applyToAll() {
        when {
            canAccessGlobalConfig() && binding.applyToAllSwitch.isChecked -> {
                binding.applyToAllSwitch.isChecked = false
                updateColorTheme(getCurrentThemeId())
                saveChanges(false)
            }

            canAccessGlobalConfig() -> {
                binding.applyToAllSwitch.isChecked = true
                updateColorTheme(getCurrentThemeId())
                saveChanges(false)
                ConfirmationDialog(
                    activity = this,
                    message = "",
                    messageId = R.string.global_theme_success,
                    positive = R.string.ok,
                    negative = 0,
                    callback = {}
                )
            }

            else -> {
                binding.applyToAllSwitch.isChecked = false
                PurchaseThankYouDialog(this)
            }
        }
    }

    private fun updateLabelColors(textColor: Int = getProperTextColor()) {
        arrayListOf(
            binding.customizationThemeLabel,
            binding.customizationTheme,
            binding.customizationTextColorLabel,
            binding.customizationBackgroundColorLabel,
            binding.customizationPrimaryColorLabel,
            binding.customizationAccentColorLabel,
            binding.customizationAppIconColorLabel,
            binding.applyToAllLabel,
            binding.applyToAllNote
        ).forEach {
            it.setTextColor(textColor)
        }
    }

    private fun updateHeaderColors(primaryColor: Int = getProperPrimaryColor()) {
        arrayListOf(
            binding.settingsThemeAndColorsLabel,
            binding.settingsAllFossifyAppsLabel
        ).forEach {
            it.setTextColor(primaryColor)
        }
    }

    private fun updateApplyToAllColors() {
        binding.applyToAllSwitch.setColors(
            textColor = getCurrentTextColor(),
            accentColor = getCurrentAccentOrPrimaryColor(),
            backgroundColor = getCurrentBackgroundColor()
        )
    }

    private fun getCurrentTextColor() = when (binding.customizationTheme.value) {
        getMaterialYouString() -> resources.getColor(R.color.you_neutral_text_color)
        else -> curTextColor
    }

    private fun getCurrentBackgroundColor() = when (binding.customizationTheme.value) {
        getMaterialYouString() -> resources.getColor(R.color.you_background_color)
        else -> curBackgroundColor
    }

    private fun getCurrentPrimaryColor() = when (binding.customizationTheme.value) {
        getMaterialYouString() -> resources.getColor(R.color.you_primary_color)
        else -> curPrimaryColor
    }

    private fun getCurrentTopBarColor() = when {
        binding.customizationTheme.value == getMaterialYouString() -> {
            resources.getColor(R.color.you_status_bar_color)
        }

        isCurrentWhiteTheme() || isCurrentBlackAndWhiteTheme() -> curAccentColor
        else -> curPrimaryColor
    }

    private fun getCurrentAccentOrPrimaryColor() = when {
        isCurrentWhiteTheme() || isCurrentBlackAndWhiteTheme() -> curAccentColor
        else -> getCurrentPrimaryColor()
    }

    private fun getMaterialYouString() = getString(R.string.system_default)

    private fun showOrHideThankYouFeatures() {
        val showThankYouFeatures = canAccessGlobalConfig()
                || !resources.getBoolean(R.bool.hide_google_relations)
        binding.applyToAllNote.beVisibleIf(!canAccessGlobalConfig())
        binding.applyToAllHolder.beVisibleIf(showThankYouFeatures)
        binding.applyToAllDivider.root.beVisibleIf(showThankYouFeatures)
        binding.settingsAllFossifyAppsLabel.beVisibleIf(showThankYouFeatures)
        binding.settingsThemeAndColorsLabel.beVisibleIf(showThankYouFeatures)
        binding.applyToAllSwitch.isChecked = baseConfig.isGlobalThemeEnabled
        updateApplyToAllColors()
    }
}
