package com.github.nizienko.autokey

import com.intellij.openapi.components.Service
import com.intellij.ui.KeyStrokeAdapter
import org.assertj.swing.core.BasicRobot
import org.assertj.swing.core.Robot
import kotlin.concurrent.thread

@Service
internal class KeyRunner {
    private val robot = BasicRobot.robotWithCurrentAwtHierarchyWithoutScreenLock()

    private var currentScript: String = ""
    private var timeBeforeStart = 5000L

    fun launch(script: String) {
        thread {
            currentScript = script
            listeners.forEach { it.onScriptLoaded(script) }
            listeners.forEach { it.onScriptStarted(script) }
            Thread.sleep(timeBeforeStart)
            execute(currentScript)
            listeners.forEach { it.onScriptFinished(script) }
        }
    }
    fun setScript(script: String) {
        currentScript = script
    }
    fun relaunch() {
        launch(currentScript)
    }

    fun isScriptLoaded(): Boolean = currentScript.isNotEmpty()

    private fun execute(script: String) {
        script.split("\n")
            .filter { line -> line.isNotEmpty() && line.startsWith("#").not() }
            .forEachIndexed { n, line ->
                listeners.forEach { it.onStepStarted(n, line) }
                val command = Command.values().firstOrNull { command ->
                    command.isValidFor(line)
                }
                if (command == null) {
                    listeners.forEach { it.onStepFinished(n, line, false, "Unknown command") }
                } else {
                    try {
                        command.execute(robot, line)
                        listeners.forEach { it.onStepFinished(n, line, true) }
                    } catch (e: Throwable) {
                        listeners.forEach { it.onStepFinished(n, line, false, e.localizedMessage) }
                    }
                }
            }
    }

    private val listeners: MutableList<KeyRunnerListener> = mutableListOf()
    fun addListener(listener: KeyRunnerListener) {
        listeners.add(listener)
    }
}

private val sleepRegex = Regex("^sleep (\\d{1,5})")
private val typeRegex = Regex("^type (.*)")

internal enum class Command {
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
    },
    HOT_KEY {
        override fun isValidFor(line: String): Boolean {
            return try {
                KeyStrokeAdapter.getKeyStroke(line) != null
            } catch (e: Throwable) {
                false
            }
        }

        override fun execute(robot: Robot, line: String) {
            val keyStroke = KeyStrokeAdapter.getKeyStroke(line)
            robot.pressAndReleaseKey(keyStroke.keyCode, keyStroke.modifiers)
        }
    };

    abstract fun isValidFor(line: String): Boolean
    abstract fun execute(robot: Robot, line: String)
}

interface KeyRunnerListener {
    fun onScriptStarted(script: String)
    fun onScriptFinished(script: String)
    fun onStepStarted(n: Int, step: String)
    fun onStepFinished(n: Int, step: String, success: Boolean, error: String = "")
    fun onScriptLoaded(script: String)
}