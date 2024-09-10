package com.myl.fastplugin.common

import com.android.build.gradle.api.BaseVariant

/**
 * 
 *
 * @description 插件接口
 * 
 * @date 2022/4/13
 */

interface VariantProcessor {
    fun process(variant: BaseVariant)
}
