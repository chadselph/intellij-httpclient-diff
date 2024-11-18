package me.chadrs.httpdiff.httpdiff

import com.intellij.httpClient.http.request.run.controller.HttpClientExecutionController
import com.intellij.openapi.components.Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

// Following this guide https://plugins.jetbrains.com/docs/intellij/launching-coroutines.html#using-runblockingcancellable
@Service
class HttpDiffApplicationService(private val cs: CoroutineScope) {

    fun runHttpRequests(
        req1: HttpClientExecutionController,
        req2: HttpClientExecutionController,
        onBothFinished: Runnable
    ): Job {

        return cs.launch {
            val res1 = cs.async { req1.execute() }
            val res2 = cs.async { req2.execute() }
            res1.await()
            res2.await()
            onBothFinished.run()
        }
    }

}