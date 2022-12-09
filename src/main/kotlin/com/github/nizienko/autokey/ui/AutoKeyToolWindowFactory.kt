package com.github.nizienko.autokey.ui

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory

internal class AutoKeyToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val autoKeyToolWindow = AutoKeyToolWindow(project, toolWindow)
        val contentFactory = ContentFactory.getInstance()
        val content: Content = contentFactory.createContent(autoKeyToolWindow.getContent(), "", false)
        toolWindow.contentManager.addContent(content)
    }

}