package com.myl.fastplugin.common.command

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler

/**
 * 
 *
 * @description 没有安装的Command
 * 
 * @date 2022/4/19
 */
internal class NoneCommand(name: String) : Command(name, URL("cmd", "localhost", 9102, "/${name}", HANDLER))

private val HANDLER = object : URLStreamHandler() {
    override fun openConnection(url: URL?) = object : URLConnection(url) {
        override fun connect() {
        }

        override fun getInputStream(): InputStream = ByteArrayInputStream(ByteArray(0))
    }
}