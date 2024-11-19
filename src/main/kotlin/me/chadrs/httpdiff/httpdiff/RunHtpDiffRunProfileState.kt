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
import com.intellij.httpClient.http.request.run.HttpClientRequestProcessHandler
import com.intellij.httpClient.http.request.run.console.HttpResponseConsole
import com.intellij.httpClient.http.request.run.test.HttpMultiResponseConsole
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
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
            project, smtRunnerConsoleProperties, processHandler, null
        )

        val job = service<HttpDiffApplicationService>().runHttpRequests(
            env1,
            env2,
            runConfig, project, processHandler, console
        ) { respFile1: VirtualFile?, respFile2: VirtualFile? ->
            processHandler.onRunFinished()
            if (respFile1 != null && respFile2 != null) {
                ApplicationManager.getApplication().invokeLater {
                    launchDiffWindow(respFile1, respFile2)
                }
            }
        }
        return job?.let { DefaultExecutionResult(console.console, processHandler) }

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
}