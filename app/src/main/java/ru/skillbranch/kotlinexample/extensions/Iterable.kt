package ru.skillbranch.kotlinexample.extensions

fun <T> List<T>.dropLastUntil(predicate: (T) -> Boolean): ArrayList<T>{
    val res = ArrayList<T>()
    res.addAll(this.dropLastWhile { !predicate(it) }.dropLastWhile(predicate))
    return res
}