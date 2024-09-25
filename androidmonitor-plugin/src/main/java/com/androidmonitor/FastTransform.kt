package com.androidmonitor

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.myl.fastplugin.common.*
import com.myl.fastplugin.common.utils.*
import org.apache.commons.compress.archivers.jar.JarArchiveEntry
import org.apache.commons.compress.archivers.zip.ParallelScatterZipCreator
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.parallel.InputStreamSupplier
import org.gradle.api.Project
import java.io.File
import java.io.IOException
import java.net.URI
import java.util.concurrent.*
import java.util.jar.JarFile

/**
 * Copyright (C) @2022 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * @description FastPlugin的Transform
 * @author jadenma
 * @date 2022/4/5
 */
class FastTransform(private val project: Project) : Transform() {

    private val outputs = CopyOnWriteArrayList<File>()

    private val collectors = CopyOnWriteArrayList<Collector<*>>()

    internal val transformers = loadTransformers(project.buildscript.classLoader)

    private var transformContext: TransformContext? = null

    companion object {
        private const val NAME = "FastTransform"
    }

    override fun getName(): String {
        return NAME
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean {
        return false
    }

    override fun transform(transformInvocation: TransformInvocation) {
        super.transform(transformInvocation)
        transformContext = TransformContext(transformInvocation, project)
        //todo pretransform处理，也需要线程池
        if (isIncremental) {
            doTransform(this::doIncrementalTransform)
        } else {
            transformInvocation.outputProvider?.deleteAll()
            doTransform(this::transformFully)
        }
    }

    private fun doTransform(transFunc: (ExecutorService, Set<File>) -> Iterable<Future<*>>) {
        this.outputs.clear()
        this.collectors.clear()
        val executor = Executors.newFixedThreadPool(NCPU)


        // Look ahead to determine which input need to be transformed even incremental build
        val outOfDate = this.lookAhead(executor).onEach {
            project.logger.info("✨ ${it.canonicalPath} OUT-OF-DATE ")
        }

        doPreTransform(executor).forEach {
            it.get()
        }
        onAfterPreTransform()

        onBeforeTransform()
        try {
            transFunc(executor, outOfDate).forEach {
                it.get()
            }
        } finally {
            executor.shutdown()
            executor.awaitTermination(1, TimeUnit.HOURS)
        }
        onAfterTransform()
    }

    private fun doPreTransform(executor: ExecutorService) =
        transformContext!!.transformInvocation.inputs.map {
            it.jarInputs + it.directoryInputs
        }.flatten().map { input ->
            executor.submit {
                extractClassFile(input.file)
            }
        }

    private fun onBeforeTransform() {
        transformers.forEach { pluginTransform ->
            transformContext?.let { it -> pluginTransform.onBeforeTransform(it) }
        }
    }

    private fun onAfterPreTransform() {
        transformers.forEach { pluginTransform ->
            transformContext?.let { it -> pluginTransform.onAfterPreTransform(it) }
        }
    }

    private fun lookAhead(executor: ExecutorService): Set<File> {
        transformContext?.transformInvocation?.apply {
            return inputs.asSequence().map {
                it.jarInputs + it.directoryInputs
            }.flatten().map { input ->
                executor.submit(Callable {
                    input.file.takeIf { file ->
                        file.collect(CompositeCollector(collectors)).isNotEmpty()
                    }
                })
            }.mapNotNull {
                it.get()
            }.toSet()
        }
        return emptySet()
    }


    private fun doIncrementalTransform(executor: ExecutorService, outOfDate: Set<File>) =
        transformContext!!.transformInvocation.inputs.map { input ->
            input.jarInputs.filter {
                it.status != Status.NOTCHANGED || outOfDate.contains(it.file)
            }.map { jarInput ->
                executor.submit {
                    doIncrementalTransform(jarInput)
                }
            } + input.directoryInputs.filter {
                it.changedFiles.isNotEmpty() || outOfDate.contains(it.file)
            }.map { dirInput ->
                executor.submit {
                    doIncrementalTransform(dirInput, dirInput.file.toURI())
                }
            }
        }.flatten()

    @Suppress("NON_EXHAUSTIVE_WHEN")
    private fun doIncrementalTransform(dirInput: DirectoryInput, base: URI) {
        dirInput.changedFiles.forEach { (file, status) ->
            when (status) {
                Status.REMOVED -> {
                    project.logger.info("Deleting $file")
                    transformContext?.transformInvocation?.outputProvider?.let { provider ->
                        provider.getContentLocation(
                            dirInput.name,
                            dirInput.contentTypes,
                            dirInput.scopes,
                            Format.DIRECTORY
                        ).parentFile.listFiles()?.asSequence()
                            ?.filter { it.isDirectory }
                            ?.map { File(it, dirInput.file.toURI().relativize(file.toURI()).path) }
                            ?.filter { it.exists() }
                            ?.forEach { it.delete() }
                    }
                    file.delete()
                }
                else -> {
                    project.logger.info("Transforming $file")
                    transformContext?.transformInvocation?.outputProvider?.let { provider ->
                        val root = provider.getContentLocation(
                            dirInput.name,
                            dirInput.contentTypes,
                            dirInput.scopes,
                            Format.DIRECTORY
                        )
                        val output = File(root, base.relativize(file.toURI()).path)
                        transformInputFile(inputFile = file, output = output)
                    }
                }
            }
        }
    }

    @Suppress("NON_EXHAUSTIVE_WHEN")
    private fun doIncrementalTransform(jarInput: JarInput) {
        when (jarInput.status) {
            Status.REMOVED -> jarInput.file.delete()
            else -> {
                project.logger.info("Transforming ${jarInput.file}")
                transformContext?.transformInvocation?.outputProvider?.let { provider ->
                    val output = provider.getContentLocation(
                        jarInput.name,
                        jarInput.contentTypes,
                        jarInput.scopes,
                        Format.JAR
                    )
                    transformInputFile(jarInput.file, output)
                }
            }
        }
    }

    private fun transformFully(
        executor: ExecutorService,
        @Suppress("UNUSED_PARAMETER") outOfDate: Set<File>
    ) = transformContext!!.transformInvocation.inputs.map {
        it.jarInputs + it.directoryInputs
    }.flatten().map { input ->
        executor.submit {
            val format = if (input is DirectoryInput) Format.DIRECTORY else Format.JAR
            transformContext!!.transformInvocation.outputProvider?.let { provider ->
                project.logger.info("Transforming ${input.file}")
                val output = provider.getContentLocation(
                    input.name,
                    input.contentTypes,
                    input.scopes,
                    format
                )
                transformInputFile(input.file, output)
            }
        }
    }

    private fun onAfterTransform() {
        transformers.forEach { pluginTransform ->
            transformContext?.let { it -> pluginTransform.onAfterTransform(it) }
        }
    }

    private fun transformInputFile(inputFile: File, output: File) {
        outputs += output
        inputFile.apply {
            when {
                isDirectory -> {
                    inputFile.toURI().let { base ->
                        this.search().parallelStream().forEach {
                            transformInputFile(
                                it,
                                File(output, base.relativize(it.toURI()).path)
                            )
                        }
                    }
                }

                isFile -> when (extension.toLowerCase()) {
                    FILE_JAR -> JarFile(this).use {
                        transformClass(it, output)
                    }
                    FILE_CLASS -> this.inputStream().use {
                        transformClass(it.readBytes()).redirect(output)
                    }
                    else -> this.copyTo(output, true)
                }
                else -> throw IOException("Unexpected file: ${this.canonicalPath}")
            }
        }
    }

    private fun transformClass(jarFile: JarFile, output: File) {
        output.touch().outputStream().buffered().use {
            val entries = mutableSetOf<String>()
            val creator = ParallelScatterZipCreator(
                ThreadPoolExecutor(
                    NCPU,
                    NCPU,
                    0L,
                    TimeUnit.MILLISECONDS,
                    LinkedBlockingQueue<Runnable>(),
                    Executors.defaultThreadFactory(),
                    RejectedExecutionHandler { runnable, _ ->
                        runnable.run()
                    })
            )

            jarFile.entries().asSequence().forEach { entry ->
                if (!entries.contains(entry.name)) {
                    val zae = JarArchiveEntry(entry)
                    val stream = InputStreamSupplier {
                        when (entry.name.substringAfterLast('.', "")) {
                            FILE_CLASS -> jarFile.getInputStream(entry).use { src ->
                                try {
                                    transformClass(src.readBytes()).inputStream()
                                } catch (e: Throwable) {
                                    System.err.println("Broken class: ${this.name}!/${entry.name}")
                                    jarFile.getInputStream(entry)
                                }
                            }
                            else -> jarFile.getInputStream(entry)
                        }
                    }

                    creator.addArchiveEntry(zae, stream)
                    entries.add(entry.name)
                } else {
                    System.err.println("Duplicated jar entry: ${this.name}!/${entry.name}")
                }
            }

            ZipArchiveOutputStream(output).use(creator::writeTo)
        }
    }




    private fun extractClassFile(inputFile: File) {
        inputFile.apply {
            when {
                isDirectory -> toURI().let { base ->
                    inputFile.search().parallelStream().forEach {
                        extractClassFile(it)
                    }
                }
                isFile -> when (inputFile.extension.toLowerCase()) {
                    FILE_JAR -> {
                        extractJar(JarFile(inputFile))
                    }
                    FILE_CLASS -> {
                        inputStream().use {
                            preTransform(it.readBytes())
                        }
                    }
                }
            }
        }
    }

    private fun preTransform(readBytes: ByteArray) {
        transformers.forEach {
            transformContext?.let { it1 -> it.preTransform(it1, readBytes) }
        }
    }

    private fun transformClass(readBytes: ByteArray): ByteArray {
        //fold函数，详细使用参考https://juejin.cn/post/6971087979045453855，方法返回的结果作为下一次循环的参数，这样就把所有的transformers串行起来，
        // 第一个的输出作为第二个的输入，一直串行执行，就使得在一个Transform里运行了所有的插件transformers
        return transformers.fold(readBytes) { bytes, transformer ->
            transformer.transform(transformContext!!, bytes)
        }
    }

    private fun extractJar(jarFile: JarFile) {
        val entries = mutableSetOf<String>()
        jarFile.entries().asSequence().forEach { entry ->
            if (!entries.contains(entry.name)) {
                when (entry.name.substringAfterLast('.', "")) {
                    FILE_CLASS -> jarFile.getInputStream(entry).use { src ->
                        try {
                            preTransform(src.readBytes())
                        } catch (e: Throwable) {
                            System.err.println("Broken class: ${this.name}!/${entry.name}")
                        }
                    }
                }
            }
        }
    }
}