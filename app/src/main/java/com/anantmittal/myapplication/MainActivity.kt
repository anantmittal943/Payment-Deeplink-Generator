package com.anantmittal.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anantmittal.myapplication.ui.theme.PaymentDeeplinkGeneratorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PaymentDeeplinkGeneratorTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNavigateToUpiPayment = { navController.navigate("upi_payment") },
                onNavigateToSetuPayment = { navController.navigate("setu_payment") }
            )
        }
        composable("upi_payment") {
            PaymentScreen()
        }
        composable("setu_payment") {
            SetuPaymentLinkScreen()
        }
    }
}
