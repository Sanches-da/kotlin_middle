package ru.skillbranch.kotlinexample.extensions

fun <T> List<T>.dropLastUntil(predicate: (T) -> Boolean): List<T>{
    return this.dropLastWhile { !predicate(it) }.dropLastWhile(predicate)
}