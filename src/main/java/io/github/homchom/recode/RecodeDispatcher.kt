package io.github.homchom.recode

import io.github.homchom.recode.RecodeDispatcher.expedite
import io.github.homchom.recode.RecodeDispatcher.invoke
import io.github.homchom.recode.util.callWhile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import net.minecraft.util.thread.ReentrantBlockableEventLoop
import java.util.concurrent.Executor

/**
 * Runs [block] on Minecraft's main thread. If this is called from another thread, the calling thread is blocked
 * until completion.
 */
inline fun <R> runOnMinecraftThread(crossinline block: () -> R) =
    if (mc.isSameThread) block() else runBlocking(RecodeDispatcher()) { block() }

/**
 * An object providing access (via [invoke]) to an immediate [CoroutineDispatcher] confining execution to
 * Minecraft's main thread. Use this instead of Minecraft's default [Executor] because call sites will often
 * [expedite] dispatch, allowing for inline execution without breaking Minecraft's vanilla behavior.
 */
object RecodeDispatcher {
    private val taskLoop = object : ReentrantBlockableEventLoop<Runnable>(MOD_NAME) {
        override fun wrapRunnable(runnable: Runnable) = runnable
        override fun getRunningThread() = Thread.currentThread().takeIf { mc.isSameThread }
        override fun shouldRun(runnable: Runnable) = true

        override fun isSameThread() = mc.isSameThread
    }

    private val coroutineDispatcher = taskLoop.asCoroutineDispatcher()

    operator fun invoke() = coroutineDispatcher

    /**
     * Tells the dispatcher to expedite previously dispatched blocks. If this is called from Minecraft's main
     * thread, it is guaranteed that all blocks will complete before the function returns.
     */
    fun expedite() {
        if (mc.isSameThread) callWhile { taskLoop.pollTask() }
    }
}