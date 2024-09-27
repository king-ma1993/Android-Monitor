package com.androidmonitor.method_trace_plugin

import com.androidmonitor.TraceBuildConstants
import com.androidmonitor.TraceBuildConstants.MATRIX_TRACE_CLASS
import com.androidmonitor.method_trace_plugin.MethodTraceUtils.noNeedTraceClass
import com.androidmonitor.method_trace_plugin.item.TraceMethod
import com.google.auto.service.AutoService
import com.myl.fastplugin.asm.transform.IClassTransform
import com.myl.fastplugin.common.TransformContext
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.VarInsnNode
import org.stringtemplate.v4.compiler.Bytecode.instructions
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger


@AutoService(IClassTransform::class)
class MethodTraceTransform : IClassTransform {

    private val TAG = "MethodTraceTransform"

    private val methodCollector = MethodCollector()
    private var hasWindowFocusMethod = false
    private var isActivityOrSubClass = false
    private val traceMethodCount = AtomicInteger()

    override fun transform(transformContext: TransformContext, klass: ClassNode): ClassNode {
        if (noNeedTraceClass(klass.name)) {
            return klass
        }
        val className = klass.name
        isActivityOrSubClass =
            isActivityOrSubClass(className, methodCollector.getCollectedClassExtendMap());
        traceMethod(klass)
        return klass
    }

    private fun traceMethod(klass: ClassNode) {
        val methodList = klass.methods
        val collectedMethodMap = methodCollector.getCollectedMethodMap()
        methodList.forEach { methodNode ->
            val desc = methodNode.desc
            val methodName = methodNode.name
            if (!hasWindowFocusMethod) {
                hasWindowFocusMethod = methodCollector.isWindowFocusChangeMethod(methodName, desc)
            }
            if (isABSClass(klass)) return
            val traceMethod = collectedMethodMap[methodName]
            traceMethod?.let { traceMethod ->
                insertInMethodEnter(methodNode, traceMethod, klass)
                insertInMethodExit(methodNode, traceMethod)
            }
        }
    }

    private fun insertInMethodExit(
        methodNode: MethodNode?,
        traceMethod: TraceMethod,
    ) {
        methodNode ?: return
        val instructions: InsnList = methodNode.instructions
        // 查找方法的返回指令
        val returnInsn = findReturnInsn(instructions)
        returnInsn?.let { returnInsn ->
            // 创建 LDC 指令节点
            val ldcInsnNode = LdcInsnNode(traceMethod.id)
            // 创建 INVOKESTATIC 指令节点
            val invokeStaticInsnNode = MethodInsnNode(
                Opcodes.INVOKESTATIC,
                MATRIX_TRACE_CLASS,
                "o",
                "(I)V",
                false
            )
            // 在返回指令之前插入 LDC 指令
            instructions.insertBefore(returnInsn, ldcInsnNode)
            // 在返回指令之前插入 INVOKESTATIC 指令
            instructions.insertBefore(returnInsn, invokeStaticInsnNode)
        }
    }

    private fun findReturnInsn(instructions: InsnList): AbstractInsnNode? {
        for (insn in instructions.toArray()) {
            val opcode = insn.opcode
            if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
                return insn
            }
        }
        return null // 如果没有找到返回指令，返回 null
    }

    private fun insertInMethodEnter(
        methodNode: MethodNode,
        traceMethod: TraceMethod,
        klass: ClassNode
    ) {
        traceMethodCount.incrementAndGet()
        val insnList: InsnList = methodNode.instructions
        insnList.let { insnList ->
            // 创建 LDC 指令节点
            val ldcInsnNode = LdcInsnNode(traceMethod.id)
            // 创建 INVOKESTATIC 指令节点
            val invokeStaticInsnNode = MethodInsnNode(
                Opcodes.INVOKESTATIC,
                TraceBuildConstants.MATRIX_TRACE_CLASS,
                "i",
                "(I)V",
                false
            )
            val methodBegin = insnList.first
            insnList.insertBefore(methodBegin, ldcInsnNode)
            insnList.insertBefore(methodBegin, invokeStaticInsnNode)
            if (checkNeedTraceWindowFocusChangeMethod(traceMethod, klass.name)) {
                traceWindowFocusChangeMethod(insnList)
            }
        }
    }

    private fun traceWindowFocusChangeMethod(insnList: InsnList) {
        val methodBegin = insnList.first
        // 创建 ALOAD 0 指令
        val aload0 = VarInsnNode(Opcodes.ALOAD, 0)
        // 创建 ILOAD 1 指令
        val iload1 = VarInsnNode(Opcodes.ILOAD, 1)
        // 创建 INVOKESTATIC 指令
        val invokestatic = MethodInsnNode(
            Opcodes.INVOKESTATIC,
            TraceBuildConstants.MATRIX_TRACE_CLASS,
            "at",
            "(Landroid/app/Activity;Z)V",
            false
        )
        // 将指令插入到方法的开始处
        insnList.insertBefore(methodBegin, aload0)
        insnList.insertBefore(methodBegin, iload1)
        insnList.insertBefore(methodBegin, invokestatic)
    }

    private fun checkNeedTraceWindowFocusChangeMethod(
        traceMethod: TraceMethod,
        className: String
    ): Boolean {
        if (hasWindowFocusMethod && isActivityOrSubClass) {
            val windowFocusChangeMethod = TraceMethod.create(
                -1,
                Opcodes.ACC_PUBLIC,
                className,
                TraceBuildConstants.MATRIX_TRACE_ON_WINDOW_FOCUS_METHOD,
                TraceBuildConstants.MATRIX_TRACE_ON_WINDOW_FOCUS_METHOD_ARGS
            )
            if (windowFocusChangeMethod.equals(traceMethod)) {
                return true
            }
        }
        return false
    }

    private fun isActivityOrSubClass(
        className: String?,
        mCollectedClassExtendMap: ConcurrentHashMap<String, String>
    ): Boolean {
        var className = className
        className = className!!.replace(".", "/")
        val isActivity =
            className == TraceBuildConstants.MATRIX_TRACE_ACTIVITY_CLASS
                    || className == TraceBuildConstants.MATRIX_TRACE_V4_ACTIVITY_CLASS
                    || className == TraceBuildConstants.MATRIX_TRACE_V7_ACTIVITY_CLASS
                    || className == TraceBuildConstants.MATRIX_TRACE_ANDROIDX_ACTIVITY_CLASS
        return if (isActivity) {
            true
        } else {
            if (!mCollectedClassExtendMap.containsKey(className)) {
                false
            } else {
                isActivityOrSubClass(mCollectedClassExtendMap[className], mCollectedClassExtendMap)
            }
        }
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
        ConfProvider.init(transformContext)
        methodCollector.collectClassExtend(klass)
        if (isABSClass(klass)) return
        methodCollector.collectMethod(klass)
    }

    override fun onAfterPreTransform(transformContext: TransformContext) {
        //todo 先把字节码插桩阶段放到混淆前，所以这里先不用处理混淆映射的逻辑
        saveIgnoreCollectedMethod()
        saveCollectedMethod()
    }

    private fun saveIgnoreCollectedMethod() {
        methodCollector.saveIgnoreCollectedMethod()
    }

    private fun saveCollectedMethod() {
        methodCollector.saveCollectedMethod()
    }

}