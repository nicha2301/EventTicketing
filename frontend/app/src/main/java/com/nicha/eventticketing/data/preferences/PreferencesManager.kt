package com.nicha.eventticketing.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import java.io.IOException
import java.security.GeneralSecurityException

/**
 * Interface quản lý ưu tiên của ứng dụng, bao gồm token xác thực và cài đặt người dùng
 */
interface PreferencesManager {
    /**
     * Lưu token xác thực
     * @param token Token để lưu trữ
     * @return True nếu lưu thành công, false nếu thất bại
     */
    suspend fun saveAuthToken(token: String): Boolean
    
    /**
     * Lấy token xác thực hiện tại
     * @return Flow chứa token hoặc null nếu không tìm thấy
     */
    fun getAuthToken(): Flow<String?>
    
    /**
     * Lấy token xác thực hiện tại (phương thức đồng bộ)
     * @return Token hiện tại hoặc null nếu không tìm thấy
     */
    fun getAuthTokenSync(): String?
    
    /**
     * Xóa token xác thực
     * @return True nếu xóa thành công, false nếu thất bại
     */
    suspend fun clearAuthToken(): Boolean
    
    /**
     * Đặt trạng thái giao diện tối
     * @param enabled True nếu đang bật chế độ tối
     */
    suspend fun setDarkThemeEnabled(enabled: Boolean)
    
    /**
     * Kiểm tra trạng thái giao diện tối
     * @return Flow của trạng thái chế độ tối
     */
    fun isDarkThemeEnabled(): Flow<Boolean>
    
    /**
     * Đặt trạng thái đã hoàn thành onboarding
     * @param completed True nếu đã hoàn thành onboarding
     */
    suspend fun setOnboardingCompleted(completed: Boolean)
    
    /**
     * Kiểm tra trạng thái onboarding
     * @return Flow của trạng thái onboarding
     */
    fun isOnboardingCompleted(): Flow<Boolean>
    
    /**
     * Xóa tất cả dữ liệu preferences
     */
    suspend fun clearAllPreferences()
}

/**
 * Triển khai của PreferencesManager sử dụng DataStore và EncryptedSharedPreferences
 */
class PreferencesManagerImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val context: Context
) : PreferencesManager {

    companion object {
        // Các khóa cho preferences
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val DARK_THEME = booleanPreferencesKey("dark_theme")
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        
        // Thiết lập EncryptedSharedPreferences
        private const val ENCRYPTED_PREFS_FILE_NAME = "encrypted_user_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
    }
    
    /**
     * Tạo MasterKey để mã hóa preferences
     */
    private val masterKey by lazy {
        try {
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
        } catch (e: GeneralSecurityException) {
            Timber.e(e, "Lỗi bảo mật khi tạo MasterKey")
            null
        } catch (e: IOException) {
            Timber.e(e, "Lỗi IO khi tạo MasterKey")
            null
        } catch (e: Exception) {
            Timber.e(e, "Lỗi không xác định khi tạo MasterKey")
            null
        }
    }
    
    /**
     * Khởi tạo EncryptedSharedPreferences hoặc sử dụng SharedPreferences thông thường nếu không thành công
     */
    private val encryptedPrefs by lazy {
        try {
            if (masterKey != null) {
                EncryptedSharedPreferences.create(
                    context,
                    ENCRYPTED_PREFS_FILE_NAME,
                    masterKey!!,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } else {
                // Fallback to regular SharedPreferences if encryption fails
                Timber.w("Sử dụng SharedPreferences thông thường vì không thể tạo EncryptedSharedPreferences")
                context.getSharedPreferences(ENCRYPTED_PREFS_FILE_NAME, Context.MODE_PRIVATE)
            }
        } catch (e: GeneralSecurityException) {
            Timber.e(e, "Lỗi bảo mật khi tạo EncryptedSharedPreferences")
            context.getSharedPreferences(ENCRYPTED_PREFS_FILE_NAME, Context.MODE_PRIVATE)
        } catch (e: IOException) {
            Timber.e(e, "Lỗi IO khi tạo EncryptedSharedPreferences")
            context.getSharedPreferences(ENCRYPTED_PREFS_FILE_NAME, Context.MODE_PRIVATE)
        } catch (e: Exception) {
            Timber.e(e, "Lỗi không xác định khi tạo EncryptedSharedPreferences")
            context.getSharedPreferences(ENCRYPTED_PREFS_FILE_NAME, Context.MODE_PRIVATE)
        }
    }
    
    override suspend fun saveAuthToken(token: String): Boolean {
        return try {
            // Sử dụng commit() thay vì apply() để đảm bảo token được lưu ngay lập tức
            val result = encryptedPrefs.edit().putString(KEY_AUTH_TOKEN, token).commit()
            if (result) {
                Timber.d("Token đã được lưu thành công")
            } else {
                Timber.e("Không thể lưu token")
            }
            result
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi lưu token")
            false
        }
    }
    
    override fun getAuthToken(): Flow<String?> {
        return flow {
            Timber.d("Đang đọc token từ EncryptedSharedPreferences")
            val token = encryptedPrefs.getString(KEY_AUTH_TOKEN, null)
            if (!token.isNullOrEmpty()) {
                Timber.d("Đọc token thành công: ${token.take(10)}...") 
            } else {
                Timber.d("Không tìm thấy token")
            }
            emit(token)
        }.catch { exception ->
            when (exception) {
                is SecurityException -> 
                    Timber.e(exception, "Lỗi bảo mật khi đọc token")
                is IOException ->
                    Timber.e(exception, "Lỗi IO khi đọc token")
                else -> 
                    Timber.e(exception, "Lỗi không xác định khi đọc token")
            }
            emit(null)
        }
    }
    
    override fun getAuthTokenSync(): String? {
        return encryptedPrefs.getString(KEY_AUTH_TOKEN, null)
    }
    
    override suspend fun clearAuthToken(): Boolean {
        return try {
            val result = encryptedPrefs.edit().remove(KEY_AUTH_TOKEN).commit()
            if (result) {
                Timber.d("Token đã được xóa thành công")
            } else {
                Timber.e("Không thể xóa token")
            }
            result
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi xóa token")
            false
        }
    }
    
    override suspend fun setDarkThemeEnabled(enabled: Boolean) {
        try {
        dataStore.edit { preferences ->
            preferences[DARK_THEME] = enabled
            }
            Timber.d("Đã lưu cài đặt chế độ tối: $enabled")
        } catch (e: IOException) {
            Timber.e(e, "Lỗi khi lưu cài đặt chế độ tối")
        }
    }
    
    override fun isDarkThemeEnabled(): Flow<Boolean> {
        return dataStore.data
            .map { preferences -> preferences[DARK_THEME] ?: false }
            .catch { exception ->
                Timber.e(exception, "Lỗi khi đọc cài đặt chế độ tối")
                emit(false)
        }
    }
    
    override suspend fun setOnboardingCompleted(completed: Boolean) {
        try {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
            }
            Timber.d("Đã lưu trạng thái onboarding: $completed")
        } catch (e: IOException) {
            Timber.e(e, "Lỗi khi lưu trạng thái onboarding")
        }
    }
    
    override fun isOnboardingCompleted(): Flow<Boolean> {
        return dataStore.data
            .map { preferences -> preferences[ONBOARDING_COMPLETED] ?: false }
            .catch { exception ->
                Timber.e(exception, "Lỗi khi đọc trạng thái onboarding")
                emit(false)
        }
    }
    
    override suspend fun clearAllPreferences() {
        try {
            // Xóa DataStore
            dataStore.edit { it.clear() }
            // Xóa EncryptedSharedPreferences
            encryptedPrefs.edit().clear().commit()
            Timber.d("Đã xóa tất cả preferences")
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi xóa tất cả preferences")
        }
    }
} 