# Setu Payment Link Integration - Setup Guide

## ✅ What's Been Implemented

I've created a complete Setu API integration for generating dynamic payment links with the following features:

### 📁 New Files Created:

1. **SetuApiService.kt** - Complete Retrofit API client with:
   - Authentication API (`/auth/token`)
   - Create Payment Link API (`/payment-links`)
   - Get Payment Status API (`/payment-links/{platformBillId}`)
   - All data models and error handling

2. **SetuPaymentLinkScreen.kt** - Modern UI screen with:
   - API credentials management
   - Payment link generation form
   - Generated link display with copy/share options
   - Material 3 Expressive design
   - Loading states and error handling

3. **HomeScreen.kt** - Navigation hub with:
   - Choice between UPI Direct Payment and Setu Payment Link
   - Beautiful card-based navigation
   - Animated transitions

### 🎨 Features

#### Setu Payment Link Screen:
- ✅ **API Credentials Setup** - Dialog to configure Setu credentials
- ✅ **Payment Form** - Fields for:
  - Bill ID (required)
  - Amount in paise (required)
  - Merchant Name (optional)
  - Transaction Note (max 80 chars)
- ✅ **Generate Link** - Creates payment link via Setu API
- ✅ **Display Results** - Shows:
  - Platform Bill ID
  - UPI ID
  - Short URL
  - Copy and Share buttons
- ✅ **Error Handling** - Clear error messages

#### Home Screen:
- ✅ **Two Payment Options**:
  1. UPI Direct Payment (your existing screen)
  2. Setu Payment Link (new screen)
- ✅ **Modern Navigation** - Card-based selection
- ✅ **Expressive Design** - Asymmetric shapes and animations

---

## 🚀 Setup Instructions

### Step 1: Sync Gradle Dependencies

The following dependencies have been added to `build.gradle.kts`:

```kotlin
// Retrofit for API calls
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.google.code.gson:gson:2.10.1")

// Navigation
implementation("androidx.navigation:navigation-compose:2.7.5")
```

**In Android Studio:**
1. Click "Sync Now" in the banner at the top
2. Wait for Gradle sync to complete

### Step 2: Get Setu API Credentials

You need to get your Setu API credentials from the Setu Dashboard:

1. Go to [Setu Dashboard](https://bridge.setu.co/)
2. Sign up / Log in
3. Navigate to **API Keys** section
4. Copy:
   - `Client ID`
   - `Client Secret`
   - `Product Instance ID`

### Step 3: Configure Credentials in App

When you run the app:

1. Open the app on your device
2. Select **"Setu Payment Link"** from home screen
3. Tap **"⚙️ Setup API Credentials"** button
4. Enter your credentials:
   - Client ID
   - Client Secret
   - Product Instance ID
5. Tap **"Save"**

---

## 📱 How to Use

### Creating a Payment Link:

1. **Open App** → Select "Setu Payment Link"
2. **Setup Credentials** (first time only)
3. **Fill Payment Details**:
   ```
   Bill ID: Any unique identifier (e.g., "ORDER-123")
   Amount: In paise (e.g., 10000 for ₹100)
   Merchant Name: Optional (e.g., "My Store")
   Transaction Note: Optional (e.g., "Payment for order")
   ```
4. **Tap "Generate Payment Link"**
5. **View Generated Link**:
   - Copy UPI ID
   - Copy Short URL
   - Tap "Pay Now" to open UPI apps
   - Share the link with customers

### Example Usage:

**Scenario**: Create a payment link for ₹250

```
Bill ID: "ORDER-789"
Amount: 25000 (₹250 in paise)
Merchant Name: "Coffee Shop"
Transaction Note: "Coffee order payment"
```

**Result**: You'll get a payment link like:
- UPI ID: `merchant@setu`
- Short URL: `https://bills.setu.co/xyz123`
- UPI Link: `upi://pay?pa=merchant@setu&am=250.00...`

---

## 🔄 API Flow

### 1. Authentication:
```
POST /api/v2/auth/token
Body: { clientID, secret }
Response: { token, expiresIn }
```

### 2. Create Payment Link:
```
POST /api/v2/payment-links
Headers:
  - Authorization: Bearer {token}
  - X-Setu-Product-Instance-ID: {productInstanceId}
Body: {
  billerBillID: "ORDER-123",
  amountExactness: "EXACT",
  amount: { value: 10000, currencyCode: "INR" },
  name: "Merchant Name",
  transactionNote: "Payment note"
}
Response: {
  platformBillID: "...",
  paymentLink: {
    upiID: "...",
    upiLink: "...",
    shortURL: "..."
  }
}
```

### 3. Check Payment Status (Optional):
```
GET /api/v2/payment-links/{platformBillId}
Headers:
  - Authorization: Bearer {token}
  - X-Setu-Product-Instance-ID: {productInstanceId}
Response: {
  status: "PAYMENT_SUCCESSFUL" | "PAYMENT_FAILED" | "BILL_CREATED",
  amountPaid: { ... },
  receipt: { ... }
}
```

---

## 🎯 Setu API Parameters

### Amount Exactness:
- `EXACT` - Customer must pay exact amount
- `EXACT_UP` - Customer can pay exact or more
- `EXACT_DOWN` - Customer can pay exact or less
- `ANY` - Customer can pay any amount
- `RANGE` - Amount within a range

### Payment Status:
- `BILL_CREATED` - Link created, not paid yet
- `PAYMENT_SUCCESSFUL` - Payment completed
- `PAYMENT_FAILED` - Payment failed
- `CREDIT_RECEIVED` - Payment received
- `BILL_EXPIRED` - Link expired

---

## 🛠️ Customization

### Change API Base URL:

In `SetuApiService.kt`:
```kotlin
object SetuApiClient {
    // For Production:
    private const val BASE_URL = "https://prod.setu.co/api/v2/"
    
    // For UAT/Sandbox:
    private const val BASE_URL = "https://uat.setu.co/api/v2/"
}
```

### Store Credentials Securely:

Currently, credentials are stored in memory. For production:

1. Use **EncryptedSharedPreferences**:
```kotlin
implementation("androidx.security:security-crypto:1.1.0-alpha06")
```

2. Or use **Android Keystore** for sensitive data

### Add Payment Status Polling:

To automatically check payment status:

```kotlin
// In SetuPaymentLinkScreen.kt
LaunchedEffect(generatedLink) {
    if (generatedLink != null) {
        delay(5000) // Wait 5 seconds
        // Call repository.getPaymentStatus()
        // Update UI based on status
    }
}
```

---

## 📊 UI Components

### Home Screen:
- **Payment Solutions** title
- **Two Navigation Cards**:
  1. UPI Direct Payment (Green, 💳)
  2. Setu Payment Link (Blue, 🔗)

### Setu Screen:
- **Title**: "Setu Payment Link"
- **Credentials Button**: Setup/Update API keys
- **Payment Details Card**: Input form
- **Generate Button**: Creates payment link
- **Result Card**: Shows generated link details
- **Error Messages**: Red card for failures

---

## ✅ Testing Checklist

1. **Setup**:
   - [ ] Gradle sync completed
   - [ ] App builds successfully
   - [ ] App installs on device

2. **Credentials**:
   - [ ] Can open credentials dialog
   - [ ] Can save credentials
   - [ ] Credentials persist during session

3. **Generate Link**:
   - [ ] Can enter Bill ID
   - [ ] Can enter amount
   - [ ] Can add optional fields
   - [ ] Generate button enables when required fields filled
   - [ ] Loading state shows while generating
   - [ ] Success shows generated link
   - [ ] Error shows if API fails

4. **Use Link**:
   - [ ] Can copy UPI ID
   - [ ] Can copy Short URL
   - [ ] "Pay Now" opens UPI apps
   - [ ] Link works in UPI apps

---

## 🔐 Security Notes

⚠️ **Important for Production:**

1. **Never hardcode credentials** in source code
2. **Use environment variables** or secure storage
3. **Implement token caching** to reduce API calls
4. **Add request/response logging** for debugging
5. **Implement retry logic** for network failures
6. **Validate all user inputs** before API calls
7. **Use HTTPS only** for API calls
8. **Implement rate limiting** on client side

---

## 📚 Setu API Documentation

- **Official Docs**: https://docs.setu.co/
- **API Reference**: https://docs.setu.co/collect/api-reference
- **Swagger**: https://uat.setu.co/api/v2/swagger

---

## 🎉 You're All Set!

The Setu payment link integration is complete. Just sync Gradle, enter your API credentials, and you're ready to generate dynamic payment links!

**Need Help?**
- Check Setu Dashboard for API status
- Review API logs in Setu Dashboard
- Test with small amounts first
- Use UAT environment for testing

Happy Coding! 🚀

