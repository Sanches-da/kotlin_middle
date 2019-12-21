package ru.skillbranch.kotlinexample.extensions

fun <T, P:Iterable<T>> P.dropLastUntil(predicate: (T) -> Boolean): ArrayList<T>{
    val res = arrayListOf<T>()
    res.addAll(this.reversed().dropWhile{!predicate(it)}.dropWhile(predicate).reversed())
    return res
}