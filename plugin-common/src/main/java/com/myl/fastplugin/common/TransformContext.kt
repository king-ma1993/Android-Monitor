package com.myl.fastplugin.common

import com.android.build.api.transform.TransformInvocation
import com.myl.fastplugin.common.log.ICachedLogger
import com.myl.fastplugin.common.log.ILogger
import com.myl.fastplugin.common.log.LevelLog
import com.myl.fastplugin.common.log.LogDistributor
import com.myl.fastplugin.common.log.impl.FileLoggerImpl
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import org.gradle.api.Project

/**
 * 
 *
 * @description plugin的上下文
 * 
 * @date 2022/4/15
 */
class TransformContext(
    val transformInvocation: TransformInvocation,
    val project: Project
) {
    private var logger: ILogger

    fun reportsDir(): File {
        return File(project.buildDir, "reports").also { it.mkdirs() }
    }


    init {
        logger = createLogger()
        getLogger().i("init")
    }

    fun getLogger(): ILogger {
        return logger
    }


    protected fun createLogger(): ILogger {
        val logFile: File = getLoggerFile()
        logFile.delete()
        val fileLogger: ILogger
        fileLogger = try {
            FileLoggerImpl.of(logFile.absolutePath)
        } catch (e: IOException) {
            throw RuntimeException("can not create log file", e)
        }
        val logDistributor = LogDistributor()
        logDistributor.addLogger(fileLogger)
        val levelLog = LevelLog(logDistributor)
        return levelLog
    }

    protected fun getLoggerFile(): File {
        return File(File(reportsDir(), "const-in-line"), transformInvocation.context.variantName)
    }
}