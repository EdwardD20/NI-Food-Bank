package org.danleys.barcode

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import android.content.pm.ActivityInfo
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.danleys.barcode.ui.theme.BarcodeTheme

class MainActivity : ComponentActivity() {

    private val clientIdKey = stringPreferencesKey("client_id")
    private val languageKey = stringPreferencesKey("language")
    private val displayModeKey = stringPreferencesKey("display_mode")

    override fun attachBaseContext(newBase: Context) {
        val dataStore = newBase.dataStore
        var savedLang = "en"
        runBlocking {
            savedLang = dataStore.data.first()[languageKey] ?: "en"
        } // runBlocking
        val context = LocaleManager.wrapContextWithLocale(newBase, savedLang)
        super.attachBaseContext(context)
    } // fun attachBaseContext

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dataStore = applicationContext.dataStore

        setContent {
            BarcodeTheme {
                var showSplash by remember { mutableStateOf(true) }
                val scope = rememberCoroutineScope()

                val prefsState by dataStore.data.collectAsState(initial = null)

                val currentClientId = prefsState?.get(clientIdKey) ?: ""
                val currentDisplayMode = prefsState?.get(displayModeKey) ?: "barcode"

                var showSettings by remember { mutableStateOf(false) }
                var initialized by remember { mutableStateOf(false) }

                val configuration = LocalConfiguration.current
                LaunchedEffect(configuration.smallestScreenWidthDp) {
                    requestedOrientation = if (configuration.smallestScreenWidthDp < 600) {
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    } else {
                        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    } // if
                } // LaunchedEffect

                if (prefsState != null && !initialized) {
                    showSettings = currentClientId.isEmpty()
                    initialized = true
                } // if

                fun saveSettings(newId: String, newDisplayMode: String) {
                    scope.launch {
                        try {
                            dataStore.edit {
                                it[clientIdKey] = newId
                                it[displayModeKey] = newDisplayMode
                            } // dataStore.edit
                            showSettings = false
                        } catch (_: Exception) {}
                    } // scope.launch
                } // fun saveSettings

                Surface(modifier = Modifier.fillMaxSize()) {
                    if (showSplash) {
                        SplashScreen(
                            onTimeout = { showSplash = false }
                        )
                    } else {
                        val configuration = LocalConfiguration.current
                        val isTwoPaneMode = configuration.screenWidthDp >= 600

                        if (showSettings && !isTwoPaneMode) {
                            SettingsScreen(
                                currentClientId = currentClientId,
                                currentDisplayMode = currentDisplayMode,
                                onSettingsSaved = { id, mode -> saveSettings(id, mode) }
                            )
                        } else {
                            MainScreen(
                                clientId = currentClientId,
                                displayMode = currentDisplayMode,
                                onSettingsClick = { showSettings = true },
                                onSettingsSaved = { id, mode -> saveSettings(id, mode) }
                            )
                        } // if
                    } // if
                } // Surface
            } // BarcodeTheme
        } // setContent
    } // fun onCreate
} // class MainActivity
