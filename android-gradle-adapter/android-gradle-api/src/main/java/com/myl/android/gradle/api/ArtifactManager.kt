package com.myl.android.gradle.api

import java.io.File

/**
 * Copyright (C) @2022 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * @description Artifact管理类
 * @author jadenma
 * @date 2022/3/31 8:19 下午
 */
interface ArtifactManager {
    companion object {
        const val AAR = "AAR"
        const val ALL_CLASSES = "ALL_CLASSES"
        const val APK = "APK"
        const val MERGED_ASSETS = "MERGED_ASSETS"
        const val MERGED_RES = "MERGED_RES"
        const val MERGED_MANIFESTS = "MERGED_MANIFESTS"
        const val PROCESSED_RES = "PROCESSED_RES"
        const val SYMBOL_LIST = "SYMBOL_LIST"
        const val SYMBOL_LIST_WITH_PACKAGE_NAME = "SYMBOL_LIST_WITH_PACKAGE_NAME"
        const val DATA_BINDING_DEPENDENCY_ARTIFACTS = "DATA_BINDING_DEPENDENCY_ARTIFACTS"
    }

    /**
     * 根据类型返回特定的artifacts
     *
     * @param type artifacts的类型
     */
    fun get(type: String): Collection<File> = emptyList()
}