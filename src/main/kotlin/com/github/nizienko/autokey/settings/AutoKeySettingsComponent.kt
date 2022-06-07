package com.github.nizienko.autokey.settings

import com.intellij.ide.ui.UINumericRange
import com.intellij.ui.JBIntSpinner
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.components.BorderLayoutPanel
import javax.swing.JComponent


internal class AutoKeySettingsComponent {
    private val timeoutSpinner = JBIntSpinner(UINumericRange(5, 0, 30))

    var timeoutSecondsBeforeRun: Int
        get() = timeoutSpinner.number
        set(value) {
            timeoutSpinner.number = value
        }
    val panel = BorderLayoutPanel().apply {
        addToTop(
            FormBuilder.createFormBuilder()
                .addLabeledComponent("Wait seconds before run", timeoutSpinner)
                .panel
        )
    }

    val preferredFocusedComponent: JComponent = timeoutSpinner
}