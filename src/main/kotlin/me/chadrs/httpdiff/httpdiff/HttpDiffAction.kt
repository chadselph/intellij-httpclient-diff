package me.chadrs.httpdiff.httpdiff

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiFile

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