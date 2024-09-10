package com.myl.fastplugin.common

/**
 * 
 *
 * @description plugin内部使用常量类
 * 
 * @date 2022/4/14
 */

const val FILE_JAR = "jar"
const val FILE_CLASS = "class"
const val R_REGEX = ".*/R\\\$.*|.*/R\\.*"
const val JAVA_CLASS = "java/lang/Class"

const val ANDROID_APPLICATION = "com.android.application"
const val ANDROID_LIBRARY = "com.android.library"
const val DYNAMIC_FEATURE = "com.android.dynamic-feature"
const val PLUGIN_NAME = "FastPlugin"

val NCPU = Runtime.getRuntime().availableProcessors()