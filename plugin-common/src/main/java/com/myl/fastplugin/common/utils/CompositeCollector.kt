package com.myl.fastplugin.common.utils

/**
 * 
 *
 * @description 合成的收集
 * 
 * @date 2022/5/2
 */
class CompositeCollector(private val collectors: Iterable<Collector<*>>) : Collector<List<*>> {

    constructor(vararg collectors: Collector<*>) : this(collectors.asIterable())

    override fun accept(name: String): Boolean {
        return collectors.any { collector ->
            collector.accept(name)
        }
    }

    override fun collect(name: String, data: () -> ByteArray): List<*> {
        return collectors.filter {
            it.accept(name)
        }.mapNotNull {
            it.collect(name, data)
        }
    }
}
