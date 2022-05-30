package com.github.nizienko.autokey

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory

internal class AutoKeyToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val autoKeyToolWindow = AutoKeyToolWindow(project, toolWindow)
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content: Content = contentFactory.createContent(autoKeyToolWindow.getContent(), "", false)
        toolWindow.contentManager.addContent(content)
    }
}