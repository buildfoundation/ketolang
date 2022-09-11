package com.pushtorefresh.ketolang.sample

// fib(n) = fib(n - 1) + fib(n - 2)
fun naiveFibonacci(n: Int): Int {
    if (n == 0 || n == 1) {
        return n
    } else {
        return naiveFibonacci(n - 1) + naiveFibonacci(n - 2)
    }
}
