package com.github.nizienko.autokey.actions

import com.github.nizienko.autokey.KeyRunner
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware

internal class RerunKeysAction : AnAction(), DumbAware {
    override fun actionPerformed(e: AnActionEvent) {
        service<KeyRunner>().relaunch()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = service<KeyRunner>().let { it.isScriptLoaded() && it.isRunning().not() }
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}