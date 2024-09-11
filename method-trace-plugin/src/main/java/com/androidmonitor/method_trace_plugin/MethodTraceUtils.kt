package com.androidmonitor.method_trace_plugin

import com.myl.fastplugin.common.utils.Utils.isRFile

object MethodTraceUtils {

    private val UN_TRACE_CLASS = arrayOf("Manifest", "BuildConfig")

    /**
     * 固定的类不需要进行method-trace的类
     */
    fun noNeedTraceClass(className: String): Boolean {
        return isRFile(className) || isInUnTraceList(className)

    }

    private fun isInUnTraceList(className: String): Boolean {
        for (unTraceCls in UN_TRACE_CLASS) {
            if (className.contains(unTraceCls)) {
                return true
            }
        }
        return false
    }
}