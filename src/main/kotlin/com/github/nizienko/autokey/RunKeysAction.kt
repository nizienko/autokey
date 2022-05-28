package com.github.nizienko.autokey

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange

class RunKeysAction: AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val text = getText(project, editor)
        service<KeyRunner>().launch(text)
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabledAndVisible = (project != null
                && editor != null && editor.selectionModel.hasSelection())
    }

    private fun getText(project: Project, editor: Editor): String {
        val document = editor.document
        val caret = editor.caretModel.primaryCaret
        val start = caret.selectionStart
        val end = caret.selectionEnd
        return document.getText(TextRange(start, end))
    }
}