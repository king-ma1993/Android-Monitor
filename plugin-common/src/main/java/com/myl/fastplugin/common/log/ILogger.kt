package com.myl.fastplugin.common.log

import com.myl.fastplugin.common.log.impl.SystemLoggerImpl

/**
 * 
 *
 * @description ILogger接口
 * 
 * @date 2022/5/9
 */
interface ILogger {
    fun setTag(tag: String)
    fun d(msg: String)
    fun d(tag: String, msg: String)
    fun i(msg: String)
    fun i(tag: String, msg: String)
    fun w(msg: String)
    fun w(tag: String, msg: String)
    fun w(msg: String, t: Throwable?)
    fun w(tag: String, msg: String, t: Throwable?)
    fun e(msg: String)
    fun e(tag: String, msg: String)
    fun e(msg: String, t: Throwable?)
    fun e(tag: String, msg: String, t: Throwable?)

    companion object {
        val DEFAULT: ILogger = SystemLoggerImpl()
    }
}