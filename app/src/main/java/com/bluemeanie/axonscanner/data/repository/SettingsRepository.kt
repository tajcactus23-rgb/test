package com.bluemeanie.axonscanner.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.bluemeanie.axonscanner.domain.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val BACKGROUND = stringPreferencesKey("background")
        val BACKGROUND_OPACITY = floatPreferencesKey("background_opacity")
        val PERFORMANCE_MODE = booleanPreferencesKey("performance_mode")
        val RADAR_STYLE = stringPreferencesKey("radar_style")
        val SCAN_BUTTON_STYLE = stringPreferencesKey("scan_button_style")
        val SCAN_BUTTON_COLOR = stringPreferencesKey("scan_button_color")
        val SCAN_BUTTON_SIZE = stringPreferencesKey("scan_button_size")
        val COUNTER_SIZE = stringPreferencesKey("counter_size")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val FULLSCREEN_ALERT_ENABLED = booleanPreferencesKey("fullscreen_alert_enabled")
        val TELEGRAM_BOT_TOKEN = stringPreferencesKey("telegram_bot_token")
        val TELEGRAM_CHAT_ID = stringPreferencesKey("telegram_chat_id")
        val SCAN_MODE = stringPreferencesKey("scan_mode")
        val CALLSIGN = stringPreferencesKey("callsign")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val FONT_RADAR_SIZE = intPreferencesKey("font_radar_size")
        val FONT_RESULTS_SIZE = intPreferencesKey("font_results_size")
        val FONT_MENU_SIZE = intPreferencesKey("font_menu_size")
        val FONT_SETTINGS_SIZE = intPreferencesKey("font_settings_size")
        val FONT_CONSOLE_SIZE = intPreferencesKey("font_console_size")
        val FONT_GAUGE_SIZE = intPreferencesKey("font_gauge_size")
        val FONT_RADAR_FAMILY = stringPreferencesKey("font_radar_family")
        val FONT_RESULTS_FAMILY = stringPreferencesKey("font_results_family")
        val FONT_MENU_FAMILY = stringPreferencesKey("font_menu_family")
        val FONT_SETTINGS_FAMILY = stringPreferencesKey("font_settings_family")
        val FONT_CONSOLE_FAMILY = stringPreferencesKey("font_console_family")
        val FONT_GAUGE_FAMILY = stringPreferencesKey("font_gauge_family")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        AppSettings(
            theme = ThemeType.valueOf(preferences[Keys.THEME] ?: ThemeType.CLASSIC.name),
            background = BackgroundType.valueOf(preferences[Keys.BACKGROUND] ?: BackgroundType.NEURAL.name),
            backgroundOpacity = preferences[Keys.BACKGROUND_OPACITY] ?: 0.5f,
            performanceMode = preferences[Keys.PERFORMANCE_MODE] ?: false,
            radarStyle = RadarStyle.valueOf(preferences[Keys.RADAR_STYLE] ?: RadarStyle.TACTICAL.name),
            scanButtonStyle = ScanButtonStyle.valueOf(preferences[Keys.SCAN_BUTTON_STYLE] ?: ScanButtonStyle.PULSE.name),
            scanButtonColor = preferences[Keys.SCAN_BUTTON_COLOR] ?: "#00F0FF",
            scanButtonSize = ScanButtonSize.valueOf(preferences[Keys.SCAN_BUTTON_SIZE] ?: ScanButtonSize.MEDIUM.name),
            counterSize = CounterSize.valueOf(preferences[Keys.COUNTER_SIZE] ?: CounterSize.MEDIUM.name),
            soundEnabled = preferences[Keys.SOUND_ENABLED] ?: true,
            vibrationEnabled = preferences[Keys.VIBRATION_ENABLED] ?: true,
            notificationsEnabled = preferences[Keys.NOTIFICATIONS_ENABLED] ?: true,
            fullscreenAlertEnabled = preferences[Keys.FULLSCREEN_ALERT_ENABLED] ?: true,
            telegramBotToken = preferences[Keys.TELEGRAM_BOT_TOKEN],
            telegramChatId = preferences[Keys.TELEGRAM_CHAT_ID],
            scanMode = ScanMode.valueOf(preferences[Keys.SCAN_MODE] ?: ScanMode.TACTICAL.name),
            fontRadarSize = preferences[Keys.FONT_RADAR_SIZE] ?: 32,
            fontResultsSize = preferences[Keys.FONT_RESULTS_SIZE] ?: 14,
            fontMenuSize = preferences[Keys.FONT_MENU_SIZE] ?: 12,
            fontSettingsSize = preferences[Keys.FONT_SETTINGS_SIZE] ?: 14,
            fontConsoleSize = preferences[Keys.FONT_CONSOLE_SIZE] ?: 10,
            fontGaugeSize = preferences[Keys.FONT_GAUGE_SIZE] ?: 36,
            fontRadarFamily = preferences[Keys.FONT_RADAR_FAMILY] ?: "JetBrains Mono",
            fontResultsFamily = preferences[Keys.FONT_RESULTS_FAMILY] ?: "Fira Code",
            fontMenuFamily = preferences[Keys.FONT_MENU_FAMILY] ?: "JetBrains Mono",
            fontSettingsFamily = preferences[Keys.FONT_SETTINGS_FAMILY] ?: "Source Code Pro",
            fontConsoleFamily = preferences[Keys.FONT_CONSOLE_FAMILY] ?: "JetBrains Mono",
            fontGaugeFamily = preferences[Keys.FONT_GAUGE_FAMILY] ?: "Space Mono"
        )
    }

    val callsign: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[Keys.CALLSIGN] ?: "OPERATOR"
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.ONBOARDING_COMPLETED] ?: false
    }

    suspend fun updateSettings(settings: AppSettings) {
        context.dataStore.edit { preferences ->
            preferences[Keys.THEME] = settings.theme.name
            preferences[Keys.BACKGROUND] = settings.background.name
            preferences[Keys.BACKGROUND_OPACITY] = settings.backgroundOpacity
            preferences[Keys.PERFORMANCE_MODE] = settings.performanceMode
            preferences[Keys.RADAR_STYLE] = settings.radarStyle.name
            preferences[Keys.SCAN_BUTTON_STYLE] = settings.scanButtonStyle.name
            preferences[Keys.SCAN_BUTTON_COLOR] = settings.scanButtonColor
            preferences[Keys.SCAN_BUTTON_SIZE] = settings.scanButtonSize.name
            preferences[Keys.COUNTER_SIZE] = settings.counterSize.name
            preferences[Keys.SOUND_ENABLED] = settings.soundEnabled
            preferences[Keys.VIBRATION_ENABLED] = settings.vibrationEnabled
            preferences[Keys.NOTIFICATIONS_ENABLED] = settings.notificationsEnabled
            preferences[Keys.FULLSCREEN_ALERT_ENABLED] = settings.fullscreenAlertEnabled
            settings.telegramBotToken?.let { preferences[Keys.TELEGRAM_BOT_TOKEN] = it }
            settings.telegramChatId?.let { preferences[Keys.TELEGRAM_CHAT_ID] = it }
            preferences[Keys.SCAN_MODE] = settings.scanMode.name
            preferences[Keys.FONT_RADAR_SIZE] = settings.fontRadarSize
            preferences[Keys.FONT_RESULTS_SIZE] = settings.fontResultsSize
            preferences[Keys.FONT_MENU_SIZE] = settings.fontMenuSize
            preferences[Keys.FONT_SETTINGS_SIZE] = settings.fontSettingsSize
            preferences[Keys.FONT_CONSOLE_SIZE] = settings.fontConsoleSize
            preferences[Keys.FONT_GAUGE_SIZE] = settings.fontGaugeSize
            preferences[Keys.FONT_RADAR_FAMILY] = settings.fontRadarFamily
            preferences[Keys.FONT_RESULTS_FAMILY] = settings.fontResultsFamily
            preferences[Keys.FONT_MENU_FAMILY] = settings.fontMenuFamily
            preferences[Keys.FONT_SETTINGS_FAMILY] = settings.fontSettingsFamily
            preferences[Keys.FONT_CONSOLE_FAMILY] = settings.fontConsoleFamily
            preferences[Keys.FONT_GAUGE_FAMILY] = settings.fontGaugeFamily
        }
    }

    suspend fun setCallsign(callsign: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.CALLSIGN] = callsign.uppercase()
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun updateTheme(theme: ThemeType) {
        context.dataStore.edit { preferences ->
            preferences[Keys.THEME] = theme.name
        }
    }

    suspend fun updateBackground(background: BackgroundType) {
        context.dataStore.edit { preferences ->
            preferences[Keys.BACKGROUND] = background.name
        }
    }

    suspend fun updateScanMode(scanMode: ScanMode) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SCAN_MODE] = scanMode.name
        }
    }

    suspend fun updateRadarStyle(radarStyle: RadarStyle) {
        context.dataStore.edit { preferences ->
            preferences[Keys.RADAR_STYLE] = radarStyle.name
        }
    }

    suspend fun updateScanButtonStyle(style: ScanButtonStyle) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SCAN_BUTTON_STYLE] = style.name
        }
    }

    suspend fun updateTelegramConfig(botToken: String?, chatId: String?) {
        context.dataStore.edit { preferences ->
            if (botToken != null) {
                preferences[Keys.TELEGRAM_BOT_TOKEN] = botToken
            } else {
                preferences.remove(Keys.TELEGRAM_BOT_TOKEN)
            }
            if (chatId != null) {
                preferences[Keys.TELEGRAM_CHAT_ID] = chatId
            } else {
                preferences.remove(Keys.TELEGRAM_CHAT_ID)
            }
        }
    }

    suspend fun resetToDefaults() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}