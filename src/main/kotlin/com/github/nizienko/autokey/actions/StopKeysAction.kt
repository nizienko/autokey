package com.github.nizienko.autokey.actions

import com.github.nizienko.autokey.KeyRunner
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware

internal class StopKeysAction : AnAction(), DumbAware {

    override fun actionPerformed(e: AnActionEvent) {
        service<KeyRunner>().stop()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = service<KeyRunner>().isRunning()
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}