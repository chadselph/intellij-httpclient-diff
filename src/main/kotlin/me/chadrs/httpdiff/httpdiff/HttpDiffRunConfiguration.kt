package me.chadrs.httpdiff.httpdiff

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.httpClient.http.request.HttpRequestPsiFile
import com.intellij.httpClient.http.request.HttpRequestVariableSubstitutionKey
import com.intellij.httpClient.http.request.HttpRequestVariableSubstitutor
import com.intellij.httpClient.http.request.environment.HttpRequestEnvironment
import com.intellij.httpClient.http.request.run.HttpRequestNotifications
import com.intellij.httpClient.http.request.run.config.HttpRequestRunConfiguration
import com.intellij.httpClient.http.request.run.info.HttpRunRequestInfo
import com.intellij.httpClient.http.request.run.info.createHttpRunRequestInfo
import com.intellij.httpClient.http.request.substitution.HttpEnvironmentContextProvider
import com.intellij.httpClient.http.request.substitution.HttpRequestVariableRootSubstitutor
import com.intellij.httpClient.http.request.substitutor.HttpRequestVariableSessionSubstitutor
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Condition
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer

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
            return null
        }
    }

    override fun getOptions(): HttpDiffConfigOptions = super.getOptions() as HttpDiffConfigOptions

    fun getFirstEnv(): String? = options.getFirstEnv()

    fun setFirstEnv(env: String?) = options.setFirstEnv(env)

    fun getSecondEnv(): String? = options.getSecondEnv()

    fun setSecondEnv(env: String?) = options.setSecondEnv(env)

    fun getHttpFilePath(): String? = options.getHttpFilePath()

    fun setHttpFilePath(filePath: String) = options.setHttpFilePath(filePath)

    fun getRequestIndex() = options.getRequestIndex()

    fun setRequestIndex(selectedRequestIndex: Int) = options.setRequestIndex(selectedRequestIndex)

    fun getRequestIdentifier(): String? = options.getRequestIdentifier()
    fun setRequestIdentifier(selectedRequestIdentifier: String?) =
        options.setRequestIdentifier(selectedRequestIdentifier)


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
            val substitutor = HttpRequestVariableRootSubstitutor(
                HttpEnvironmentContextProvider(listOf(env), true, env, true)
            )
            createHttpRunRequestInfo(
                req, req.createSmartPointer(), object : HttpRequestVariableSessionSubstitutor {

                    override fun getSessionProvider(): (String) -> Any? {
                        // No idea what this is supposed to do!
                        return { _: String -> null }
                    }

                    override fun getValue(p0: PsiElement): String {
                        return substitutor.getValue(p0)
                    }

                    override fun getValue(p0: PsiElement, p1: Condition<in PsiElement>): String {
                        return substitutor.getValue(p0, p1)
                    }

                    override fun getVariableValue(
                        p0: String?,
                        p1: String?,
                        p2: Project,
                        p3: HttpRequestVariableSubstitutionKey?
                    ): String? {
                        return substitutor.getVariableValue(p0, p1, p2, p3)
                    }

                    override fun invalidVariablesAware(p0: Boolean): HttpRequestVariableSubstitutor {
                        return substitutor.invalidVariablesAware(p0)
                    }

                    override fun setSessionVariables(newVars: Map<String, Any?>) {}
                }
            )
        } else null
    }
}