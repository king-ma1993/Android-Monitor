package com.myl.fastplugin.asm.transform

import com.myl.fastplugin.common.ITransform
import com.myl.fastplugin.common.TransformContext
import org.objectweb.asm.tree.ClassNode
import java.io.File

/**
 * Copyright (C) @2022 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * @description asm字节码操作Transform接口
 * @author jadenma
 * @date 2022/4/6
 */
interface IClassTransform : ITransform {

    val name: String
        get() = javaClass.simpleName

    /**
     * 对asm的class node进行处理
     *
     * @param transformContext 插件上下文
     * @param klass 要被处理的class node
     * @return 处理后的class node
     */
    fun transform(transformContext: TransformContext, klass: ClassNode) = klass

    /**
     * 用于Transform预处理一遍字节码，例如收集类关系图等
     * @param transformContext 插件上下文
     * @param klass 原始文件的class node
     */
    fun preTransform(transformContext: TransformContext, klass: ClassNode) {}


    fun getReport(transformContext: TransformContext, name: String): File {
        val report: File by lazy {
            val dir = getReportDir(transformContext)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val file = File(dir, name)
            if (!file.exists()) {
                file.createNewFile()
            }
            file
        }
        return report
    }

    private fun getReportDir(transformContext: TransformContext): File = File(
        File(transformContext.reportsDir(), name), transformContext.transformInvocation.context.variantName
    )
}