package com.nicha.eventticketing.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import timber.log.Timber

object NetworkStatusObserver {
    /**
     * Flow phát ra true nếu có mạng, false nếu offline
     * Phát hiện thay đổi ngay lập tức
     */
    fun observe(context: Context): Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        fun isConnected(): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork ?: return false
                val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
                val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                return hasInternet
            } else {
                val networkInfo = connectivityManager.activeNetworkInfo
                val isConnected = networkInfo != null && networkInfo.isConnected
                return isConnected
            }
        }

        // Gửi trạng thái ban đầu
        val initialState = isConnected()
        trySend(initialState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    trySend(true)
                }
                
                override fun onLost(network: Network) {
                    // Check the current state because this might be called just for one network
                    val currentState = isConnected()
                    trySend(currentState)
                }
                
                override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                    val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    trySend(hasInternet)
                }
            }
            
            // Đăng ký NetworkCallback với tất cả các loại mạng để phát hiện nhanh nhất
            val request = NetworkRequest.Builder().build()
            connectivityManager.registerNetworkCallback(request, callback)
            
            awaitClose { 
                connectivityManager.unregisterNetworkCallback(callback)
            }
        } else {
            // Fallback cho API thấp hơn
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    trySend(isConnected())
                }
            }
            val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            context.registerReceiver(receiver, filter)
            awaitClose { 
                context.unregisterReceiver(receiver)
            }
        }
    }.distinctUntilChanged()
} 