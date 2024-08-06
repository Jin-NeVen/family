package com.ntt.jin.skywaycomposequickstart

import kotlin.random.Random

fun String.Companion.randomString(length: Int): String {
    val chars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { chars.random() }
        .joinToString("")
}