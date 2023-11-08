package ru.kot1.demo.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.kot1.demo.activity.utils.prepareFileName
import ru.kot1.demo.auth.AppAuth
import ru.kot1.demo.dto.Post
import ru.kot1.demo.model.FeedModel
import ru.kot1.demo.model.FeedModelState
import ru.kot1.demo.model.PostModel
import ru.kot1.demo.model.SingleLiveEvent
import ru.kot1.demo.repository.AppEntities
import java.io.File
import javax.inject.Inject


interface CallbackR<T> {
    fun onSuccess(answer: T) {}
    fun onError(e: Exception) {}
}


@HiltViewModel
@ExperimentalCoroutinesApi
class MediaWorkPostViewModel @Inject constructor(
    var repository: AppEntities,
    var workManager: WorkManager,
    var auth: AppAuth,
    var context: Application
) : AndroidViewModel(context) {

    private val files = HashMap<Long, File>()

    fun downloadMedia(post: Post) {
        if (post.attachment == null) {
            return
        }

        val outputFile = File(context.cacheDir, post.attachment.prepareFileName())
        outputFile.setReadable(true, false)

        files[post.id] = outputFile

        if (outputFile.exists()) {
            viewModelScope.launch {
                repository.updatePost(post.copy(downloadingProgress = 100))
            }
            return
        }


        repository.download(post.attachment.url, outputFile,
            object : CallbackR<Byte> {
                override fun onSuccess(progress: Byte) {
                    viewModelScope.launch {
                        repository.updatePost(post.copy(downloadingProgress = progress))
                    }
                }

                override fun onError(e: Exception) {
                    Log.e("exc", "error")
                }
            }
        )

    }

    fun openMedia(id: Long, play: (file: File) -> Unit) {
        files[id]?.let { play(it) }
    }


    fun deleteFile(post: Post){
        files.remove(post.id)
        if (post.attachment == null)
            return

        val outputFile = File(context.cacheDir, post.attachment.prepareFileName())
        outputFile.setReadable(true, false)

        if (outputFile.exists()) {
             outputFile.delete()
        }

    }

}




