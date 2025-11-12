# Payment Deeplink Generator

A modern, aesthetic Android payment application with UPI deeplink integration. Features a sleek black and white UI with smooth animations.

## Features

✨ **Modern UI Design**
- Clean black and white color scheme
- Smooth animations and transitions
- Glass-morphism effect cards
- Responsive layout

💰 **Payment Functionality**
- Amount input with decimal support
- Quick amount selection buttons (₹100, ₹500, ₹1000, ₹2000)
- UPI deeplink integration
- Real-time payment status verification

🎭 **Animations**
- Animated pay button with scale effects
- Loading indicator during payment processing
- Success/failure status dialog with spring animations
- Pulsing status icons

🔒 **Payment Verification**
- Automatic payment status detection
- Success/failure feedback
- Transaction amount display
- Error handling

## How to Use

1. **Enter Amount**: Type the payment amount or use quick select buttons
2. **Pay Now**: Tap the pay button to initiate UPI payment
3. **Select UPI App**: Choose your preferred UPI app from the system dialog
4. **Complete Payment**: Follow your UPI app's flow
5. **Verification**: Return to the app to see payment status

## Customization

### Update UPI Details

Edit the `generateUpiDeeplink()` function in `PaymentScreen.kt`:

```kotlin
fun generateUpiDeeplink(amount: Double): String {
    val upiId = "your-upi-id@okaxis"  // Your UPI ID
    val name = "Your Business Name"    // Your business/personal name
    val transactionNote = "Payment"    // Transaction note
    
    return "upi://pay?pa=$upiId&pn=${Uri.encode(name)}&tn=${Uri.encode(transactionNote)}&am=$amount&cu=INR"
}
```

### Customize Quick Amount Buttons

In `PaymentScreen.kt`, find this line:

```kotlin
listOf(100, 500, 1000, 2000).forEach { quickAmount ->
```

Replace with your desired amounts:

```kotlin
listOf(50, 200, 500, 1000).forEach { quickAmount ->
```

### Color Customization

To change colors, modify these values in `PaymentScreen.kt`:

- Background: `Color.Black`
- Text: `Color.White`
- Button: `Color.White` (background), `Color.Black` (text)
- Success: `Color(0xFF4CAF50)`
- Failure: `Color(0xFFF44336)`

## Technical Details

### Architecture
- **Jetpack Compose**: Modern declarative UI
- **Material 3**: Latest Material Design components
- **Coroutines**: Async payment processing
- **Activity Result API**: Handle UPI app results

### UPI Deeplink Format
```
upi://pay?pa={UPI_ID}&pn={NAME}&tn={NOTE}&am={AMOUNT}&cu=INR
```

### Payment Flow
1. User enters amount
2. App generates UPI deeplink
3. System opens UPI apps chooser
4. User completes payment in UPI app
5. App receives result and shows status

## Requirements

- Android 7.0 (API 24) or higher
- UPI-enabled payment apps installed
- Internet permission (already configured)

## Build & Run

1. Open project in Android Studio
2. Sync Gradle files
3. Run on device/emulator
4. Test with UPI apps installed

## Testing

For testing purposes, you can:
- Use any amount value
- Test with real UPI apps
- Verify animations and UI flow
- Check payment status handling

## Notes

⚠️ **Important**: Update the UPI ID in `generateUpiDeeplink()` before production use!

📱 **UPI Apps**: The user must have at least one UPI-enabled app installed (Google Pay, PhonePe, Paytm, etc.)

🔄 **Payment Status**: The app assumes success when user returns, as UPI doesn't always provide reliable status callbacks.

## License

Free to use and modify for personal and commercial projects.

