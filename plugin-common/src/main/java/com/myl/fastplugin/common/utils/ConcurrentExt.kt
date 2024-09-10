package com.myl.fastplugin.common.utils

import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinTask

/**
 * 
 *
 * @description 并发拓展函数
 * 
 * @date 2022/3/31 9:10 下午
 */
fun <T> ForkJoinTask<T>.execute(): T {
    val pool = ForkJoinPool()
    val result = pool.invoke(this)
    pool.shutdown()
    return result
}