package com.myl.fastplugin.common.log

import org.gradle.api.logging.LogLevel

/**
 * 
 *
 * @description ICachedLogger接口
 * 
 * @date 2022/5/9
 */
interface ICachedLogger : ILogger {
    fun accept(logger: CachedLogVisitor)
    fun clear()
    interface CachedLogVisitor {
        fun visitLog(logTime: Long, level: LogLevel, prefix: String, msg: String, t: Throwable?)
    }
}