package com.rkhrapunov.versustest.framework.helpers

import com.rkhrapunov.versustest.framework.helpers.CustomDispatchers.singleCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import java.util.concurrent.Executors

object CustomDispatchers {
    val singleCoroutineDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
}

class CoroutineLauncherHelper : KoinComponent {

    private val parentJob = Job()

    private val coroutineExceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            coroutineScope.launch(Dispatchers.Main) { }
            GlobalScope.launch { println("Caught ${throwable.printStackTrace()}") }
        }

    private val coroutineScope = CoroutineScope(Dispatchers.Main + parentJob + coroutineExceptionHandler)

    fun launch(dispatcher: CoroutineDispatcher = Dispatchers.Default,
               action: suspend CoroutineScope.() -> Unit) = coroutineScope.launch(dispatcher, block = action)

    fun launchWithSingleCoroutineDispatcher(singleContextAction: suspend CoroutineScope.() -> Unit,
                                            coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
                                            coroutineAction: suspend () -> Unit = { }) =
        coroutineScope.launch(coroutineDispatcher) {
            withContext(singleCoroutineDispatcher, singleContextAction)
            coroutineAction()
        }

    fun launchImgLoading(action: suspend CoroutineScope.() -> Unit) =
        coroutineScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO, action)
        }
}