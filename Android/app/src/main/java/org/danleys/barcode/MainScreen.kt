package org.danleys.barcode

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

@Composable
fun GoogleWalletButton(clientId: String, barcodeFormat: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                scope.launch {
                    try {
                        val saveUrl = withContext(Dispatchers.IO) {
                            val url = URL("https://edwardd20.homedns.org/google/index.php?clientID=$clientId&pass=nifb-pass&env=dev")
                            val response = url.readText()
                            val json = JSONObject(response)
                            if (json.has("error")) throw Exception(json.getString("error"))
                            json.getString("url")
                        } // withContext

                        val intent = Intent(Intent.ACTION_VIEW, saveUrl.toUri())

                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        } else {
                            snackbarHostState.showSnackbar("Google Wallet is not installed")
                        } // if

                    } catch (e: Exception) {
                        e.printStackTrace()
                        snackbarHostState.showSnackbar("Error opening Google Wallet")
                    } // try-catch
                } // scope.launch
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.googlewallet_vector),
            contentDescription = "Add to Google Wallet",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .height(64.dp)
                .wrapContentWidth()
                .semantics {
                    role = Role.Button
                    contentDescription = "Add to Google Wallet"
                } // semantics
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    } // Box
} // fun GoogleWalletButton

@Composable
fun MainScreen(
    clientId: String,
    displayMode: String,
    onSettingsClick: () -> Unit,
    onSettingsSaved: (String, String) -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isTwoPaneMode = configuration.screenWidthDp >= 600

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
        } // onFacebookTap
    ) {
        if (isTwoPaneMode) {
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    MainContent(
                        clientId = clientId,
                        displayMode = displayMode,
                        isLandscape = isLandscape,
                        isTwoPaneMode = isTwoPaneMode,
                        onSettingsClick = onSettingsClick,
                    )
                } // Column

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    SettingsScreen(
                        currentClientId = clientId,
                        currentDisplayMode = displayMode,
                        onSettingsSaved = onSettingsSaved,
                        hideBottomBar = true
                    )
                } // Surface
            } // Row
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                MainContent(
                    clientId = clientId,
                    displayMode = displayMode,
                    isLandscape = isLandscape,
                    isTwoPaneMode = isTwoPaneMode,
                    onSettingsClick = onSettingsClick,
                )
                Spacer(modifier = Modifier.height(32.dp))
            } // Column
        } // if
    } // BottomBarLayout
} // fun MainScreen

@Composable
private fun MainContent(
    clientId: String,
    displayMode: String,
    isLandscape: Boolean,
    isTwoPaneMode: Boolean,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val noRipple = remember { MutableInteractionSource() }
    val siteUrl = "https://solvehungertoday.org"

    val logoModifier = if (isLandscape || isTwoPaneMode) {
        Modifier
    } else {
        Modifier
            .fillMaxWidth()
            .height(200.dp)
    } // if

    Image(
        painter = painterResource(id = R.drawable.logo),
        contentDescription = "Open website",
        modifier = logoModifier
            .semantics {
                role = Role.Button
                contentDescription = "Open website"
            } // semantics
            .clickable(
                interactionSource = noRipple,
                indication = null,
                role = Role.Button,
                onClickLabel = "Open website"
            ) {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, siteUrl.toUri())
                )
            } // clickable
    )

    Spacer(modifier = Modifier.height(16.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.app_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            softWrap = true,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        )
    } // Box

    if (clientId.isNotEmpty()) {
        BarcodeGenerator(data = clientId, mode = displayMode)

        Text(
            text = clientId,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        GoogleWalletButton(clientId = clientId, barcodeFormat = "code128")

    } else {
        Text(
            text = stringResource(id = R.string.no_client),
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            color = Color.White,
            modifier = Modifier.padding(vertical = 32.dp)
        )
    } // if

    Spacer(modifier = Modifier.height(24.dp))

    MediumWidthButton(
        text = "Facebook",
        icon = R.drawable.ic_facebook,
        onClick = {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    "https://www.facebook.com/northernilfoodbank".toUri()
                )
            )
        } // onClick
    )

    Spacer(modifier = Modifier.height(12.dp))

    MediumWidthButton(
        text = stringResource(id = R.string.settings_button),
        onClick = { onSettingsClick() },
        enabled = !isTwoPaneMode
    )
} // fun MainContent
