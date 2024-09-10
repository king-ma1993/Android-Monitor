package com.androidmonitor

import com.myl.fastplugin.common.IPluginTransform
import com.myl.fastplugin.common.VariantProcessor
import org.gradle.api.Project
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.ServiceConfigurationError

/**
 * Copyright (C) @2022 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * @description 插件ServiceLoader
 * @author jadenma
 * @date 2022/4/13
 */
internal inline fun <reified T> newServiceLoader(classLoader: ClassLoader, vararg types: Class<*>) =
    ServiceLoaderFactory(classLoader, T::class.java).newServiceLoader(*types)

internal interface ServiceLoader<T> {
    fun load(vararg args: Any): List<T>
}

internal class ServiceLoaderFactory<T>(
    private val classLoader: ClassLoader,
    private val service: Class<T>
) {

    fun newServiceLoader(vararg types: Class<*>) = object : ServiceLoader<T> {

        override fun load(vararg args: Any) =
            classLoader.getResources("META-INF/services/${service.name}")?.asSequence()
                ?.map(::parse)?.flatten()?.toSet()?.map { provider ->
                    try {
                        val providerClass = Class.forName(provider, false, classLoader)
                        if (!service.isAssignableFrom(providerClass)) {
                            throw ServiceConfigurationError("Provider $provider not a subtype")
                        }

                        try {
                            providerClass.getConstructor(*types).newInstance(*args) as T
                        } catch (e: NoSuchMethodException) {
                            providerClass.newInstance() as T
                        }
                    } catch (e: ClassNotFoundException) {
                        throw ServiceConfigurationError("Provider $provider not found")
                    }
                } ?: emptyList()

    }

}


/**
 * Load [VariantProcessor]s with the specified [classLoader]
 */
@Throws(ServiceConfigurationError::class)
internal fun loadVariantProcessors(project: Project) =
    newServiceLoader<VariantProcessor>(project.buildscript.classLoader, Project::class.java).load(
        project
    )


/**
 * Load [IPluginTransform]s with the specified [classLoader]
 */
@Throws(ServiceConfigurationError::class)
internal fun loadTransformers(classLoader: ClassLoader) =
    newServiceLoader<IPluginTransform>(classLoader, ClassLoader::class.java).load(classLoader)


private fun parse(u: URL) = try {
    u.openStream().bufferedReader(StandardCharsets.UTF_8).readLines().filter {
        it.isNotEmpty() && it.isNotBlank() && !it.startsWith('#')
    }.map(String::trim).filter(::isJavaClassName)
} catch (e: Throwable) {
    emptyList<String>()
}

private fun isJavaClassName(text: String): Boolean {
    if (!Character.isJavaIdentifierStart(text[0])) {
        throw ServiceConfigurationError("Illegal provider-class name: $text")
    }

    for (i in 1 until text.length) {
        val cp = text.codePointAt(i)
        if (!Character.isJavaIdentifierPart(cp) && cp != '.'.toInt()) {
            throw ServiceConfigurationError("Illegal provider-class name: $text")
        }
    }

    return true
}
