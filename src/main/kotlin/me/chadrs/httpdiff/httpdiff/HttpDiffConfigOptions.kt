package me.chadrs.httpdiff.httpdiff

import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.openapi.components.StoredProperty

class HttpDiffConfigOptions : RunConfigurationOptions() {

    private val firstEnv: StoredProperty<String?> = string("").provideDelegate(this, "firstEnv")
    private val secondEnv: StoredProperty<String?> = string("").provideDelegate(this, "secondEnv")
    private val httpFilePath: StoredProperty<String?> = string("").provideDelegate(this, "httpFilePath")
    private val selectedRequestIndex: StoredProperty<Int> = property(1).provideDelegate(this, "requestIndex")
    private val selectedRequestIdentifier: StoredProperty<String?> =
        string("").provideDelegate(this, "requestIdentifier")

    fun getFirstEnv(): String? = firstEnv.getValue(this)
    fun setFirstEnv(toValue: String?) = firstEnv.setValue(this, toValue)

    fun getSecondEnv(): String? = secondEnv.getValue(this)
    fun setSecondEnv(toValue: String?) = secondEnv.setValue(this, toValue)

    fun getHttpFilePath() = httpFilePath.getValue(this)
    fun setHttpFilePath(toValue: String?) = httpFilePath.setValue(this, toValue)

    fun getSelectedRequestIndex() = selectedRequestIndex.getValue(this)
    fun setSelectedRequestIndex(index: Int) = selectedRequestIndex.setValue(this, index)

    fun getSelectedRequestIdentifier(): String? = selectedRequestIdentifier.getValue(this)
    fun setSelectedRequestIdentifier(requestIdentifier: String?) =
        selectedRequestIdentifier.setValue(this, requestIdentifier)

}