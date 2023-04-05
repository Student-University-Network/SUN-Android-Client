package com.sun.sunclient.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun <T> stringify(obj: T): String {
    val gson = Gson()
    return gson.toJson(obj)
}

fun <T> parseJson(json: String, typeToken: TypeToken<T>): T? {
    val gson = Gson()
    return gson.fromJson(json, typeToken)
}