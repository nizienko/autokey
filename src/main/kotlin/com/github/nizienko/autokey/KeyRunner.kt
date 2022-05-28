package com.github.nizienko.autokey

import com.intellij.openapi.components.Service
import com.intellij.ui.KeyStrokeAdapter
import org.assertj.swing.core.BasicRobot
import org.assertj.swing.core.Robot
import kotlin.concurrent.thread

@Service
class KeyRunner {
    private val robot = BasicRobot.robotWithCurrentAwtHierarchyWithoutScreenLock()

    fun launch(script: String) {
        thread {
            execute(script)
        }
    }

    private fun execute(string: String) {
        string.split("\n").forEach { line ->
            val command = Command.values().firstOrNull { command ->
                command.isValidFor(line)
            }
            command?.execute(robot, line) ?: println("Unknown command '$line'")
        }
    }
}

private val sleepRegex = Regex("^sleep (\\d{1,5})")
private val typeRegex = Regex("^type (.*)")

internal enum class Command {
    HOT_KEY {
        override fun isValidFor(line: String): Boolean {
            return KeyStrokeAdapter.getKeyStroke(line) != null
        }

        override fun execute(robot: Robot, line: String) {
            val keyStroke = KeyStrokeAdapter.getKeyStroke(line)
            robot.pressAndReleaseKey(keyStroke.keyCode, keyStroke.modifiers)
        }
    },
    SLEEP {
        override fun isValidFor(line: String): Boolean {
            return sleepRegex.matches(line)
        }

        override fun execute(robot: Robot, line: String) {
            val time = sleepRegex.find(line)?.destructured?.component1()?.toInt() ?: 0
            Thread.sleep(time * 1000L)
        }
    },
    TYPE {
        override fun isValidFor(line: String): Boolean {
            return typeRegex.matches(line)
        }

        override fun execute(robot: Robot, line: String) {
            val textToType = typeRegex.find(line)?.destructured?.component1() ?: ""
            textToType.forEach {
                robot.type(it)
            }
        }
    };

    abstract fun isValidFor(line: String): Boolean
    abstract fun execute(robot: Robot, line: String)
}