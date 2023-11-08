package ru.kot1.demo.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ru.kot1.demo.error.UnknownError
import ru.kot1.demo.repository.AppEntities


@HiltWorker
class SaveJobWorker  @AssistedInject constructor(
    @Assisted  applicationContext: Context,
    @Assisted  params: WorkerParameters,
    var repository: AppEntities
) : CoroutineWorker(applicationContext, params) {

    companion object {
        const val postKey = "job"
    }

    override suspend fun doWork(): Result {
        val task = inputData.getStringArray(postKey)
            ?: return Result.failure()

        return try {
            repository.processJobWork(task)
            Result.success()

        } catch (e: Exception) {
            Result.retry()
        } catch (e: UnknownError) {
            Result.failure()
        }

    }
}
