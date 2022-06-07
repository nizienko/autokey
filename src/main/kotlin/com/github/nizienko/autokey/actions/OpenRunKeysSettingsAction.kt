package com.github.nizienko.autokey.actions

import com.github.nizienko.autokey.settings.AutoKeyConfigurable
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAware

internal class OpenRunKeysSettingsAction : AnAction(), DumbAware {
    override fun actionPerformed(e: AnActionEvent) {
        ShowSettingsUtil.getInstance().showSettingsDialog(e.project, AutoKeyConfigurable::class.java)
    }
}