package com.sun.sunclient.config

object Config {
    object University {
        // complete name of university
        val name = "Atharva College of Engineering"
        // short name or abbreviation to be used in UI
        val nickname = "ACE"
    }

    // currently needs to set ip address of your device to access local running backend
    // needs to replace it with domain for backend hosted in cloud
    const val API_BASE_URL = "http://192.168.43.245:5001/api/"
}