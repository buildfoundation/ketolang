package com.pushtorefresh.ketolang.sample

import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@OptIn(ExperimentalTime::class)
fun main(args: Array<String>) {
    val n = args.single().toInt()
    val result: Int
    val time = measureTime {
        result = naiveFibonacci(n)
    }

    println("fibonacci(n=$n) == $result, took $time")
}
