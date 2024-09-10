package com.myl.fastplugin.common.command

import java.io.File

/**
 *
 * @description 安装的Command
 * @date 2022/4/19
 */
internal class InstalledCommand(name: String, exe: File) : Command(name, exe.toURI().toURL())