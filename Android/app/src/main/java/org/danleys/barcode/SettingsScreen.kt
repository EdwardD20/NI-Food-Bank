package org.danleys.barcode

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.core.net.toUri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import java.util.Locale
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    currentClientId: String,
    currentDisplayMode: String,
    onSettingsSaved: (String, String) -> Unit,
    hideBottomBar: Boolean = false
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val dataStore = context.dataStore
    val coroutineScope = rememberCoroutineScope()
    var showScanner by remember { mutableStateOf(false) }

    val displayModes = listOf(
        "barcode" to stringResource(id = R.string.display_mode_barcode),
        "qrcode" to stringResource(id = R.string.display_mode_qrcode)
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showScanner = true
        } else {
            Log.d("PERMISSION_DEBUG", "Camera permission denied")
        } // if
    } // rememberLauncherForActivityResult

    val languages = listOf(
        "en" to "English",
        "es" to "Español",
        "fr" to "Français",
        "ar" to "العربية",
        "arz" to "العربية (مصر)",
        "de" to "Deutsch",
        "pl" to "Polski",
        "uk" to "Українська",
        "zh" to "中文（简体）",
        "hi" to "हिन्दी",
        "tl" to "Tagalog",
        "vi" to "Tiếng Việt",
        "ko" to "한국어",
        "ru" to "Русский",
        "gu" to "ગુજરાતી",
        "km" to "ភាសាខ្មែر",
        "lo" to "ພາສາລາວ",
        "hmn" to "Lus Hmoob"
    )

    val languageKey = stringPreferencesKey("language")
    val systemLang = Locale.getDefault().language

    var selectedLanguageCode by remember { mutableStateOf<String?>(null) }
    var selectedLanguageName by remember { mutableStateOf<String?>(null) }
    var initializedLanguage by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!initializedLanguage) {
            val code = dataStore.data.map { prefs -> prefs[languageKey] ?: "" }.first()
            val effectiveCode = when {
                languages.any { it.first == code } -> code
                languages.any { it.first == systemLang } -> systemLang
                else -> "en"
            } // when
            selectedLanguageCode = effectiveCode
            selectedLanguageName = languages.first { it.first == effectiveCode }.second
            initializedLanguage = true
        } // if
    } // LaunchedEffect

    var clientId by rememberSaveable { mutableStateOf(currentClientId) }
    var selectedDisplayMode by rememberSaveable { mutableStateOf(currentDisplayMode) }
    var displayExpanded by remember { mutableStateOf(false) }
    var shareTheApp by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val content: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1B5E20)) // Dark Green Background
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
                val versionName = try {
                    val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                    pInfo.versionName ?: "1.0"
                } catch (_: Exception) {
                    "1.0"
                } // try-catch
                Text(
                    text = stringResource(id = R.string.version_label) + ": $versionName",
                    fontSize = 10.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 4.dp),
                    textAlign = TextAlign.Center
                )
            } // Column

            OutlinedTextField(
                value = clientId,
                onValueChange = { input ->
                    clientId = input.filter { it.isDigit() }
                }, // onValueChange
                label = { Text(stringResource(id = R.string.client_id_placeholder)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                placeholder = { Text("e.g. 123456") },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            val permissionCheckResult = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            )
                            if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                                showScanner = true
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            } // if
                        }, // onClick
                        modifier = Modifier.semantics {
                            contentDescription = "Scan barcode"
                        } // semantics
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = Color.Black
                        )
                    } // IconButton
                }, // trailingIcon
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Enter numeric Client ID"
                    }, // semantics
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    cursorColor = Color.White
                )
            )

            PrimaryRaisedWideButton(
                text = stringResource(id = R.string.save_client_and_close),
                onClick = { onSettingsSaved(clientId, selectedDisplayMode) }
            )

            FilledWideButton(
                text = AnnotatedString.fromHtml(stringResource(id = R.string.client_enrollment)),
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        "https://nifb.link2feed.com/create-account".toUri()
                    )
                    context.startActivity(intent)
                }
            )

            FilledWideButton(
                text = stringResource(id = R.string.website_label),
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        "https://solvehungertoday.org".toUri()
                    )
                    context.startActivity(intent)
                }
            )

            FilledWideButton(
                text = stringResource(id = R.string.shareTheApp),
                onClick = { shareTheApp = !shareTheApp }
            )

            CondensedDropdown(
                label = stringResource(id = R.string.display_mode_label),
                currentValue = displayModes.firstOrNull { it.first == selectedDisplayMode }?.second ?: "",
                onRowClick = { displayExpanded = true }
            ) {
                DropdownMenu(
                    expanded = displayExpanded,
                    onDismissRequest = { displayExpanded = false },
                    modifier = Modifier.background(Color(0xFF003300)) // Even darker green for menu
                ) {
                    displayModes.forEach { (mode, label) ->
                        DropdownMenuItem(
                            text = { Text(label, color = Color.Black) },
                            onClick = {
                                selectedDisplayMode = mode
                                displayExpanded = false
                            }
                        )
                    } // forEach
                } // DropdownMenu
            } // CondensedDropdown

            CondensedDropdown(
                label = stringResource(id = R.string.language_label),
                currentValue = selectedLanguageName ?: "",
                onRowClick = { expanded = true }
            ) {
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color(0xFF003300))
                ) {
                    languages.forEach { (code, name) ->
                        DropdownMenuItem(
                            text = { Text(name, color = Color.Black) },
                            onClick = {
                                expanded = false
                                if (selectedLanguageCode != code) {
                                    selectedLanguageCode = code
                                    selectedLanguageName = name
                                    coroutineScope.launch {
                                        dataStore.edit { prefs ->
                                            prefs[languageKey] = code
                                        } // dataStore.edit
                                        if (activity != null) {
                                            LocaleManager.setLocale(code)
                                            activity.recreate()
                                        } // if
                                    } // coroutineScope.launch
                                } // if
                            } // onClick
                        )
                    } // forEach
                } // DropdownMenu
            } // CondensedDropdown

        } // Column
    } // content lambda

    if (hideBottomBar) {
        content()
    } else {
        BottomBarLayout(
            websiteLabel = stringResource(id = R.string.website),
            facebookLabel = stringResource(id = R.string.facebook),
            onWebsiteTap = {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        "https://solvehungertoday.org".toUri()
                    )
                )
            }, // onWebsiteTap
            onFacebookTap = {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        "https://www.facebook.com/northernilfoodbank".toUri()
                    )
                )
            }, // onFacebookTap
            content = content
        )
    } // if

    if (shareTheApp) {
        Dialog(onDismissRequest = { shareTheApp = false }) {
            ShareAppSheet(
                appStoreLogo = R.drawable.appstorelogo,
                googlePlayLogo = R.drawable.googleplaystorelogo,
                onClose = { shareTheApp = false }
            )
        } // Dialog
    } // if

    if (showScanner) {
        BarcodeScannerScreen(
            onCodeScanned = { raw ->
                clientId = raw.filter { it.isDigit() }
                showScanner = false
            }, // onCodeScanned
            onDismiss = { showScanner = false }
        )
    } // if
} // fun SettingsScreen

@Composable
fun ShareAppSheet(
    appStoreLogo: Int,
    googlePlayLogo: Int,
    onClose: () -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val googleUrl = "https://play.google.com/store/apps/details?id=org.danleys.barcode"
    val appleUrl = "https://apps.apple.com/app/northern-illinois-food-bank/id6753714955"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 8.dp,
        color = Color(0xFF1B5E20) // Match theme
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(id = R.string.shareTheApp),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black
                    )
                    TextButton(onClick = { onClose() }) {
                        Text(
                            text = stringResource(id = R.string.close_button),
                            color = Color.Black
                        )
                    } // TextButton
                } // Row

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = googlePlayLogo),
                        contentDescription = "Google Play Store",
                        modifier = Modifier.width(screenWidth * 0.5f)
                    )
                    val googleBitmap: Bitmap? =
                        remember(googleUrl) { generateQRCodeBitmap(googleUrl) }
                    if (googleBitmap != null) {
                        Image(
                            bitmap = googleBitmap.asImageBitmap(),
                            contentDescription = "Google Play QR",
                            modifier = Modifier
                                .width(screenWidth * 0.75f)
                                .aspectRatio(1f)
                        )
                    } // if
                } // Column

                Spacer(modifier = Modifier.height(16.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = appStoreLogo),
                        contentDescription = "Apple App Store",
                        modifier = Modifier.width(screenWidth * 0.5f)
                    )
                    val appleBitmap: Bitmap? = remember(appleUrl) { generateQRCodeBitmap(appleUrl) }
                    if (appleBitmap != null) {
                        Image(
                            bitmap = appleBitmap.asImageBitmap(),
                            contentDescription = "Apple App Store QR",
                            modifier = Modifier
                                .width(screenWidth * 0.75f)
                                .aspectRatio(1f)
                        )
                    } // if
                } // Column
            } // Column
        } // Box
    } // Surface
} // fun ShareAppSheet

fun generateQRCodeBitmap(content: String): Bitmap? {
    return try {
        val size = 512
        val bits = MultiFormatWriter().encode(
            content,
            BarcodeFormat.QR_CODE,
            size,
            size
        )
        val bmp = createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp[x, y] = if (bits[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
            } // for y
        } // for x
        bmp
    } catch (_: Exception) {
        null
    } // try-catch
} // fun generateQRCodeBitmap

@Composable
private fun CondensedDropdown(
    label: String,
    currentValue: String,
    onRowClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRowClick() },
        color = Color.Transparent,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Box {
                Text(
                    text = currentValue,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
                content()
            } // Box
        } // Row
    } // Surface
} // fun CondensedDropdown

@Composable
private fun PrimaryRaisedWideButton(
    text: String,
    onClick: () -> Unit
) {
    ElevatedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = Color.White.copy(alpha = 0.9f),
            contentColor = Color(0xFF1B5E20)
        ),
        elevation = ButtonDefaults.elevatedButtonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 8.dp
        ),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            maxLines = 2,
            softWrap = true
        )
    } // ElevatedButton
} // fun PrimaryRaisedWideButton

@Composable
private fun FilledWideButton(
    text: Any,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.15f),
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(vertical = 10.dp)
    ) {
        val annotatedText = when(text) {
            is AnnotatedString -> text
            is String -> AnnotatedString(text)
            else -> AnnotatedString(text.toString())
        } // when
        Text(
            text = annotatedText,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            maxLines = 2,
            softWrap = true
        )
    } // Button
} // fun FilledWideButton
