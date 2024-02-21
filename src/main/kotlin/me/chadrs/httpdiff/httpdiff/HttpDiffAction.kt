package me.chadrs.httpdiff.httpdiff

import com.intellij.httpClient.http.request.HttpRequestPsiFile
import com.intellij.httpClient.http.request.run.HttpRequestExecutorExtension
import com.intellij.httpClient.http.request.run.RunHttpRequestAction.RunRequestWithEnvAction
import com.intellij.httpClient.http.request.run.config.HttpRequestFileExecutionConfig
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.*

// I don't think this will work :(
class HttpDiffAction : AnAction("HttpDiff") {
    override fun actionPerformed(event: AnActionEvent) {
        val ele = event.getData(CommonDataKeys.PSI_ELEMENT)
        val file = event.getData(CommonDataKeys.PSI_FILE) as PsiFile
        event.project?.let { project ->
            // todo: run the right kind of runconfig here
        }
    }
}