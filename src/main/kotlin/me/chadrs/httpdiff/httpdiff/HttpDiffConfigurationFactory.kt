package me.chadrs.httpdiff.httpdiff

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.project.Project

class HttpDiffConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {

    override fun getId(): String {
        return HttpDiffRunConfigurationType.ID;
    }
    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return HttpDiffRunConfiguration(project, this, "Diff?")
    }

    override fun getOptionsClass(): Class<out BaseState> {
        return HttpDiffConfigOptions::class.java;
    }
}