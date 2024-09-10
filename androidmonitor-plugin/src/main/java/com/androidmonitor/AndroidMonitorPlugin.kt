package com.androidmonitor

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.myl.android.gradle.api.getAndroid
import com.myl.fastplugin.common.ANDROID_APPLICATION
import com.myl.fastplugin.common.ANDROID_LIBRARY
import com.myl.fastplugin.common.DYNAMIC_FEATURE
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidMonitorPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        when {
            project.plugins.hasPlugin(ANDROID_APPLICATION) || project.plugins.hasPlugin(
                DYNAMIC_FEATURE
            ) -> project.getAndroid<AppExtension>()
                .let { android ->
                    android.registerTransform(FastTransform(project))
                    project.afterEvaluate {
                        loadVariantProcessors(project).let { processors ->
                            android.applicationVariants.forEach { variant ->
                                processors.forEach { processor ->
                                    processor.process(variant)
                                }
                            }
                        }
                    }
                }
            project.plugins.hasPlugin(ANDROID_LIBRARY) -> project.getAndroid<LibraryExtension>()
                .let { android ->
                    android.registerTransform(FastTransform(project))
                    project.afterEvaluate {
                        loadVariantProcessors(project).let { processors ->
                            android.libraryVariants.forEach { variant ->
                                processors.forEach { processor ->
                                    processor.process(variant)
                                }
                            }
                        }
                    }
                }
        }
    }
}