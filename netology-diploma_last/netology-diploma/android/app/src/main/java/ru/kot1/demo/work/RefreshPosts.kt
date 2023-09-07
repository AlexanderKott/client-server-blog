package ru.kot1.demo.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.kot1.demo.repository.AppEntities

@HiltWorker
class RefreshPostsWorker @AssistedInject constructor(
    @Assisted  applicationContext: Context,
    @Assisted  params: WorkerParameters,
    var repository:  AppEntities
    ) : CoroutineWorker(applicationContext, params) {
    companion object {
        const val name = "RefreshPostsWorker"
    }


    override suspend fun doWork(): Result = withContext(Dispatchers.Default) {
        try {
            repository.getAllPosts()
            repository.getAllUsers()
            repository.getAllEvents()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}