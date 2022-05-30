package com.github.nizienko.autokey

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.*
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import javax.swing.JComponent

internal class AutoKeyToolWindow(private val project: Project, private val toolWindow: ToolWindow) : KeyRunnerListener {
    init {
        service<KeyRunner>().addListener(this)
    }

    private val document: Document = EditorFactory.getInstance().createDocument("")
    private val editor: Editor = EditorFactory.getInstance().createEditor(document, project, PlainTextFileType.INSTANCE, true)
    private val disposableEditorPanel =
        DisposableEditorPanel(editor).apply { Disposer.register(toolWindow.disposable, this) }

    fun getContent(): JComponent {
        return disposableEditorPanel
    }

    override fun onScriptStarted(script: String) {
        clearConsole()
        printToConsole("Here we go in 5 sec...\n")
    }

    override fun onScriptFinished(script: String) {
        printToConsole("Done!\n")
        NotificationGroupManager.getInstance()
            .getNotificationGroup("AutoKey Notification Group")
            .createNotification("AutoKey script finished", NotificationType.INFORMATION)
            .notify(project)
    }

    override fun onStepStarted(n: Int, step: String) {
        printToConsole(step)
    }

    override fun onStepFinished(n: Int, step: String, success: Boolean, error: String) {
        if (success) {
            printToConsole(" - OK\n")
        } else {
            printToConsole(" - ERROR $error\n")
        }
    }

    private fun printToConsole(text: String) = invokeLater {
        runWriteAction {
            val runnable = { document.insertString(document.textLength, text) }
            CommandProcessor.getInstance().executeCommand(project, runnable, null, null)
            val caret = editor.caretModel
            caret.moveToOffset(document.textLength)
            editor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
        }
    }

    private fun clearConsole() = invokeLater {
        runWriteAction {
            val runnable = { document.setText("") }
            CommandProcessor.getInstance().executeCommand(project, runnable, null, null)
        }
    }
}