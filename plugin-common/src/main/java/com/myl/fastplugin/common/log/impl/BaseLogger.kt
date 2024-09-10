package com.myl.fastplugin.common.log.impl

import com.myl.fastplugin.common.PLUGIN_NAME
import com.myl.fastplugin.common.log.ILogger
import com.myl.fastplugin.common.log.impl.BaseLogger
import java.io.Closeable
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import org.gradle.api.logging.LogLevel

/**
 * 
 *
 * @description 基础logger类
 * 
 * @date 2022/5/9
 */
abstract class BaseLogger : ILogger, Closeable {
    private var tag = PLUGIN_NAME
    override fun setTag(tag: String) {
        this.tag = if (tag == null || "" == tag) PLUGIN_NAME else tag
    }

    override fun d(msg: String) {
        d(tag, msg)
    }

    override fun d(tag: String, msg: String) {
        write(LogLevel.DEBUG, tag, msg, null)
    }

    override fun i(msg: String) {
        i(tag, msg)
    }

    override fun i(tag: String, msg: String) {
        write(LogLevel.INFO, tag, msg, null)
    }

    override fun w(msg: String) {
        w(tag, msg)
    }

    override fun w(tag: String, msg: String) {
        w(tag, msg, null)
    }

    override fun w(msg: String, t: Throwable?) {
        w(tag, msg, t)
    }

    override fun w(tag: String, msg: String, t: Throwable?) {
        write(LogLevel.WARN, tag, msg, t)
    }

    override fun e(msg: String) {
        e(tag, msg)
    }

    override fun e(tag: String, msg: String) {
        e(tag, msg, null)
    }

    override fun e(msg: String, t: Throwable?) {
        e(tag, msg, t)
    }

    override fun e(tag: String, msg: String, t: Throwable?) {
        write(LogLevel.ERROR, tag, msg, t)
    }

    protected abstract fun write(level: LogLevel?, prefix: String?, msg: String?, t: Throwable?)

    override fun close() {
    }

    companion object {
        fun stackToString(t: Throwable): String {
            val sw = StringWriter(128)
            val ps = PrintWriter(sw)
            t.printStackTrace(ps)
            ps.flush()
            return sw.toString()
        }
    }
}