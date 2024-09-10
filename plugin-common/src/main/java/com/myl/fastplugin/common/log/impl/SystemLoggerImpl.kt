package com.myl.fastplugin.common.log.impl

import com.myl.fastplugin.common.PLUGIN_NAME
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class SystemLoggerImpl : BaseLogger() {
    private val logger: Logger = Logging.getLogger(PLUGIN_NAME)
    @Synchronized
    override fun write(level: LogLevel?, prefix: String?, msg: String?, t: Throwable?) {
        if (t != null) {
            logger.log(level, String.format("[%-10s] %s", prefix, msg), t)
        } else {
            logger.log(level, String.format("[%-10s] %s", prefix, msg))
        }
    }
}