package com.nicha.eventticketing.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enum đại diện cho các trạng thái mạng
 */
enum class NetworkState {
    AVAILABLE,
    UNAVAILABLE,
    LOSING,
    LOST
}

/**
 * NetworkStatus theo dõi trạng thái kết nối mạng và cung cấp thông tin về khả năng kết nối
 */
@Singleton
class NetworkStatus @Inject constructor(
    private val context: Context
) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val _networkState = MutableStateFlow(getInitialNetworkState())
    val networkState: StateFlow<NetworkState> = _networkState

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _networkState.value = NetworkState.AVAILABLE
        }

        override fun onLosing(network: Network, maxMsToLive: Int) {
            _networkState.value = NetworkState.LOSING
        }

        override fun onLost(network: Network) {
            _networkState.value = NetworkState.LOST
        }

        override fun onUnavailable() {
            _networkState.value = NetworkState.UNAVAILABLE
        }
    }

    init {
        registerNetworkCallback()
    }

    private fun getInitialNetworkState(): NetworkState {
        return if (isNetworkAvailable()) NetworkState.AVAILABLE else NetworkState.UNAVAILABLE
    }

    /**
     * Kiểm tra xem mạng có khả dụng không
     */
    fun isNetworkAvailable(): Boolean {
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return networkCapabilities != null &&
                (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
    }

    /**
     * Đăng ký theo dõi thay đổi kết nối mạng
     */
    private fun registerNetworkCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    /**
     * Hủy đăng ký theo dõi thay đổi kết nối mạng
     */
    fun unregisterNetworkCallback() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
} 