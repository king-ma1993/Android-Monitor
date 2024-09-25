package com.androidmonitor.method_trace_plugin

import com.androidmonitor.method_trace_plugin.MethodTraceUtils.noNeedTraceClass
import com.google.auto.service.AutoService
import com.myl.fastplugin.asm.transform.IClassTransform
import com.myl.fastplugin.common.TransformContext
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode

@AutoService(IClassTransform::class)
class MethodTraceTransform : IClassTransform {

    private val TAG = "MethodCollector"

    private val methodCollector = MethodCollector()

    override fun transform(transformContext: TransformContext, klass: ClassNode): ClassNode {
        if (noNeedTraceClass(klass.name)) {
            return klass
        }

        if (isABSClass(klass)) return klass

        val methodList = klass.methods
        methodList.forEach {




        }



        return klass
    }

    private fun isABSClass(klass: ClassNode): Boolean {
        val access = klass.access
        var isABSClass = false
        if (access and Opcodes.ACC_ABSTRACT > 0 || access and Opcodes.ACC_INTERFACE > 0) {
            isABSClass = true
        }

        if (isABSClass) {
            return true
        }
        return false
    }

    override fun preTransform(transformContext: TransformContext, klass: ClassNode) {
        super.preTransform(transformContext, klass)
        ConfProvider.init(transformContext)
        methodCollector.collectClassExtend(klass)
        if (isABSClass(klass)) return
        methodCollector.collectMethod(klass)
        //todo 先把字节码插桩阶段放到混淆前，所以这里先不用处理混淆映射的逻辑
        saveIgnoreCollectedMethod()

    }

    private fun saveIgnoreCollectedMethod() {
        methodCollector.saveIgnoreCollectedMethod()
    }

    private fun saveCollectedMethod() {
        methodCollector.saveCollectedMethod()
    }

}