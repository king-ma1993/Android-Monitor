package com.androidmonitor.method_trace_plugin

import com.androidmonitor.method_trace_plugin.item.TraceMethod
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnList
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger


class MethodCollector {
    companion object {
        private const val CONSTRUCTOR = "<init>"
    }

    //类继承关系的map
    private val collectedClassExtendMap = ConcurrentHashMap<String, String>()
    private val collectedIgnoreMethodMap = ConcurrentHashMap<String, TraceMethod>()
    private val collectedMethodMap = ConcurrentHashMap<String, TraceMethod>()
    private val ignoreCount = AtomicInteger()
    private val incrementCount = AtomicInteger()
    private val methodId = AtomicInteger()

    fun collectClassExtend(klass: ClassNode) {
        val className = klass.name
        val superName = klass.superName
        collectedClassExtendMap[className] = superName
    }

    fun collectMethod(klass: ClassNode) {
        klass.methods.forEach { methodNode ->
            val className = klass.name
            val access = methodNode.access
            val name = methodNode.name
            val desc = methodNode.desc
            val traceMethod: TraceMethod = TraceMethod.create(0, access, className, name, desc)
            val isConstructor = CONSTRUCTOR == name
            // todo 白名单逻辑先不做
//            val isNeedTrace: Boolean = MethodCollector.isNeedTrace(
//                configuration,
//                traceMethod.className,
//                mappingCollector
//            )
            // filter simple methods
            if ((isEmptyMethod(methodNode.instructions) || isGetSetMethod(
                    methodNode.instructions,
                    isConstructor
                ) || isSingleMethod(methodNode.instructions))
            ) {
                ignoreCount.incrementAndGet()
                collectedIgnoreMethodMap[traceMethod.getMethodName()] = traceMethod
                return
            }
            if (!collectedMethodMap.containsKey(traceMethod.getMethodName())) {
                traceMethod.id = methodId.incrementAndGet()
                collectedMethodMap[traceMethod.getMethodName()] = traceMethod
                incrementCount.incrementAndGet()
            } else if (!collectedIgnoreMethodMap.containsKey(traceMethod.className)) {
                ignoreCount.incrementAndGet()
                collectedIgnoreMethodMap[traceMethod.getMethodName()] = traceMethod
            }
        }


    }

    private fun isSingleMethod(instructions: InsnList): Boolean {
        val iterator: ListIterator<AbstractInsnNode> = instructions.iterator()
        while (iterator.hasNext()) {
            val insnNode = iterator.next()
            val opcode = insnNode.opcode
            if (-1 == opcode) {
                continue
                //操作码范围：INVOKEVIRTUAL 到 INVOKEDYNAMIC 包含了多种方法调用指令，如 INVOKEVIRTUAL、INVOKEINTERFACE、INVOKESPECIAL 和 INVOKEDYNAMIC 等。
                //方法调用：这些操作码都表示直接或间接地调用了其他方法。例如：
                //INVOKEVIRTUAL 用于调用对象的实例方法。
                //INVOKEINTERFACE 用于调用接口方法。
                //INVOKESPECIAL 用于调用私有方法、实例初始化方法 (<init>) 或父类方法。
                //INVOKEDYNAMIC 用于动态解析方法调用。
                //单一方法判断：函数 isSingleMethod 的目的是检查当前方法体内部是否只包含单一的方法调用。如果遇到上述范围内的任何操作码，说明当前方法体内至少调用了另一个方法，因此返回 false。
                //综上所述，当检测到 INVOKEVIRTUAL 到 INVOKEDYNAMIC 范围内的操作码时，表明当前方法不是单一方法，故返回 false。
            } else if (Opcodes.INVOKEVIRTUAL <= opcode && opcode <= Opcodes.INVOKEDYNAMIC) {
                return false
            }
        }
        return true
    }

    /**
     * 遍历指令列表，检查每个指令节点的opcode值。
     * 如果找到有效的opcode（不为-1），则返回false表示方法体不为空。
     * 若所有指令节点的opcode均为-1，则返回true表示方法体为空。
     */
    private fun isEmptyMethod(instructions: InsnList): Boolean {
        val iterator: ListIterator<AbstractInsnNode> = instructions.iterator()
        while (iterator.hasNext()) {
            val insnNode = iterator.next()
            val opcode = insnNode.opcode
            return if (-1 == opcode) {
                continue
            } else {
                false
            }
        }
        return true
    }

    private fun isGetSetMethod(instructions: InsnList, isConstructor: Boolean): Boolean {
        var ignoreCount = 0
        val iterator: ListIterator<AbstractInsnNode> = instructions.iterator()
        while (iterator.hasNext()) {
            val insnNode = iterator.next()
            val opcode = insnNode.opcode
            if (-1 == opcode) {
                continue
            }
            if (opcode != Opcodes.GETFIELD
                && opcode != Opcodes.GETSTATIC
                && opcode != Opcodes.H_GETFIELD
                && opcode != Opcodes.H_GETSTATIC
                && opcode != Opcodes.RETURN
                && opcode != Opcodes.ARETURN
                && opcode != Opcodes.DRETURN
                && opcode != Opcodes.FRETURN
                && opcode != Opcodes.LRETURN
                && opcode != Opcodes.IRETURN
                && opcode != Opcodes.PUTFIELD
                && opcode != Opcodes.PUTSTATIC
                && opcode != Opcodes.H_PUTFIELD
                && opcode != Opcodes.H_PUTSTATIC
                && opcode > Opcodes.SALOAD
            ) {
                //这段代码允许构造函数中出现一次 INVOKESPECIAL 指令，以处理父类构造函数的调用。
                //如果构造函数中有超过一次 INVOKESPECIAL 调用，则认为该构造函数较为复杂，不符合简单的 getter 或 setter 方法的定义。
                //INVOKESPECIAL除了显式调用父类方法：当你在子类中使用 super 关键字来调用父类的方法时，JVM 也会使用 INVOKESPECIAL 指令
                //还可能是私有方法的调用：当一个方法被声明为 private 时，JVM 使用 INVOKESPECIAL 来调用这些方法。这是因为私有方法的调用在编译期是确定的，并且不会受到继承的影响。
                //所以当构造方法除了调用super的构造方法还调用其他的私有函数时，就认为不是简单的函数了
                if (isConstructor && opcode == Opcodes.INVOKESPECIAL) {
                    ignoreCount++
                    if (ignoreCount > 1) {
                        return false
                    }
                    continue
                }
                return false
            }
        }
        return true
    }

}