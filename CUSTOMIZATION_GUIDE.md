# Quick Customization Guide

## 🎯 Essential Customizations Before Using

### 1. Update Your UPI Details (REQUIRED!)

Open `PaymentScreen.kt` and find the `generateUpiDeeplink()` function at the bottom of the file:

```kotlin
fun generateUpiDeeplink(amount: Double): String {
    // CHANGE THESE VALUES:
    val upiId = "merchant@upi"           // ← Your UPI ID (e.g., yourname@paytm)
    val name = "Merchant Name"            // ← Your business/personal name
    val transactionNote = "Payment"       // ← Transaction description
    
    return "upi://pay?pa=$upiId&pn=${Uri.encode(name)}&tn=${Uri.encode(transactionNote)}&am=$amount&cu=INR"
}
```

**Example:**
```kotlin
val upiId = "john.doe@okaxis"
val name = "John's Store"
val transactionNote = "Order Payment"
```

---

## 🎨 Optional Customizations

### 2. Change Quick Amount Buttons

Find this line in `PaymentScreen.kt` (around line 151):

```kotlin
listOf(100, 500, 1000, 2000).forEach { quickAmount ->
```

**Change to your preferred amounts:**
```kotlin
listOf(50, 100, 250, 500).forEach { quickAmount ->
```

---

### 3. Customize Colors

#### Background Color:
```kotlin
// Line ~77 - Main background
.background(Color.Black)

// Change to dark gray:
.background(Color(0xFF121212))
```

#### Button Colors:
```kotlin
// Line ~256 - Pay button
containerColor = Color.White,
contentColor = Color.Black,

// For colored button:
containerColor = Color(0xFF4CAF50),  // Green
contentColor = Color.White,
```

#### Success/Failure Colors:
```kotlin
// Line ~387 - Status icons
if (status == PaymentStatus.Success)
    Color(0xFF4CAF50)  // Green
else
    Color(0xFFF44336)  // Red
```

---

### 4. Change App Name

Edit `app/src/main/res/values/strings.xml`:

```xml
<string name="app_name">Payment Deeplink Generator</string>
```

Change to:
```xml
<string name="app_name">Your App Name</string>
```

---

### 5. Adjust Loading Time

Find the verification simulation (around line 48):

```kotlin
delay(1500) // Simulate verification
```

Change to:
```kotlin
delay(1000) // Faster verification
```

---

### 6. Modify Input Constraints

Find amount validation (around line 125):

```kotlin
if (newValue.isEmpty() || newValue.matches(Regex("^\\d+\\.?\\d{0,2}$"))) {
    amount = newValue
}
```

**Allow up to 3 decimal places:**
```kotlin
if (newValue.isEmpty() || newValue.matches(Regex("^\\d+\\.?\\d{0,3}$"))) {
    amount = newValue
}
```

**Set maximum amount (e.g., ₹10,000):**
```kotlin
if (newValue.isEmpty() || 
    (newValue.matches(Regex("^\\d+\\.?\\d{0,2}$")) && 
     newValue.toDoubleOrNull()?.let { it <= 10000 } == true)) {
    amount = newValue
}
```

---

### 7. Add Transaction ID

Update the status dialog to show a transaction ID:

In `PaymentStatusDialog` composable, add:

```kotlin
Text(
    text = "Transaction ID: ${System.currentTimeMillis()}",
    fontSize = 12.sp,
    color = Color.Gray,
    fontFamily = FontFamily.Monospace
)
```

---

### 8. Change Currency

If you want to use a different currency, update:

```kotlin
// In generateUpiDeeplink() function:
&cu=INR  // Change INR to your currency code

// And update the rupee symbol throughout:
text = "₹"  // Change to your currency symbol
```

---

## 📱 Testing Tips

1. **Use Test UPI ID**: Start with a test/personal UPI ID
2. **Small Amounts**: Test with small amounts first (₹1, ₹10)
3. **Multiple UPI Apps**: Test with different UPI apps installed
4. **Network**: Ensure device has internet connection
5. **Permissions**: Check app has necessary permissions

---

## 🚀 Quick Start Checklist

- [ ] Update UPI ID in `generateUpiDeeplink()`
- [ ] Change merchant name
- [ ] Customize quick amount buttons (optional)
- [ ] Update app name in strings.xml
- [ ] Test with real UPI app
- [ ] Build and run on device

---

## 🔗 File Locations

- **Payment Logic**: `app/src/main/java/com/anantmittal/myapplication/PaymentScreen.kt`
- **Main Activity**: `app/src/main/java/com/anantmittal/myapplication/MainActivity.kt`
- **App Name**: `app/src/main/res/values/strings.xml`
- **Permissions**: `app/src/main/AndroidManifest.xml`

---

## ⚠️ Important Notes

1. **UPI ID Format**: Must be valid (username@bankname)
2. **Amount**: Cannot be zero or negative
3. **Internet**: Required for UPI transactions
4. **UPI Apps**: User must have at least one installed
5. **Testing**: Always test thoroughly before production

---

## 💡 Pro Tips

- Keep transaction notes short and clear
- Use meaningful merchant names
- Test on multiple devices
- Handle edge cases (no UPI apps, no internet)
- Consider adding analytics for tracking

---

## 🆘 Common Issues

**Issue**: UPI app doesn't open
- **Fix**: Check UPI ID format, ensure UPI app is installed

**Issue**: Payment status shows failed
- **Fix**: This is expected if user cancels, it's working correctly

**Issue**: App crashes on pay button
- **Fix**: Ensure all required permissions are granted

**Issue**: Amount not accepting input
- **Fix**: Check regex pattern in amount validation

---

**Ready to use!** Just update the UPI details and you're good to go! 🎉

