package ru.kot1.demo.dto

import java.io.File

data class Media(val url: String)
data class MediaUpload(val file: File)
data class JobUI(val name : String,val pos : String,val start : Long, val fin : Long, )
data class EventUI(val name : String,val pos : String,val start : Long, val fin : Long, )


