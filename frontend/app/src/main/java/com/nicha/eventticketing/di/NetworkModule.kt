package com.nicha.eventticketing.di

import com.nicha.eventticketing.config.AppConfig
import com.nicha.eventticketing.data.preferences.PreferencesManager
import com.nicha.eventticketing.data.remote.interceptor.AuthInterceptor
import com.nicha.eventticketing.data.remote.service.ApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    // SSL Pinning - Thay thế các giá trị này bằng certificate hash thực tế của bạn
    // Lưu ý: Vì chúng ta đang sử dụng http cho localhost/emulator, SSL pinning chỉ áp dụng khi chuyển sang https
    private val certificatePinner = CertificatePinner.Builder()
        .add("api.eventticketing.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=") // Thay thế bằng hash thực tế
        .add("api.eventticketing.com", "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=") // Backup hash
        .build()

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(com.nicha.eventticketing.data.remote.adapter.TicketTypeAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (AppConfig.FeatureFlags.ENABLE_DEBUG_LOGGING) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.BASIC
            }
        }
    }
    
    /**
     * Interceptor để retry khi request thất bại
     */
    @Provides
    @Singleton
    fun provideRetryInterceptor(): Interceptor {
        return object : Interceptor {
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): Response {
                val request = chain.request()
                var response: Response? = null
                var exception: IOException? = null
                
                // Thử lại tối đa MAX_RETRIES lần
                var retryCount = 0
                
                while (retryCount < AppConfig.Api.MAX_RETRIES && (response == null || !response.isSuccessful)) {
                    try {
                        if (retryCount > 0) {
                            Timber.d("Đang thử lại request lần thứ $retryCount")
                            // Tăng thời gian chờ trước mỗi lần thử lại (exponential backoff)
                            val waitTime = (Math.pow(2.0, retryCount.toDouble()) * 100).toLong()
                            Thread.sleep(waitTime)
                        }
                        
                        // Không retry các request POST, PUT, DELETE
                        if (retryCount > 0 && (request.method == "POST" || request.method == "PUT" || request.method == "DELETE")) {
                            Timber.d("Không thử lại request ${request.method}")
                            break
                        }
                        
                        // Đóng response cũ nếu có
                        response?.close()
                        
                        // Thực hiện request mới
                        response = chain.proceed(request)
                        
                        // Nếu response là 5xx, thử lại
                        if (response.code in 500..599) {
                            response.close()
                            retryCount++
                            continue
                        }
                        
                        // Nếu thành công hoặc lỗi client (4xx), không thử lại nữa
                        return response
                        
                    } catch (e: SocketTimeoutException) {
                        // Chỉ thử lại nếu bị timeout
                        exception = e
                        Timber.e(e, "Timeout, đang thử lại")
                        retryCount++
                    } catch (e: IOException) {
                        // Lỗi IO khác, ghi log và thử lại
                        exception = e
                        Timber.e(e, "Lỗi IO, đang thử lại")
                        retryCount++
                    }
                }
                
                // Nếu vẫn còn response, trả về nó
                if (response != null) {
                    return response
                }
                
                // Nếu không có response và có exception, ném lại exception
                if (exception != null) {
                    throw exception
                }
                
                // Trường hợp không nên xảy ra
                throw IOException("Không thể thực hiện request sau ${AppConfig.Api.MAX_RETRIES} lần thử lại")
            }
        }
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(preferencesManager: PreferencesManager): AuthInterceptor {
        return AuthInterceptor(preferencesManager)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        retryInterceptor: Interceptor
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(retryInterceptor) // Thêm retry interceptor đầu tiên
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(AppConfig.Api.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(AppConfig.Api.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(AppConfig.Api.WRITE_TIMEOUT, TimeUnit.SECONDS)
            
        // Chỉ áp dụng certificatePinner khi sử dụng HTTPS
        if (AppConfig.Api.API_BASE_URL.startsWith("https")) {
            builder.certificatePinner(certificatePinner)
        }
        
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AppConfig.Api.API_BASE_URL) // URL cho Android Emulator để kết nối đến localhost máy chủ
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
} 