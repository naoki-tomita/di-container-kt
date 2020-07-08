package com.tomita

import java.io.File
import java.net.URL
import java.util.*
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

class DIContainer {
    companion object {
        val CLASSES: MutableList<Class<*>> = mutableListOf()
        val INSTANCES: MutableMap<Class<*>, Any> = mutableMapOf()

        fun initialize() {
            // Collect classes.
            Thread.currentThread().contextClassLoader!!.definedPackages.forEach {
                CLASSES.addAll(getClasses(it.name).filter { it.isAnnotationPresent(Named::class.java) })
            }

            // Construct classes.
            // It can construct only zero parameter constructor.
            CLASSES.forEach { INSTANCES[it] = it.constructors[0].newInstance() }

            // Inject instances.
            INSTANCES.entries.forEach {(clazz, instance) ->
                instance::class.memberProperties
                        .filter { it.findAnnotation<Binding>() != null }
                        .forEach { it.javaField!!.set(instance, INSTANCES[it.javaField!!.type]) }
            }
        }

        // コピペ
        private fun getClasses(packageName: String): List<Class<*>> {
            val classLoader = Thread.currentThread().contextClassLoader!!
            val path = packageName.replace('.', '/')
            val resources: Enumeration<URL> = classLoader.getResources(path)
            val dirs = mutableListOf<File>()
            while (resources.hasMoreElements()) {
                val resource: URL = resources.nextElement()
                dirs.add(File(resource.getFile()))
            }
            val classes = mutableListOf<Class<*>>()
            for (directory in dirs) {
                DIContainer.findClasses(directory, packageName).forEach { classes.add(it) }
            }
            return classes
        }

        // コピペ
        private fun findClasses(directory: File, packageName: String): List<Class<*>> {
            val classes: MutableList<Class<*>> = mutableListOf()
            if (!directory.exists()) {
                return classes
            }
            val files = directory.listFiles()
            for (file in files) {
                if (file.isDirectory) {
                    assert(!file.name.contains("."))
                    classes.addAll(findClasses(file, packageName + "." + file.name)!!)
                } else if (file.name.endsWith(".class")) {
                    classes.add(Class.forName(packageName + '.' + file.name.substring(0, file.name.length - 6)))
                }
            }
            return classes
        }

        fun <T>get(clazz: Class<T>): T? {
            return INSTANCES[clazz] as T
        }
    }
}

annotation class Named
annotation class Binding
