package com.myl.fastplugin.common.utils

import java.io.File
import java.io.InputStream

/**
 * 
 *
 * @description IO相关拓展函数
 * 
 * @date 2022/3/31 4:49 下午
 */

/**
 * 如果文件不存在则创建后返回文件
 */
fun File.touch(): File {
    if (!this.exists()) {
        this.parentFile?.mkdirs()
        this.createNewFile()
    }
    return this
}


fun File.file(vararg path: String) = File(this, path.joinToString(File.separator))

/**
 * 将此字节数据重定向到指定文件
 *
 * 
 */
fun ByteArray.redirect(file: File): Long = this.inputStream().use { it.redirect(file) }

/**
 * 将此输入流重定向到指定文件
 *
 * 
 */
fun InputStream.redirect(file: File): Long = file.touch().outputStream().use { this.copyTo(it) }