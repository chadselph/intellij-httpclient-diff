package me.chadrs.httpdiff.httpdiff

import com.intellij.httpClient.http.request.HttpRequestPsiFile
import com.intellij.httpClient.http.request.run.config.HttpEnvironmentComboBox
import com.intellij.httpClient.http.request.run.config.HttpRequestComboBox
import com.intellij.httpClient.http.request.run.config.HttpRequestRunConfiguration
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.DocumentAdapter
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.event.DocumentEvent


/**
 * Closely based on [com.intellij.httpClient.http.request.run.config.HttpRequestRunConfigurationEditor]
 */
class HttpDiffSettingsEditor(private val project: Project) : SettingsEditor<HttpDiffRunConfiguration>() {

    private val httpFileField = TextFieldWithBrowseButton()
    private val env1Field = HttpEnvironmentComboBox(this)
    private val env2Field = HttpEnvironmentComboBox(this)
    private val reqField = HttpRequestComboBox()

    override fun resetEditorFrom(config: HttpDiffRunConfiguration) {
        // don't trigger updates while we programmatically set things
        httpFileField.textField.document.removeDocumentListener(textChangedListener)

        val file = HttpRequestRunConfiguration.findFileByPath(project, config.getHttpFilePath())
        httpFileField.textField.text = config.getHttpFilePath()
        env1Field.reset(project, file, config.getFirstEnv())
        env2Field.reset(project, file, config.getSecondEnv())
        if (file is HttpRequestPsiFile) {
            reqField.reset(file, config.getRequestIndex(), config.getRequestIdentifier())
            reqField.isEnabled = true
        } else {
            reqField.isEnabled = false
        }
        httpFileField.textField.document.addDocumentListener(textChangedListener)
    }

    override fun applyEditorTo(config: HttpDiffRunConfiguration) {
        config.setFirstEnv(env1Field.selectedItem?.name)
        config.setSecondEnv(env2Field.selectedItem?.name)
        config.setHttpFilePath(httpFileField.textField.text)
        config.setRequestIndex(reqField.selectedRequestIndex)
        config.setRequestIdentifier(reqField.selectedRequestIdentifier)
    }

    override fun createEditor(): JComponent {
        httpFileField.addBrowseFolderListener(
            project,
            FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
        )
        httpFileField.textField.document.addDocumentListener(textChangedListener)
        return FormBuilder.createFormBuilder()
            .addLabeledComponent("File", httpFileField)
            .addLabeledComponent("First environment", env1Field)
            .addLabeledComponent("Second environment", env2Field)
            .addLabeledComponent("Request", reqField).panel
    }

    private val textChangedListener = object : DocumentAdapter() {
        override fun textChanged(e: DocumentEvent) {
            val file = HttpRequestRunConfiguration.findFileByPath(project, httpFileField.textField.text)
            env1Field.reset(project, file, null)
            env2Field.reset(project, file, null)
            if (file is HttpRequestPsiFile) {
                reqField.reset(file, reqField.selectedIndex, reqField.selectedRequestIdentifier)
                reqField.isEnabled = true
            } else {
                reqField.isEnabled = false
            }
        }
    }

}
