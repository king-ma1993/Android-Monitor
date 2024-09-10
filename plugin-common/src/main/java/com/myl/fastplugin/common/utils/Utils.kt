package com.myl.fastplugin.common.utils

import com.myl.fastplugin.common.R_REGEX
import java.util.regex.Pattern

/**
 * 
 *
 * @description plugin工具类
 * 
 * @date 2022/4/14
 */
object Utils {
    fun replaceSlash2Dot(str: String): String {
        return str.replace('/', '.')
    }

    fun isRFile(className: String): Boolean {
        return Pattern.matches(R_REGEX, className)
    }

    fun replaceDot2Slash(str: String): String {
        return str.replace('.', '/')
    }

}