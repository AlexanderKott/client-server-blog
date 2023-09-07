package ru.kot1.demo.activity.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import ru.kot1.demo.dto.Attachment
import java.io.File


fun Attachment.prepareFileName() =   url.subSequence(
     url.lastIndexOf("/") + 1,  url.length).toString()


fun Fragment.prepareIntent(file : File): Intent{
     val intent = Intent(Intent.ACTION_VIEW)
     val contentUri = FileProvider.getUriForFile(this.requireContext(), "${this.context?.packageName}.fileprovider", file)
     val mimeType = this.context?.contentResolver?.getType(contentUri)
     intent.setDataAndType(contentUri, mimeType)
     intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
     return intent
}