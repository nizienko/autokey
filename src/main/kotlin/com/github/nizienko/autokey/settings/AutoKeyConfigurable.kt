package com.github.nizienko.autokey.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent


internal class AutoKeyConfigurable : Configurable {
    private var component: AutoKeySettingsComponent? = null

    override fun createComponent(): JComponent? {
        component = AutoKeySettingsComponent()
        return component?.panel
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return component?.preferredFocusedComponent
    }

    override fun isModified(): Boolean {
        val settings = AutoKeySettingsState.getInstance()
        return settings.timeoutSecondsBeforeRun != component?.timeoutSecondsBeforeRun
    }

    override fun apply() {
        val settings = AutoKeySettingsState.getInstance()
        settings.timeoutSecondsBeforeRun = component?.timeoutSecondsBeforeRun?:  throw IllegalStateException("Component is not created")
    }

    override fun reset() {
        val settings = AutoKeySettingsState.getInstance()
        component?.timeoutSecondsBeforeRun = settings.timeoutSecondsBeforeRun
    }

    override fun disposeUIResources() {
        component = null
    }

    override fun getDisplayName(): String = "Auto Key"
}