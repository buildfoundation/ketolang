@file:JvmName("Main")

package com.pushtorefresh.rikochet.dummyapp

import java.io.File

const val x = 1
const val str = "static val"
//var varStr = "static var"
//val valArray = Array<String>(1) { " " }
//var varArray = Array<String>(1) { " " }
//val map = mutableMapOf<String, Any>()


class Y {
    companion object K {
    }
}

fun main() {
    println("Hello world! $x")
    File("test-${System.currentTimeMillis()}").writeText("abc")
    println("Text written to file!")
    println("Goodbye.")
}
