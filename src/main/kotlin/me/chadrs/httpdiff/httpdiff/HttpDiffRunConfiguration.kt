package me.chadrs.httpdiff.httpdiff

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.httpClient.http.request.HttpRequestPsiFile
import com.intellij.httpClient.http.request.HttpRequestVariableSubstitutorImpl
import com.intellij.httpClient.http.request.environment.HttpRequestEnvironment
import com.intellij.httpClient.http.request.run.HttpRequestNotifications
import com.intellij.httpClient.http.request.run.HttpRunRequestInfo
import com.intellij.httpClient.http.request.run.config.HttpRequestRunConfiguration
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.refactoring.suggested.createSmartPointer

class HttpDiffRunConfiguration(project: Project, factory: ConfigurationFactory?, name: String?) :
    RunConfigurationBase<HttpDiffConfigOptions>(project, factory, name) {
    override fun getState(executor: Executor, executionEnv: ExecutionEnvironment): RunProfileState? {

        val file = HttpRequestRunConfiguration.findFileByPath(project, options.getHttpFilePath())
        if (file != null) {
            val env1 = HttpRequestEnvironment.create(project, options.getFirstEnv(), file)
            val env2 = HttpRequestEnvironment.create(project, options.getSecondEnv(), file)
            return RunHtpDiffRunProfileState(env1, env2, project, this)
        } else {
            HttpRequestNotifications.showErrorBalloon(
                project,
                "Failed to run requests",
                "Cannot find requests in ${options.getHttpFilePath()}"
            )
            return null;
        }
    }

    override fun getOptions(): HttpDiffConfigOptions = super.getOptions() as HttpDiffConfigOptions

    fun getFirstEnv(): String? = options.getFirstEnv()

    fun setFirstEnv(env: String?) = options.setFirstEnv(env)

    fun getSecondEnv(): String? = options.getSecondEnv()

    fun setSecondEnv(env: String?) = options.setSecondEnv(env)

    fun getHttpFilePath(): String? = options.getHttpFilePath()

    fun setHttpFilePath(filePath: String) = options.setHttpFilePath(filePath)

    fun getRequestIndex() = options.getSelectedRequestIndex()

    fun setRequestIndex(selectedRequestIndex: Int) = options.setSelectedRequestIndex(selectedRequestIndex)

    fun getRequestIdentifier(): String? = options.getSelectedRequestIdentifier()
    fun setRequestIdentifier(selectedRequestIdentifier: String?) =
        options.setSelectedRequestIdentifier(selectedRequestIdentifier)


    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return HttpDiffSettingsEditor(project)
    }

    fun toHttpSettings(): HttpRequestRunConfiguration.Settings {
        val settings = HttpRequestRunConfiguration.Settings()
        settings.filePath = this.getHttpFilePath()
        settings.environment = this.getFirstEnv()
        settings.index = getRequestIndex()
        return settings
    }

    fun getRequestInfo(env: HttpRequestEnvironment): HttpRunRequestInfo? {
        val file = HttpRequestRunConfiguration.findFileByPath(project, options.getHttpFilePath())
        return if (file != null && file is HttpRequestPsiFile) {
            val req = HttpRequestRunConfiguration.findRequestInFile(file, toHttpSettings())
            HttpRunRequestInfo.create(
                req, req.createSmartPointer(), HttpRequestVariableSubstitutorImpl.create(project, env, file)
            )
        } else null;
    }
}