package com.myl.fastplugin.common


/**
 * 
 *
 * @description gradle插件的transform
 * 
 * @date 2022/4/6
 */
interface IPluginTransform : ITransform {
    /**
     * Returns 处理后的字节码
     *
     * @param transformContext plugin上下文
     * @param bytecode 要被处理的字节码
     * @return 返回处理过的bytecode
     */
    fun transform(transformContext: TransformContext, bytecode: ByteArray): ByteArray

    /**
     * 用于Transform预处理一遍字节码，例如收集类关系图等
     * @param transformContext plugin上下文
     * @param bytecode 原始的字节码
     */
    fun preTransform(transformContext: TransformContext, bytecode: ByteArray)
}