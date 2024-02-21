package me.chadrs.httpdiff.httpdiff

import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.icons.AllIcons
import com.intellij.openapi.util.NotNullLazyValue


class HttpDiffRunConfigurationType : ConfigurationTypeBase(
    ID,
    "HTTP Client Diff",
    "Diff two http responses",
    NotNullLazyValue.createValue { AllIcons.Actions.Diff }
) {
    init {
        addFactory(HttpDiffConfigurationFactory(this));
    }

    companion object {
        const val ID = "HttpDiffRun"
    }
}