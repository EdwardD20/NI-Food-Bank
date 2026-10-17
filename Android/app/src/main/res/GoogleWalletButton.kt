@Composable
fun GoogleWalletButton(clientId: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    scope.launch {
                        try {
                            val saveUrl = withContext(Dispatchers.IO) {
                                val url = URL("https://edwardd20.homedns.org/google/index.php?clientID=$clientId&pass=nifb-pass&env=dev")
                                val response = url.readText()
                                val json = org.json.JSONObject(response)
                                if (json.has("error")) throw Exception(json.getString("error"))
                                json.getString("url")
                            } // val saveURL
                            context.startActivity(Intent(Intent.ACTION_VIEW, saveUrl.toUri()))
                        } catch (e: Exception) {
                            e.printStackTrace()
                            withContext(Dispatchers.Main) {
                                android.widget.Toast.makeText(
                                    context,
                                    context.getString(R.string.wallet_error),
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            } // withContext
                        } // catch
                    } // scope.lauhch
                }) // detectTapGestures
            }, // .pointerInput
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
                } // .senantics
        ) // image
    } // Box (second part)
} // fun GoogleWalletButton