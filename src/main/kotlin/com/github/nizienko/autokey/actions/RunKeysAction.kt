package com.github.nizienko.autokey.actions

import com.github.nizienko.autokey.KeyRunner
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.wm.ToolWindowManager

internal class RunKeysAction : AnAction(), DumbAware {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val text = getText(editor)
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("AutoKey runner");
        toolWindow?.activate(null);
        service<KeyRunner>().launch(text)
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabledAndVisible = (project != null
                && editor != null && editor.selectionModel.hasSelection()) && service<KeyRunner>().isRunning().not()
    }

    private fun getText(editor: Editor): String {
        val document = editor.document
        val caret = editor.caretModel.primaryCaret
        val start = caret.selectionStart
        val end = caret.selectionEnd
        return document.getText(TextRange(start, end))
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}