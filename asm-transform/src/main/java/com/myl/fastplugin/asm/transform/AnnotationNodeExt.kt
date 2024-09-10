package com.myl.fastplugin.asm.transform

import org.objectweb.asm.tree.AnnotationNode

/**
 * Copyright (C) @2022 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * @description AnnotationNode拓展函数
 * @author jadenma
 * @date 2022/5/1
 */

@Suppress("UNCHECKED_CAST")
fun <T> AnnotationNode.getValue(name: String = "value"): T? = values?.withIndex()?.iterator()?.let {
    while (it.hasNext()) {
        val i = it.next()
        if (i.index % 2 == 0 && i.value == name) {
            return@let it.next().value as T
        }
    }
    null
}
