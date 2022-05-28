package com.github.nizienko.autokey

import org.assertj.swing.core.BasicRobot
import kotlin.concurrent.thread

class KeyRunner {
    private val robot = BasicRobot.robotWithCurrentAwtHierarchyWithoutScreenLock()

    fun launch(script: String) {
        thread {
            Thread.sleep(5000)
            type(script)
        }
    }

    private fun type(string: String, delayBetweenShortcutAndTypingMs: Int = 0) {
        string.forEach {
            robot.type(it)
        }
    }
}