package com.anantmittal.myapplication

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PaymentScreen() {
    var amount by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var paymentStatus by remember { mutableStateOf<PaymentStatus>(PaymentStatus.Idle) }
    var showStatusDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Activity result launcher for UPI
    val upiLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        scope.launch {
            isLoading = true
            delay(1000) // Brief delay for UI feedback

            // Check payment result with proper status handling
            val data = result.data
            val response = data?.getStringExtra("response")?.lowercase() ?: ""
            val status = data?.getStringExtra("Status")?.lowercase() ?: ""
            val txnId = data?.getStringExtra("txnId") ?: ""

            // Proper payment verification logic
            paymentStatus = when {
                // Explicit success indicators
                response.contains("success") || status.contains("success") -> PaymentStatus.Success
                response.contains("submitted") || status.contains("submitted") -> PaymentStatus.Success
                txnId.isNotEmpty() && (response.contains("success") || status.contains("success")) -> PaymentStatus.Success

                // Explicit failure indicators
                response.contains("fail") || status.contains("fail") -> PaymentStatus.Failed
                response.contains("failure") || status.contains("failure") -> PaymentStatus.Failed
                response.contains("declined") || status.contains("declined") -> PaymentStatus.Failed
                response.contains("cancelled") || status.contains("cancelled") -> PaymentStatus.Failed
                response.contains("cancel") && !response.contains("success") -> PaymentStatus.Failed

                // User cancelled/backed out (no response or empty)
                result.resultCode == android.app.Activity.RESULT_CANCELED -> PaymentStatus.Failed
                response.isEmpty() && status.isEmpty() -> PaymentStatus.Failed

                // Default to failed for unknown status
                else -> PaymentStatus.Failed
            }

            isLoading = false
            showStatusDialog = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title with Expressive Typography
            Text(
                text = "Payment",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 32.dp),
                letterSpacing = (-0.5).sp
            )

            // Amount Input Card with Expressive Shape
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(
                    topStart = 32.dp,
                    topEnd = 8.dp,
                    bottomEnd = 32.dp,
                    bottomStart = 8.dp
                ),
                color = Color.White.copy(alpha = 0.08f),
                border = BorderStroke(2.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Enter Amount",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 12.dp),
                        letterSpacing = 0.5.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "₹",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(end = 8.dp)
                        )

                        OutlinedTextField(
                            value = amount,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || newValue.matches(Regex("^\\d+\\.?\\d{0,2}$"))) {
                                    amount = newValue
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 40.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                letterSpacing = (-1).sp
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = Color.White,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            placeholder = {
                                Text(
                                    text = "0.00",
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White.copy(alpha = 0.25f),
                                    letterSpacing = (-1).sp
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Quick Amount Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(100, 500, 1000, 2000).forEach { quickAmount ->
                    QuickAmountButton(
                        amount = quickAmount,
                        onClick = { amount = quickAmount.toString() }
                    )
                }
            }

            // Pay Button
            AnimatedPayButton(
                enabled = amount.isNotEmpty() && amount.toDoubleOrNull() != null && amount.toDouble() > 0,
                isLoading = isLoading,
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (amountValue != null && amountValue > 0) {
                        isLoading = true
                        scope.launch {
                            delay(500) // Show loading animation
                            val upiUri = generateUpiDeeplink(amountValue)
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse(upiUri)
                            }
                            try {
                                upiLauncher.launch(intent)
                            } catch (_: Exception) {
                                paymentStatus = PaymentStatus.Failed
                                showStatusDialog = true
                            }
                            isLoading = false
                        }
                    }
                }
            )

            // UPI Info
            Text(
                text = "Pay securely via UPI",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }

    // Status Dialog
    if (showStatusDialog) {
        PaymentStatusDialog(
            status = paymentStatus,
            amount = amount,
            onDismiss = {
                showStatusDialog = false
                if (paymentStatus == PaymentStatus.Success) {
                    amount = ""
                }
                paymentStatus = PaymentStatus.Idle
            }
        )
    }
}

@Composable
fun QuickAmountButton(
    amount: Int,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "button_scale"
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .height(44.dp)
            .widthIn(min = 68.dp)
            .scale(scale),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.08f),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 4.dp,
            bottomEnd = 16.dp,
            bottomStart = 4.dp
        ),
        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.25f)),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = "₹$amount",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AnimatedPayButton(
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
            .heightIn(min = 56.dp, max = 64.dp)
            .scale(scale),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black,
            disabledContainerColor = Color.White.copy(alpha = 0.3f),
            disabledContentColor = Color.Black.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(
            topStart = 32.dp,
            topEnd = 8.dp,
            bottomEnd = 32.dp,
            bottomStart = 8.dp
        ),
        interactionSource = interactionSource
    ) {
        if (isLoading) {
            LoadingIndicator()
        } else {
            Text(
                text = "Pay Now",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun LoadingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(20.dp)
                .scale(pulse),
            color = Color.Black,
            strokeWidth = 2.5.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Processing...",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            letterSpacing = 0.3.sp
        )
    }
}

@Composable
fun PaymentStatusDialog(
    status: PaymentStatus,
    amount: String,
    onDismiss: () -> Unit
) {
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(300)
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(
            topStart = 40.dp,
            topEnd = 12.dp,
            bottomEnd = 40.dp,
            bottomStart = 12.dp
        ),
        modifier = Modifier
            .scale(scale.value)
            .fillMaxWidth(0.9f),
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animated Icon
                AnimatedStatusIcon(status = status)

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = if (status == PaymentStatus.Success) "Payment Successful!" else "Payment Failed",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.3).sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (status == PaymentStatus.Success) {
                    Text(
                        text = "₹$amount",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Your payment has been processed successfully",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.2.sp
                    )
                } else {
                    Text(
                        text = "Something went wrong. Please try again.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.2.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .heightIn(min = 48.dp, max = 52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(
                    topStart = 26.dp,
                    topEnd = 6.dp,
                    bottomEnd = 26.dp,
                    bottomStart = 6.dp
                )
            ) {
                Text(
                    text = "Done",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    )
}

@Composable
fun AnimatedStatusIcon(status: PaymentStatus) {
    val infiniteTransition = rememberInfiniteTransition(label = "status_icon")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(80.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                if (status == PaymentStatus.Success)
                    Color(0xFF4CAF50)
                else
                    Color(0xFFF44336)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (status == PaymentStatus.Success) "✓" else "✕",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

sealed class PaymentStatus {
    object Idle : PaymentStatus()
    object Success : PaymentStatus()
    object Failed : PaymentStatus()
}

fun generateUpiDeeplink(amount: Double): String {
    // Replace these with your actual UPI details
    val upiId = "arpit.srivastava70@axl"
    val name = "Arpit Srivastava"
    val transactionNote = "Payment"

    return "upi://pay?pa=$upiId&pn=${Uri.encode(name)}&tn=${Uri.encode(transactionNote)}&am=$amount&cu=INR"
}

