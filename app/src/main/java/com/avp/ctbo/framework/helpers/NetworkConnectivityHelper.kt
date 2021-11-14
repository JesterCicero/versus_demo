package com.avp.ctbo.framework.helpers

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import com.avp.ctbo.R
import com.avp.ctbo.presentation.topsnackbar.ITopBarNotification
import com.avp.ctbo.presentation.topsnackbar.TopBarNotification
import com.avp.ctbo.presentation.topsnackbar.TopSnackBarType
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import timber.log.Timber

@DelicateCoroutinesApi
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class NetworkConnectivityHelper : KoinComponent {

    private val mContext by inject<Context>()
    private var mOnline = false
    private var mConnectivityManager = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var callback = ConnectionStatusCallback()
    private val mNetworkStateChannel by inject<MutableSharedFlow<Boolean>>(named("NetworkState"))
    @DelicateCoroutinesApi
    private val mCoroutineLauncherHelper by inject<CoroutineLauncherHelper>()

    @Suppress("DEPRECATION")
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

    @DelicateCoroutinesApi
    inner class ConnectionStatusCallback : ConnectivityManager.NetworkCallback() {

        private val activeNetworks: MutableList<Network> = mutableListOf()
        private val mNotificationChannel by inject<MutableSharedFlow<ITopBarNotification>>(named("Notification"))
        private val mNotificationDismissChannel by inject<MutableSharedFlow<TopSnackBarType>>(named("NotificationDismiss"))

        override fun onLost(network: Network) {
            super.onLost(network)
            activeNetworks.removeAll { activeNetwork -> activeNetwork == network }
            mOnline = activeNetworks.isNotEmpty()
            Timber.d("onLost(): online: $mOnline")
            mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                mNotificationChannel.emit(TopBarNotification(
                    TopSnackBarType.NO_CONNECTION,
                    R.string.no_internet_connection,
                    R.drawable.ic_no_internet,
                    unique = true,
                    notificationTimeout = NO_CONNECTION_TIMEOUT_MS,
                    action = {
                        mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                            mNotificationDismissChannel.emit(TopSnackBarType.NO_CONNECTION)
                        }
                    }
                ))
            }
            mCoroutineLauncherHelper.launchWithSingleCoroutineDispatcher({ mNetworkStateChannel.emit(mOnline) })
        }

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            if (activeNetworks.none { activeNetwork -> activeNetwork == network }) {
                activeNetworks.add(network)
            }
            mOnline = activeNetworks.isNotEmpty()
            Timber.d("onAvailable(): online: $mOnline")
            mCoroutineLauncherHelper.launch(Dispatchers.Main) {
                mNotificationDismissChannel.emit(TopSnackBarType.NO_CONNECTION)
            }
            mCoroutineLauncherHelper.launchWithSingleCoroutineDispatcher({ mNetworkStateChannel.emit(mOnline) })
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