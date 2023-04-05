package com.sun.sunclient.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun <T> stringify(obj: T) : String {
    val gson = Gson()
    val json = gson.toJson(obj)
    return json
}

fun <T> parseJson(json : String, typeToken: TypeToken<T>) : T {
    val gson = Gson()
    val obj = gson.fromJson(json, typeToken)
    return obj
}