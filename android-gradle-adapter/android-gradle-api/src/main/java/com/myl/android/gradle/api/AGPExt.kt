package com.myl.android.gradle.api

import com.android.build.gradle.BaseExtension
import org.gradle.api.Project

/**
 * Copyright (C) @2022 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * @description AGP相关拓展函数
 * @author jadenma
 * @date 2022/3/31 5:37 下午
 */
inline fun <reified T : BaseExtension> Project.getAndroid(): T = extensions.getByName(ANDROID) as T

