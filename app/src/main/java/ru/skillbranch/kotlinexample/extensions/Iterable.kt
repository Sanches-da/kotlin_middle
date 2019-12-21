package ru.skillbranch.kotlinexample.extensions

fun <T> Iterable<T>.dropLastUntil(predicate: (T) -> Boolean): Iterable<T>{
    val res = ArrayList<T>()
    res.addAll(this.reversed().dropWhile{!predicate(it)}.dropWhile(predicate).reversed())
    return res.asIterable()
}