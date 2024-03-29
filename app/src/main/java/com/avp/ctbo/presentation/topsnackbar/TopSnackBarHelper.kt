package com.avp.ctbo.presentation.topsnackbar

import android.os.Build
import androidx.annotation.RequiresApi
import com.avp.ctbo.framework.helpers.CoroutineLauncherHelper
import com.avp.ctbo.presentation.base.weak
import com.avp.ctbo.presentation.main.MainActivity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import timber.log.Timber

@DelicateCoroutinesApi
class TopSnackBarHelper : KoinComponent {

    private val mTopSnackBarsList = mutableListOf<ITopBarNotification>()
    private val mNotificationChannel by inject<MutableSharedFlow<ITopBarNotification>>(named("Notification"))
    private val mNotificationDismissChannel by inject<MutableSharedFlow<TopSnackBarType>>(named("NotificationDismiss"))
    private var mActivity: MainActivity? by weak()
    private var mCurrentTopSnackBar: TopSnackBar? = null
    private val mCoroutineLauncherHelper by inject<CoroutineLauncherHelper>()

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun init() {
        subscribeNotificationChannel()
        subscribeNotificationDismissChannel()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun setActivity(activity: MainActivity) {
        reset()
        mActivity = activity
        if (mTopSnackBarsList.isNotEmpty()) {
            mCurrentTopSnackBar = showNextTopSnackBar()
        }
    }

    fun reset() {
        mCurrentTopSnackBar?.removeTopSnackBar()
        mCurrentTopSnackBar = null
        mActivity = null
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun subscribeNotificationDismissChannel() {
        mCoroutineLauncherHelper.launch(Dispatchers.Main) {
            mNotificationDismissChannel
                .collect {
                    Timber.d("topSnackBar type: $it")
                    onDismiss(it)
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun subscribeNotificationChannel() {
        mCoroutineLauncherHelper.launch(Dispatchers.Main) {
            mNotificationChannel
                .filter { it.type != TopSnackBarType.UNKNOWN }
                .collect {
                    Timber.d("topSnackBar type: ${it.type.name}, current topSnackBar type: $mCurrentTopSnackBar")
                    handleNotification(it)
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun handleNotification(notification: ITopBarNotification) {
        mActivity?.let {
            if (it.isFinishing) {
                return
            }
            val topSnackBar = createTopSnackBar(it, notification)
            if (mTopSnackBarsList.isEmpty()) {
                topSnackBar.show()
                mCurrentTopSnackBar = topSnackBar
            }
        }
        if (!mTopSnackBarsList.any { it.type == notification.type } || !notification.unique) {
            mTopSnackBarsList.add(notification)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun onDismiss(topSnackBarType: TopSnackBarType) {
        mActivity?.let {
            if (!it.isFinishing) {
                val topSnackBarNotificationToRemove = getTopSnackBarNotificationToRemove(topSnackBarType)
                topSnackBarNotificationToRemove?.let { topBarNotification ->
                    if (mTopSnackBarsList.indexOf(topBarNotification) == 0) {
                        mCurrentTopSnackBar?.hide()
                    }
                    mTopSnackBarsList.remove(topBarNotification)
                    if (mTopSnackBarsList.isNotEmpty()) {
                        showNextTopSnackBar()
                    } else {
                        Timber.d("List is empty")
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun showNextTopSnackBar(): TopSnackBar? {
        var nextTopSnackBar: TopSnackBar? = null
        run {
            mTopSnackBarsList.forEach {
                Timber.d("Show next available topSnackBar in list: ${it.type}")
                mActivity?.let { activity ->
                    val newTopSnackBar = createTopSnackBar(activity, it)
                    newTopSnackBar.let { topSnackBar ->
                        topSnackBar.show()
                        nextTopSnackBar = topSnackBar
                        return@run
                    }
                }
            }
        }
        return nextTopSnackBar
    }

    private fun getTopSnackBarNotificationToRemove(topSnackBarType: TopSnackBarType): ITopBarNotification? {
        mTopSnackBarsList.forEach {
            if (it.type == topSnackBarType) {
                return it
            }
        }
        return null
    }

    private fun createTopSnackBar(activity: MainActivity,
                                  notification: ITopBarNotification): TopSnackBar {
        return TopSnackBar.createTopSnackBar(activity)
            .setIcon(notification.iconId)
            .setType(notification.type)
            .setText(activity.getString(notification.notificationTextResId))
            .setOnDismissListener(notification)
            .setHideTimeout(notification.notificationTimeout)
    }
}