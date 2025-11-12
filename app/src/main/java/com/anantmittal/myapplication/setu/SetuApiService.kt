package com.anantmittal.myapplication.setu

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

// Data Models
data class SetuAuthRequest(
    val clientID: String,
    val secret: String
)

data class SetuAuthResponse(
    val status: Int,
    val success: Boolean,
    val data: AuthData
)

data class AuthData(
    val token: String,
    val expiresIn: Int
)

data class CreatePaymentLinkRequest(
    val billerBillID: String,
    val amountExactness: String,
    val amount: Amount? = null,
    val name: String? = null,
    val expiryDate: String? = null,
    val transactionNote: String? = null
)

data class Amount(
    val currencyCode: String = "INR",
    val value: Int
)

data class CreatePaymentLinkResponse(
    val status: Int,
    val success: Boolean,
    val data: PaymentLinkData?
)

data class PaymentLinkData(
    val platformBillID: String,
    val name: String?,
    val paymentLink: PaymentLink
)

data class PaymentLink(
    val upiID: String,
    val upiLink: String,
    val shortURL: String
)

data class PaymentStatusResponse(
    val status: Int,
    val success: Boolean,
    val data: PaymentStatusData?
)

data class PaymentStatusData(
    val platformBillID: String,
    val billerBillID: String,
    val status: String,
    val amountPaid: Amount?,
    val receipt: Receipt?,
    val paymentLink: PaymentLink?,
    val createdAt: String?,
    val expiresAt: String?
)

data class Receipt(
    val id: String,
    val date: String
)

data class SetuErrorResponse(
    val status: Int,
    val success: Boolean,
    val error: ErrorData
)

data class ErrorData(
    val code: String,
    val title: String,
    val detail: String,
    val docURL: String?,
    val traceID: String
)

// Retrofit Interface
interface SetuApiService {

    @POST("auth/token")
    suspend fun fetchAuthToken(@Body request: SetuAuthRequest): Response<SetuAuthResponse>

    @POST("payment-links")
    suspend fun createPaymentLink(
        @Header("Authorization") authorization: String,
        @Header("X-Setu-Product-Instance-ID") productInstanceId: String,
        @Body request: CreatePaymentLinkRequest
    ): Response<CreatePaymentLinkResponse>

    @GET("payment-links/{platformBillId}")
    suspend fun getPaymentStatus(
        @Header("Authorization") authorization: String,
        @Header("X-Setu-Product-Instance-ID") productInstanceId: String,
        @Path("platformBillId") platformBillId: String
    ): Response<PaymentStatusResponse>
}

// Retrofit Client
object SetuApiClient {
    private const val BASE_URL = "https://uat.setu.co/api/v2/"

    val apiService: SetuApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SetuApiService::class.java)
    }
}

// Repository
class SetuRepository {
    private val api = SetuApiClient.apiService

    suspend fun getAuthToken(clientId: String, secret: String): Result<String> {
        return try {
            val response = api.fetchAuthToken(SetuAuthRequest(clientId, secret))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data.token)
            } else {
                Result.failure(Exception("Failed to get auth token: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPaymentLink(
        token: String,
        productInstanceId: String,
        request: CreatePaymentLinkRequest
    ): Result<PaymentLinkData> {
        return try {
            val response = api.createPaymentLink(
                "Bearer $token",
                productInstanceId,
                request
            )
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Failed to create payment link: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPaymentStatus(
        token: String,
        productInstanceId: String,
        platformBillId: String
    ): Result<PaymentStatusData> {
        return try {
            val response = api.getPaymentStatus(
                "Bearer $token",
                productInstanceId,
                platformBillId
            )
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Failed to get payment status: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

