package ru.kot1.demo.dto

import com.google.gson.annotations.SerializedName

data class Coords(
    @SerializedName("lat")
    val latitude: Float?,
    @SerializedName("long")
    val longitude : Float?
    )