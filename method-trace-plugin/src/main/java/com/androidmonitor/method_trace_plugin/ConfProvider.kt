package com.androidmonitor.method_trace_plugin

import com.android.build.gradle.AppExtension
import com.android.builder.model.AndroidProject.FD_OUTPUTS
import com.android.tools.build.jetifier.core.utils.Log
import com.google.common.base.Joiner
import com.myl.fastplugin.common.TransformContext
import org.gradle.api.Project
import java.io.File

object ConfProvider {

    private const val TAG = "ConfProvider"

    private lateinit var transformContext: TransformContext
    private var variantDirNames: Map<String, String> = emptyMap()
    private var configuration: Configuration? = null


    fun init(transformContext: TransformContext) {
        this.transformContext = transformContext
        val android = getProject().extensions.getByType(AppExtension::class.java)
        variantDirNames = android.applicationVariants.associateBy(
            { it.name },
            { it.dirName }
        )
    }

    private fun getMappingOut(): String {
        val dirName = variantDirNames[getVariantName()]
        val mappingOut = Joiner.on(File.separatorChar).join(
            getProject().buildDir.absolutePath,
            FD_OUTPUTS,
            "mapping",
            dirName
        )
        Log.i(TAG, "getMappingOut: $mappingOut")
        return mappingOut
    }

    private fun getProject(): Project {
        return transformContext.project
    }

    private fun getVariantName(): String {
        return transformContext.transformInvocation.context.variantName
    }

    private fun getIgnoreMethodMapFilePath(): String {
        return "${getMappingOut()}/ignoreMethodMapping.txt"
    }


    fun getConfiguration(): Configuration {
        if (configuration == null) {
            configuration = Configuration(ignoreMethodMapFilePath = getIgnoreMethodMapFilePath())
        }
        return configuration!!
    }
}