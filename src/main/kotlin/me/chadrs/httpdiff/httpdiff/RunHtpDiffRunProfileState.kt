package me.chadrs.httpdiff.httpdiff

import com.intellij.diff.DiffContentFactory
import com.intellij.diff.DiffManager
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ProgramRunner
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties
import com.intellij.httpClient.http.request.environment.HttpRequestEnvironment
import com.intellij.httpClient.http.request.run.HttpClientExecutionController
import com.intellij.httpClient.http.request.run.HttpClientRequestProcessHandler
import com.intellij.httpClient.http.request.run.HttpRequestHistoryManager
import com.intellij.httpClient.http.request.run.HttpRequestPostProcessor
import com.intellij.httpClient.http.request.run.HttpRequestResponseFileResult
import com.intellij.httpClient.http.request.run.HttpRunRequestInfo
import com.intellij.httpClient.http.request.run.console.HttpResponseConsole
import com.intellij.httpClient.http.request.run.test.HttpMultiResponseConsole
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile


/**
 * Highly based on [com.intellij.httpClient.http.request.run.RunHttpRequestProfileState]
 */
class RunHtpDiffRunProfileState(
    private val env1: HttpRequestEnvironment,
    private val env2: HttpRequestEnvironment,
    private val project: Project,
    private val runConfig: HttpDiffRunConfiguration
) : RunProfileState {
    override fun execute(executor: Executor?, runner: ProgramRunner<*>): ExecutionResult? {
        val smtRunnerConsoleProperties = SMTRunnerConsoleProperties(runConfig, "HTTP", executor!!)
        val processHandler = HttpClientRequestProcessHandler(true)
        val console: HttpResponseConsole = HttpMultiResponseConsole(
            project, smtRunnerConsoleProperties, processHandler
        )

        val runReq1 = runConfig.getRequestInfo(env1)
        val runReq2 = runConfig.getRequestInfo(env2)

        var respFile1: VirtualFile? = null
        var respFile2: VirtualFile? = null


        if (runReq1 != null && runReq2 != null) {
            executeForEnv(runReq1, processHandler, console, { respFile1 = getFileFromResp(it) }) {
                executeForEnv(runReq2, processHandler, console, { respFile2 = getFileFromResp(it) }) {
                    processHandler.onRunFinished()
                    ApplicationManager.getApplication().invokeLater {
                        if (respFile1 != null && respFile2 != null)
                            launchDiffWindow(respFile1!!, respFile2!!)
                    }
                }
            }
            return DefaultExecutionResult(console.console, processHandler)
        } else {
            return null; // TODO find out how to surface a better error
        }

    }

    private fun createHttpClientExecutionController(
        info: HttpRunRequestInfo,
        processHandler: HttpClientRequestProcessHandler,
        console: HttpResponseConsole,
        httpRequestPostProcessor: HttpRequestPostProcessor,
        onRequestFinished: Runnable,
    ): HttpClientExecutionController {

        val id: String = HttpClientExecutionController.toRequestId(info, console.showRequestMethod())

        return HttpClientExecutionController(
            project,
            info,
            id,
            info.requestPointer,
            processHandler,
            console,
            info.responseHandler,
            { resp ->
                info.postProcessor.onResponseExecuted(resp)
                httpRequestPostProcessor.onResponseExecuted(resp)
            },
            true,
            {
                console.onRequestEnd(id)
                onRequestFinished.run()

            },
            info.ignoreMessage,
            runConfig.toHttpSettings(),
            false
        )
    }

    private fun executeForEnv(
        runReq: HttpRunRequestInfo,
        processHandler: HttpClientRequestProcessHandler,
        console: HttpResponseConsole,
        postProcessor: HttpRequestPostProcessor,
        after: Runnable
    ) {
        createHttpClientExecutionController(
            runReq,
            processHandler,
            console,
            postProcessor,
            after,
        ).execute()

    }

    private fun launchDiffWindow(resp1: VirtualFile, resp2: VirtualFile) {

        DiffManager.getInstance().showDiff(
            project, SimpleDiffRequest(
                "Responses Diffed",
                DiffContentFactory.getInstance().create(project, resp1),
                DiffContentFactory.getInstance().create(project, resp2),
                env1.environmentName,
                env2.environmentName
            )
        )
    }

    private fun getFileFromResp(resp: HttpRequestResponseFileResult?): VirtualFile? {
        return if (resp is HttpRequestResponseFileResult.ExactFile && resp.fileName != null) {
            HttpRequestHistoryManager.findFile(project, resp.fileName!!, true)
        } else {
            null
        }
    }
}