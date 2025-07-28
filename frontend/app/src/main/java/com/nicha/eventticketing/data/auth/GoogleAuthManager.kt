package com.nicha.eventticketing.data.auth

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Quản lý xác thực Google
 */
@Singleton
class GoogleAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private lateinit var googleSignInClient: GoogleSignInClient
    
    init {
        val clientId = context.getString(com.nicha.eventticketing.R.string.google_web_client_id)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(clientId)
            .requestEmail()
            .requestProfile()
            .build()
            
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }
    
    /**
     * Lấy intent đăng nhập Google
     */
    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }
    
    /**
     * Xử lý kết quả đăng nhập Google
     */
    fun handleSignInResult(result: ActivityResult): GoogleSignInResult {
        if (result.resultCode != -1) {
            return GoogleSignInResult.Cancelled
        }

        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            GoogleSignInResult.Success(account)
        } catch (e: ApiException) {
            GoogleSignInResult.Error("Đăng nhập Google thất bại: mã lỗi ${e.statusCode}")
        } catch (e: Exception) {
            GoogleSignInResult.Error("Lỗi không xác định: ${e.localizedMessage}")
        }
    }
    
    /**
     * Đăng xuất khỏi Google
     */
    fun signOut() {
        googleSignInClient.signOut().addOnCompleteListener { signOutTask ->
            googleSignInClient.revokeAccess().addOnCompleteListener { revokeTask ->
            }
        }
    }
}

/**
 * Kết quả đăng nhập Google
 */
sealed class GoogleSignInResult {
    data class Success(val account: GoogleSignInAccount) : GoogleSignInResult()
    data class Error(val message: String) : GoogleSignInResult()
    object Cancelled : GoogleSignInResult()
}