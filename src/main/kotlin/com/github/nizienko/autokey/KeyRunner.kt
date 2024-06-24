package com.github.nizienko.autokey

import com.github.nizienko.autokey.settings.AutoKeySettingsState
import com.intellij.openapi.components.Service
import com.intellij.ui.KeyStrokeAdapter
import kotlinx.coroutines.*
import org.assertj.swing.core.BasicRobot
import org.assertj.swing.core.Robot
import java.util.concurrent.atomic.AtomicReference

@Service
internal class KeyRunner(private val scope: CoroutineScope) {
    private val robot = BasicRobot.robotWithCurrentAwtHierarchyWithoutScreenLock()

    private var currentScript: String = ""
    private var currentJob: AtomicReference<Job?> = AtomicReference(null)

    fun isRunning(): Boolean {
        return (currentJob.get()?.isCompleted?.not()
                ?: false || currentJob.get()?.isCancelled?.not() ?: false)
    }

    fun launch(script: String) {
        currentJob.compareAndSet(null, scope.launch {
            currentScript = script
            listeners.forEach { it.onScriptLoaded(script) }
            listeners.forEach { it.onScriptStarted(script) }
            try {
                delay(AutoKeySettingsState.getInstance().timeoutSecondsBeforeRun * 1000L)
            } catch (_: CancellationException) {
                listeners.forEach { it.onScriptFinished(script) }
            }
            execute(currentScript)
            listeners.forEach { it.onScriptFinished(script) }
            currentJob.set(null)
        })
    }

    fun setScript(script: String) {
        currentScript = script
    }

    fun relaunch() {
        stop()
        launch(currentScript)
    }

    fun stop() {
        currentJob.getAndSet(null)?.cancel()
    }

    fun isScriptLoaded(): Boolean = currentScript.isNotEmpty()

    private suspend fun execute(script: String) {
        script.split("\n")
            .filter { line -> line.isNotEmpty() && line.startsWith("#").not() }
            .forEachIndexed { n, line ->
                delay(1)
                listeners.forEach { it.onStepStarted(n, line) }
                val command = Command.entries.firstOrNull { command ->
                    command.isValidFor(line)
                }
                if (command == null) {
                    listeners.forEach { it.onStepFinished(n, line, StepResult.ERROR, "Unknown command") }
                } else {
                    try {
                        command.execute(robot, line)
                        listeners.forEach { it.onStepFinished(n, line, StepResult.FINISHED) }
                    } catch (e: CancellationException) {
                        listeners.forEach { it.onStepFinished(n, line, StepResult.CANCELED) }
                    } catch (e: Throwable) {
                        listeners.forEach { it.onStepFinished(n, line, StepResult.ERROR, e.localizedMessage) }
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

        override suspend fun execute(robot: Robot, line: String) {
            val time = sleepRegex.find(line)?.destructured?.component1()?.toInt() ?: 0
            delay(time * 1000L)
        }
    },
    TYPE {
        override fun isValidFor(line: String): Boolean {
            return typeRegex.matches(line)
        }

        override suspend fun execute(robot: Robot, line: String) {
            val textToType = typeRegex.find(line)?.destructured?.component1() ?: ""
            textToType.forEach {
                robot.type(it)
                delay(10)
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

        override suspend fun execute(robot: Robot, line: String) {
            val keyStroke = KeyStrokeAdapter.getKeyStroke(line)
            robot.pressAndReleaseKey(keyStroke.keyCode, keyStroke.modifiers)
        }
    };

    abstract fun isValidFor(line: String): Boolean
    abstract suspend fun execute(robot: Robot, line: String)
}

interface KeyRunnerListener {
    fun onScriptStarted(script: String)
    fun onScriptFinished(script: String)
    fun onStepStarted(n: Int, step: String)
    fun onStepFinished(n: Int, step: String, result: StepResult, message: String = "")
    fun onScriptLoaded(script: String)
}

enum class StepResult {
    FINISHED, ERROR, CANCELED
}