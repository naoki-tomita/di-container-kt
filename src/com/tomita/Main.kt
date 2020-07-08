package com.tomita

import java.io.File
import java.io.IOException
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.net.URL
import java.util.*
import kotlin.collections.HashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

@Named
class Injectable {
    val foo = "foo"

    fun method(): String {
        return "hello world $foo"
    }
}

@Named
class Injector {
    @Binding
    lateinit var foo: Injectable

    fun method(): String {
        return "hello ${foo.method()}"
    }
}


fun main() {
    DIContainer.initialize()
    val injector = DIContainer.get(Injector::class.java)
    println(injector!!.method())
}
