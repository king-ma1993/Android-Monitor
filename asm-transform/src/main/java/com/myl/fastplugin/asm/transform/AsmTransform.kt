package com.myl.fastplugin.asm.transform

import com.google.auto.service.AutoService
import com.myl.fastplugin.common.IPluginTransform
import com.myl.fastplugin.common.TransformContext
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.util.*

/**
 * Copyright (C) @2022 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * @description Asm字节码转化器
 * @author jadenma
 * @date 2022/4/6 4:38 下午
 */
@AutoService(IPluginTransform::class)
class AsmTransform : IPluginTransform {

    private val classLoader: ClassLoader

    private val transformers: Iterable<IClassTransform>

    constructor() : this(Thread.currentThread().contextClassLoader)

    constructor(classLoader: ClassLoader = Thread.currentThread().contextClassLoader) : this(
        ServiceLoader.load(
            IClassTransform::class.java
        ), classLoader
    )

    constructor(
        transformers: Iterable<IClassTransform>,
        classLoader: ClassLoader = Thread.currentThread().contextClassLoader
    ) {
        this.classLoader = classLoader
        this.transformers = transformers
    }

    override fun onBeforeTransform(transformContext: TransformContext) {
        transformers.forEach {
            it.onBeforeTransform(transformContext)
        }
    }

    override fun onAfterTransform(transformContext: TransformContext) {
        transformers.forEach {
            it.onAfterTransform(transformContext)
        }
    }

    override fun transform(
        transformContext: TransformContext,
        bytecode: ByteArray
    ): ByteArray {
        //COMPUTE_MAXS 告诉 ASM 自动计算栈的最大值以及最大数量的方法的本地变量。
        //这里使用的是Asm的Tree Api
        /**
         *  Tree Api标准步骤
         *  ClassReader classReader=new ClassReader("bytecode.Node");
         *  ClassNode cn = new ClassNode();
         *  classReader.accept(cn,ClassReader.EXPAND_FRAMES);
         *  ......各种字节码操作
         *  ClassWriter cw=new ClassWriter(ClassWriter.COMPUTE_MAXS);
         *  cn.accept(cw);
         */
        return ClassWriter(ClassWriter.COMPUTE_MAXS).also { writer ->
            //TODO 耗时计算先不统计，后期补充
            this.transformers.fold(ClassNode().also { klass ->
                ClassReader(bytecode).accept(klass, 0)
            }) { a, transformer ->
                transformer.transform(transformContext, a)
            }.accept(writer)
        }.toByteArray()
    }

    override fun preTransform(transformContext: TransformContext, bytecode: ByteArray) {
        transformers.forEach {
            //通过ClassNode解析class内部的信息
            val classNode = ClassNode().also { klass ->
                ClassReader(bytecode).accept(klass, 0)
            }
            it.preTransform(transformContext, classNode)
        }
    }
}