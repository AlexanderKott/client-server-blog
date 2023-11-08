package ru.kot1.demo.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okio.*
import ru.kot1.demo.R
import ru.kot1.demo.view.load
import java.io.IOException
import java.io.InputStream


@GlideModule
class GlideMod: AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setLogLevel(Log.ERROR)
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        super.registerComponents(context, glide, registry)
        val client = OkHttpClient.Builder()
            .addNetworkInterceptor { chain ->
                val request = chain.request()
                val response = chain.proceed(request)
                val listener = DispatchingProgressManager()
                response.newBuilder()
                    .body(OkHttpProgressResponseBody(request.url, response.body!!, listener))
                    .build()
            }
            .build()
        glide.registry.replace(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(client)) //6
    }
}


interface ResponseProgressListener {
    fun update(url: HttpUrl, bytesRead: Long, contentLength: Long)
}

interface UIonProgressListener {
    val granularityPercentage: Float
    fun onProgress(bytesRead: Long, expectedLength: Long)
}

class DispatchingProgressManager internal constructor() : ResponseProgressListener {

    companion object {
        private val PROGRESSES = HashMap<String?, Long>()
        private val LISTENERS = HashMap<String?, UIonProgressListener>()

        internal fun expect(url: String?, listener: UIonProgressListener) {
            LISTENERS[url] = listener
        }

        internal fun forget(url: String?) {
            LISTENERS.remove(url)
            PROGRESSES.remove(url)
        }
    }

    private val handler: Handler = Handler(Looper.getMainLooper())

    override fun update(url: HttpUrl, bytesRead: Long, contentLength: Long) {
        val key = url.toString()
        val listener = LISTENERS[key] ?: return
        if (contentLength <= bytesRead) {
            forget(key)
        }
        if (needsDispatch(key, bytesRead, contentLength,
                listener.granularityPercentage)) {
            handler.post { listener.onProgress(bytesRead, contentLength) }
        }
    }

    private fun needsDispatch(key: String, current: Long, total: Long, granularity: Float): Boolean {
        if (granularity == 0f || current == 0L || total == current) {
            return true
        }
        val percent = 100f * current / total
        val currentProgress = (percent / granularity).toLong()
        val lastProgress = PROGRESSES[key]
        return if (lastProgress == null || currentProgress != lastProgress) { //9
            PROGRESSES[key] = currentProgress
            true
        } else {
            false
        }
    }
}

class OkHttpProgressResponseBody internal constructor(
    private val url: HttpUrl,
    private val responseBody: ResponseBody,
    private val progressListener: ResponseProgressListener) : ResponseBody() { //1

    private var bufferedSource: BufferedSource? = null

    override fun contentType(): MediaType {
        return responseBody.contentType()!!
    }

    override fun contentLength(): Long {
        return responseBody.contentLength()
    }

    override fun source(): BufferedSource {
        if (bufferedSource == null) {
            bufferedSource = source(responseBody.source()).buffer()
        }
        return this.bufferedSource!!
    }

    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            var totalBytesRead = 0L

            @Throws(IOException::class)
            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                val fullLength = responseBody.contentLength()
                if (bytesRead.toInt() == -1) {
                    totalBytesRead = fullLength
                } else {
                    totalBytesRead += bytesRead
                }
                progressListener.update(url, totalBytesRead, fullLength)
                return bytesRead
            }
        }
    }
}




        fun ImageView.loadX(url: String?, options: RequestOptions?, mProgressBar: ProgressBar? = null) {
            if (options == null) return

            mProgressBar?.visibility = View.VISIBLE

            DispatchingProgressManager.expect(url, object : UIonProgressListener {

                override val granularityPercentage: Float
                    get() = 1.0f

                override fun onProgress(bytesRead: Long, expectedLength: Long) {
                    if (mProgressBar != null) {
                        mProgressBar?.progress = (100 * bytesRead / expectedLength).toInt()
                    }
                }
            })

            GlideApp.with(this.context)
                .load(url)
                .apply(options)

                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?, model: Any?, target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        DispatchingProgressManager.forget(url)
                        mProgressBar?.visibility = View.VISIBLE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: com.bumptech.glide.load.DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        DispatchingProgressManager.forget(url)
                        mProgressBar?.visibility = View.GONE
                        this@loadX.visibility = View.VISIBLE
                        return false
                    }


                })
                .into(this)
        }



