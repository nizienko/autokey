package com.github.nizienko.autokey.ui

import com.github.nizienko.autokey.KeyRunner
import com.github.nizienko.autokey.KeyRunnerListener
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.*
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Splitter
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.util.ui.components.BorderLayoutPanel
import javax.swing.JComponent

internal class AutoKeyToolWindow(private val project: Project, private val toolWindow: ToolWindow) : KeyRunnerListener {
    private val keyRunner: KeyRunner = service()

    init {
        keyRunner.addListener(this)
    }

    private val actionToolbar = ActionToolbarImpl(
        "AutoKeyToolWindow",
        ActionManager.getInstance().getAction("RunKeysActionGroup") as ActionGroup,
        true
    )

    private val consoleDocument: Document = EditorFactory.getInstance().createDocument("")
    private val consoleEditor: Editor =
        EditorFactory.getInstance().createEditor(consoleDocument, project, PlainTextFileType.INSTANCE, true)
    private val disposableConsoleEditorPanel =
        DisposableEditorPanel(consoleEditor).apply { Disposer.register(toolWindow.disposable, this) }


    private val scriptDocument: Document = EditorFactory.getInstance().createDocument("").apply {
        this.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                keyRunner.setScript(this@apply.text)
            }
        })
    }
    private val scriptEditor: Editor =
        EditorFactory.getInstance().createEditor(scriptDocument, project, PlainTextFileType.INSTANCE, false)
    private val disposableScriptEditorPanel =
        DisposableEditorPanel(scriptEditor).apply { Disposer.register(toolWindow.disposable, this) }


    private val panel = BorderLayoutPanel().apply {
        addToTop(actionToolbar)

        addToCenter(Splitter().apply {
            firstComponent = disposableScriptEditorPanel
            secondComponent = disposableConsoleEditorPanel
        })
    }

    fun getContent(): JComponent {
        return panel
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

    override fun onScriptLoaded(script: String) = invokeLater {
        runWriteAction {
            val runnable = {
                scriptDocument.setText(script)
            }
            CommandProcessor.getInstance().executeCommand(project, runnable, null, null)
        }
    }

    private fun printToConsole(text: String) = invokeLater {
        runWriteAction {
            val runnable = { consoleDocument.insertString(consoleDocument.textLength, text) }
            CommandProcessor.getInstance().executeCommand(project, runnable, null, null)
            val caret = consoleEditor.caretModel
            caret.moveToOffset(consoleDocument.textLength)
            consoleEditor.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
        }
    }

    private fun clearConsole() = invokeLater {
        runWriteAction {
            val runnable = { consoleDocument.setText("") }
            CommandProcessor.getInstance().executeCommand(project, runnable, null, null)
        }
    }
}

internal class AutoKeyToolbar