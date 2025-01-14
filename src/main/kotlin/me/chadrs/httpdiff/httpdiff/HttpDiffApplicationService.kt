package me.chadrs.httpdiff.httpdiff

import com.intellij.httpClient.http.request.environment.HttpRequestEnvironment
import com.intellij.httpClient.http.request.run.HttpClientRequestProcessHandler
import com.intellij.httpClient.http.request.run.HttpRequestHistoryManager
import com.intellij.httpClient.http.request.run.HttpRequestResponseFileResult
import com.intellij.httpClient.http.request.run.console.HttpResponseConsole
import com.intellij.httpClient.http.request.run.controller.HttpClientExecutionController
import com.intellij.httpClient.http.request.run.info.HttpRunRequestInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// Following this guide https://plugins.jetbrains.com/docs/intellij/launching-coroutines.html#using-runblockingcancellable
@Service
class HttpDiffApplicationService(private val cs: CoroutineScope) {

    fun runHttpRequests(
        env1: HttpRequestEnvironment,
        env2: HttpRequestEnvironment,
        runConfig: HttpDiffRunConfiguration,
        project: Project,
        processHandler: HttpClientRequestProcessHandler,
        console: HttpResponseConsole,
        onBothFinished: (VirtualFile?, VirtualFile?) -> Unit
    ): Job? {
        val runReq1 = runConfig.getRequestInfo(env1)
        val runReq2 = runConfig.getRequestInfo(env2)
        if (runReq1 != null && runReq2 != null) {
            return cs.launch {
                val resp1 = createHttpClientExecutionController(
                    project,
                    runConfig,
                    runReq1,
                    processHandler,
                    console,
                    runReq1.makeName(env1)
                ).await()
                // it would be nice to run these in parallel, but it causes some issues with the intellij client,
                // such as duplicating the resulting file name since it's based on the timestamp
                // So, we just do them sequentially for now, until I can figure out how to overcome these.
                val resp2 = createHttpClientExecutionController(
                    project,
                    runConfig,
                    runReq2,
                    processHandler,
                    console,
                    runReq2.makeName(env2)
                ).await()
                onBothFinished.invoke(resp1, resp2)
            }
        } else return null
    }


    private suspend fun createHttpClientExecutionController(
        project: Project,
        runConfig: HttpDiffRunConfiguration,
        info: HttpRunRequestInfo,
        processHandler: HttpClientRequestProcessHandler,
        console: HttpResponseConsole,
        id: String,
    ): Deferred<VirtualFile?> {
        val completer = CompletableDeferred<VirtualFile?>()
        HttpClientExecutionController(
            project,
            info,
            id,
            info.requestPointer,
            processHandler,
            console,
            info.responseHandler,
            { resp ->
                ApplicationManager.getApplication().invokeLater {
                    info.postProcessor.onResponseExecuted(resp)
                    completer.complete(getFileFromResp(resp, project))
                }
            },
            true,
            {
                ApplicationManager.getApplication().invokeLater {
                    console.onRequestEnd(id)
                }
            },
            info.ignoreMessage,
            runConfig.toHttpSettings(),
            false
        ).execute()
        return completer
    }

    private fun getFileFromResp(resp: HttpRequestResponseFileResult?, project: Project): VirtualFile? {
        return if (resp is HttpRequestResponseFileResult.ExactFile && resp.fileName != null) {
            HttpRequestHistoryManager.findFile(project, resp.fileName!!, true)
        } else {
            null
        }
    }

    private fun HttpRunRequestInfo.makeName(env: HttpRequestEnvironment): String {
        return """[${env.environmentName}] ${this.requestMethod} ${this.getRequestURL() ?: ""}"""

    }
}