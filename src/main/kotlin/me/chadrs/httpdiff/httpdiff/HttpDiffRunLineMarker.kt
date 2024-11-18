package me.chadrs.httpdiff.httpdiff

import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.httpClient.http.request.run.HttpRequestLineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement

class HttpDiffRunLineMarker : RunLineMarkerContributor() {
    override fun getInfo(psiElement: PsiElement): Info? {
        if (HttpRequestLineMarkerProvider.isHttpRequestRunElement(psiElement)) {
            return Info(AllIcons.Actions.Diff, arrayOf(HttpDiffAction()))
        }
        return null
    }
}
