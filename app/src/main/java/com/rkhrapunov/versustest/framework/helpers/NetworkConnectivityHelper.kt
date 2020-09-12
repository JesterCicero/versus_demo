package com.rkhrapunov.versustest.framework.helpers

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import com.rkhrapunov.versustest.R
import com.rkhrapunov.versustest.presentation.topsnackbar.ITopBarNotification
import com.rkhrapunov.versustest.presentation.topsnackbar.TopBarNotification
import com.rkhrapunov.versustest.presentation.topsnackbar.TopSnackBarType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import timber.log.Timber

@ExperimentalCoroutinesApi
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class NetworkConnectivityHelper : KoinComponent {

    private val mContext by inject<Context>()
    private var mOnline = false
    private var mConnectivityManager = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var callback = ConnectionStatusCallback()
    @ExperimentalCoroutinesApi
    private val mNetworkStateChannel by inject<BroadcastChannel<Boolean>>(named("NetworkState"))
    private val mCoroutineLauncherHelper by inject<CoroutineLauncherHelper>()

    private fun getInitialConnectionStatus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = mConnectivityManager.activeNetwork
            val capabilities = mConnectivityManager.getNetworkCapabilities(network)
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
        } else {
            val activeNetwork = mConnectivityManager.activeNetworkInfo // Deprecated in 29
            activeNetwork != null && activeNetwork.isConnectedOrConnecting // // Deprecated in 28
        }
    }

    @ExperimentalCoroutinesApi
    inner class ConnectionStatusCallback : ConnectivityManager.NetworkCallback() {

        private val activeNetworks: MutableList<Network> = mutableListOf()
        private val mNotificationChannel by inject<BroadcastChannel<ITopBarNotification>>(named("Notification"))
        private val mNotificationDismissChannel by inject<BroadcastChannel<TopSnackBarType>>(named("NotificationDismiss"))

        override fun onLost(network: Network) {
            super.onLost(network)
            activeNetworks.removeAll { activeNetwork -> activeNetwork == network }
            mOnline = activeNetworks.isNotEmpty()
            Timber.d("onLost(): online: $mOnline")
            mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                mNotificationChannel.send(TopBarNotification(
                    TopSnackBarType.NO_CONNECTION,
                    R.string.no_internet_connection,
                    R.drawable.ic_no_internet,
                    notificationTimeout = NO_CONNECTION_TIMEOUT_MS,
                    action = {
                        mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                            mNotificationDismissChannel.send(TopSnackBarType.NO_CONNECTION)
                        }
                    }
                ))
            }
            mCoroutineLauncherHelper.launchWithSingleCoroutineDispatcher({ mNetworkStateChannel.send(mOnline) })
        }

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            if (activeNetworks.none { activeNetwork -> activeNetwork == network }) {
                activeNetworks.add(network)
            }
            mOnline = activeNetworks.isNotEmpty()
            Timber.d("onAvailable(): online: $mOnline")
            mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                mNotificationDismissChannel.send(TopSnackBarType.NO_CONNECTION)
            }
            mCoroutineLauncherHelper.launchWithSingleCoroutineDispatcher({ mNetworkStateChannel.send(mOnline) })
        }
    }

    fun subscribeNetworkStatus() {
        mOnline = getInitialConnectionStatus()
        try {
            mConnectivityManager.unregisterNetworkCallback(callback)
        } catch (e: Exception) {
            Timber.w("NetworkCallback for Wi-fi was not registered or already unregistered\"")
        }
        mConnectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), callback)
    }

    companion object {
        private const val NO_CONNECTION_TIMEOUT_MS = 15000
    }
}