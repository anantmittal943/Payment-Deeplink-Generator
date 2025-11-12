package com.anantmittal.myapplication

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anantmittal.myapplication.setu.*
import kotlinx.coroutines.launch

@Composable
fun SetuPaymentLinkScreen() {
    var amount by remember { mutableStateOf("") }
    var billId by remember { mutableStateOf("") }
    var merchantName by remember { mutableStateOf("") }
    var transactionNote by remember { mutableStateOf("") }

    // API Credentials (TODO: Store securely)
    var clientId by remember { mutableStateOf("") }
    var clientSecret by remember { mutableStateOf("") }
    var productInstanceId by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var generatedLink by remember { mutableStateOf<PaymentLinkData?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCredentialsDialog by remember { mutableStateOf(false) }

    val repository = remember { SetuRepository() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "Setu Payment Link",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 24.dp),
                letterSpacing = (-0.5).sp
            )

            // Credentials Button
            OutlinedButton(
                onClick = { showCredentialsDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 4.dp,
                    bottomEnd = 20.dp,
                    bottomStart = 4.dp
                )
            ) {
                Text(
                    text = if (clientId.isEmpty()) "⚙️ Setup API Credentials" else "⚙️ Update Credentials",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // Input Fields Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(
                    topStart = 24.dp,
                    topEnd = 8.dp,
                    bottomEnd = 24.dp,
                    bottomStart = 8.dp
                ),
                color = Color.White.copy(alpha = 0.08f),
                border = BorderStroke(2.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Payment Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Bill ID
                    SetuTextField(
                        value = billId,
                        onValueChange = { billId = it },
                        label = "Bill ID *",
                        placeholder = "Enter unique bill ID"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Amount
                    SetuTextField(
                        value = amount,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d+$"))) {
                                amount = newValue
                            }
                        },
                        label = "Amount (in paise) *",
                        placeholder = "e.g., 10000 for ₹100",
                        keyboardType = KeyboardType.Number
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Merchant Name
                    SetuTextField(
                        value = merchantName,
                        onValueChange = { merchantName = it },
                        label = "Merchant Name",
                        placeholder = "Optional"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Transaction Note
                    SetuTextField(
                        value = transactionNote,
                        onValueChange = { if (it.length <= 80) transactionNote = it },
                        label = "Transaction Note",
                        placeholder = "Max 80 characters"
                    )
                }
            }

            // Generate Link Button
            AnimatedSetuButton(
                text = "Generate Payment Link",
                enabled = billId.isNotEmpty() && amount.isNotEmpty() && clientId.isNotEmpty(),
                isLoading = isLoading,
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        generatedLink = null

                        // Get auth token
                        val tokenResult = repository.getAuthToken(clientId, clientSecret)

                        if (tokenResult.isSuccess) {
                            val token = tokenResult.getOrNull()!!

                            // Create payment link
                            val linkRequest = CreatePaymentLinkRequest(
                                billerBillID = billId,
                                amountExactness = "EXACT",
                                amount = Amount(value = amount.toInt()),
                                name = merchantName.ifEmpty { null },
                                transactionNote = transactionNote.ifEmpty { null }
                            )

                            val linkResult = repository.createPaymentLink(
                                token,
                                productInstanceId,
                                linkRequest
                            )

                            if (linkResult.isSuccess) {
                                generatedLink = linkResult.getOrNull()
                            } else {
                                errorMessage = linkResult.exceptionOrNull()?.message ?: "Failed to generate link"
                            }
                        } else {
                            errorMessage = tokenResult.exceptionOrNull()?.message ?: "Authentication failed"
                        }

                        isLoading = false
                    }
                }
            )

            // Error Message
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF44336).copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, Color(0xFFF44336).copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "❌ $errorMessage",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFF44336),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Generated Link Display
            if (generatedLink != null) {
                Spacer(modifier = Modifier.height(16.dp))
                GeneratedLinkCard(
                    linkData = generatedLink!!,
                    context = context
                )
            }
        }
    }

    // Credentials Dialog
    if (showCredentialsDialog) {
        CredentialsDialog(
            currentClientId = clientId,
            currentSecret = clientSecret,
            currentProductId = productInstanceId,
            onDismiss = { showCredentialsDialog = false },
            onSave = { id, secret, productId ->
                clientId = id
                clientSecret = secret
                productInstanceId = productId
                showCredentialsDialog = false
            }
        )
    }
}

@Composable
fun SetuTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color.White.copy(alpha = 0.3f)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White.copy(alpha = 0.5f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                cursorColor = Color.White
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
    }
}

@Composable
fun AnimatedSetuButton(
    text: String,
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed && enabled && !isLoading -> 0.96f
            enabled && !isLoading -> 1f
            else -> 0.95f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .scale(scale),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black,
            disabledContainerColor = Color.White.copy(alpha = 0.3f),
            disabledContentColor = Color.Black.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 8.dp,
            bottomEnd = 28.dp,
            bottomStart = 8.dp
        ),
        interactionSource = interactionSource
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.Black,
                strokeWidth = 2.5.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        Text(
            text = if (isLoading) "Generating..." else text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun GeneratedLinkCard(linkData: PaymentLinkData, context: Context) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = 24.dp,
            topEnd = 8.dp,
            bottomEnd = 24.dp,
            bottomStart = 8.dp
        ),
        color = Color(0xFF4CAF50).copy(alpha = 0.2f),
        border = BorderStroke(2.dp, Color(0xFF4CAF50).copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "✓ Payment Link Generated!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Platform Bill ID
            LinkInfoRow(
                label = "Bill ID:",
                value = linkData.platformBillID,
                context = context
            )

            Spacer(modifier = Modifier.height(12.dp))

            // UPI ID
            LinkInfoRow(
                label = "UPI ID:",
                value = linkData.paymentLink.upiID,
                context = context
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Short URL
            LinkInfoRow(
                label = "Short URL:",
                value = linkData.paymentLink.shortURL,
                context = context,
                isUrl = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse(linkData.paymentLink.upiLink)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Pay Now")
                }

                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("UPI Link", linkData.paymentLink.upiLink)
                        clipboard.setPrimaryClip(clip)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Copy Link", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun LinkInfoRow(label: String, value: String, context: Context, isUrl: Boolean = false) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.6f)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUrl) Color(0xFF64B5F6) else Color.White,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText(label, value)
                    clipboard.setPrimaryClip(clip)
                }
            ) {
                Text("📋", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun CredentialsDialog(
    currentClientId: String,
    currentSecret: String,
    currentProductId: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var clientId by remember { mutableStateOf(currentClientId) }
    var secret by remember { mutableStateOf(currentSecret) }
    var productId by remember { mutableStateOf(currentProductId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        title = {
            Text(
                text = "API Credentials",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter your Setu API credentials",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                SetuTextField(
                    value = clientId,
                    onValueChange = { clientId = it },
                    label = "Client ID",
                    placeholder = "Enter client ID"
                )

                Spacer(modifier = Modifier.height(12.dp))

                SetuTextField(
                    value = secret,
                    onValueChange = { secret = it },
                    label = "Client Secret",
                    placeholder = "Enter client secret"
                )

                Spacer(modifier = Modifier.height(12.dp))

                SetuTextField(
                    value = productId,
                    onValueChange = { productId = it },
                    label = "Product Instance ID",
                    placeholder = "Enter product instance ID"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(clientId, secret, productId) },
                enabled = clientId.isNotEmpty() && secret.isNotEmpty() && productId.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

