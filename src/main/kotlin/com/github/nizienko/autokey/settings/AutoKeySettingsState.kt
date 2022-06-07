package com.github.nizienko.autokey.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "com.github.nizienko.autokey.settings.AutoKeySettingsState",
    storages = [Storage("AutoKeySettingsPlugin.xml")]
)
internal class AutoKeySettingsState: PersistentStateComponent<AutoKeySettingsState> {
    companion object {
        fun getInstance(): AutoKeySettingsState {
            return ApplicationManager.getApplication().getService(AutoKeySettingsState::class.java)
        }
    }

    var timeoutSecondsBeforeRun: Int = 5

    override fun getState(): AutoKeySettingsState {
        return this
    }

    override fun loadState(state: AutoKeySettingsState) {
        XmlSerializerUtil.copyBean(state, this);
    }
}