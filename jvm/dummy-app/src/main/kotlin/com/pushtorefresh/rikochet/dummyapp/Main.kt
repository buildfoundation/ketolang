@file:JvmName("Main")

package com.pushtorefresh.rikochet.dummyapp

import java.io.File


const val constValInt = 1

//val nonConstValInt = 2
const val constValString = "const val string"

//val nonConstValString = "non const val string"
//var varString = "var string"
//val valArray = Array<String>(1) { " " }
//var varArray = Array<String>(1) { " " }
val immutableMapOfPrimitives = mapOf<String, Int>()
//val mutableMapOfPrimitives = mutableMapOf<String, Int>()
//val immutableMapOfDynamic = mapOf<String, Array<Int>>()
//val mutableMapOfDynamic = mutableMapOf<String, Array<Int>>()
//typealias a = String

class Y {

    //var varString = "var string"

    companion object K {
    }

    fun returnList(): List<String> {
        return mutableListOf()
    }
}

fun main() {
    println("Hello world!")
    File("test-${System.currentTimeMillis()}").writeText("abc")
    println("Text written to file!")
    println("Goodbye.")
}
