package ru.kot1.demo.model

import android.net.Uri
import ru.kot1.demo.enumeration.AttachmentType

data class PreparedData(val uri: Uri? = null, val dataType : AttachmentType? = null)