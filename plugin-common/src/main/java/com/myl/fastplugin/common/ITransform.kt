package com.myl.fastplugin.common

/**
 * 
 *
 * @description tramsform的抽象接口
 * 
 * @date 2022/4/6 4:13 下午
 */
interface ITransform {

    fun onBeforeTransform(transformContext: TransformContext) {}

    fun onAfterTransform(transformContext: TransformContext) {}

    fun onAfterPreTransform(transformContext: TransformContext) {}
}