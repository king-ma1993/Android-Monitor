package com.myl.fastplugin.common

import com.myl.fastplugin.common.utils.Collector
import com.myl.fastplugin.common.utils.search
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import java.io.File
import java.io.IOException

/**
 * 
 *
 * @description Collection拓展函数
 * 
 * @date 2022/4/4
 */
inline fun <T> Collection<T>.ifNotEmpty(action: (Collection<T>) -> Unit): Collection<T> {
    if (isNotEmpty()) {
        action(this)
    }
    return this
}

fun <T> Iterator<T>.asIterable(): Iterable<T> = Iterable { this }

/**
 * Collecting information from file with [collector], the supported file types are as follows:
 *
 * - directories
 * - archive files
 */
fun <R> File.collect(collector: Collector<R>): List<R> = when {
    this.isDirectory -> {
        val base = this.toURI()
        this.search { f ->
            f.isFile && collector.accept(base.relativize(f.toURI()).normalize().path)
        }.map { f ->
            collector.collect(base.relativize(f.toURI()).normalize().path, f::readBytes)
        }
    }
    this.isFile -> {
        this.inputStream().buffered().use {
            ArchiveStreamFactory().createArchiveInputStream(it).let { archive ->
                generateSequence {
                    try {
                        archive.nextEntry
                    } catch (e: IOException) {
                        null
                    }
                }.filterNot(ArchiveEntry::isDirectory).filter { entry ->
                    collector.accept(entry.name)
                }.map { entry ->
                    collector.collect(entry.name, archive::readBytes)
                }.toList()
            }
        }
    }
    else -> emptyList()
}