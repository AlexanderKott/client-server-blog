package ru.kot1.demo.view

import android.view.View
import android.widget.ImageView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import ru.kot1.demo.R
import android.graphics.drawable.Drawable
import com.bumptech.glide.load.Option

import com.bumptech.glide.load.engine.GlideException

import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import java.security.cert.PKIXRevocationChecker


fun ImageView.load(url: String, vararg transforms: BitmapTransformation = emptyArray()) =

    Glide.with(this)
        .load(url)
        .placeholder(R.drawable.ic_baseline_user_placeholder)
        .error(Glide.with(this).load(R.drawable.ic_baseline_error_placeholder))
       // .error(R.drawable.ic_baseline_error_placeholder)
        .timeout(10_000)
        .transform(*transforms)
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: com.bumptech.glide.request.target.Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: com.bumptech.glide.request.target.Target<Drawable>?,
                dataSource: com.bumptech.glide.load.DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }

        })
        .into(this)


fun ImageView.loadCircleCrop(url: String, vararg transforms: BitmapTransformation = emptyArray()) =
    load(url, CircleCrop(), *transforms)



private const val MIN_SCALE = 0.95f
private const val MIN_ALPHA = 0.5f

class ZoomOutPageTransformer : ViewPager2.PageTransformer {

    override fun transformPage(view: View, position: Float) {
        view.apply {


            val pageWidth = width
            val pageHeight = height
            when {
                position < -1 -> { // [-Infinity,-1)
                    // This page is way off-screen to the left.
                    alpha = 0f
                }
                position <= 1 -> { // [-1,1]
                    // Modify the default slide transition to shrink the page as well
                    val scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position))
                    val vertMargin = pageHeight * (1 - scaleFactor) / 2
                    val horzMargin = pageWidth * (1 - scaleFactor) / 2
                    translationX = if (position < 0) {
                        horzMargin - vertMargin / 2
                    } else {
                        horzMargin + vertMargin / 2
                    }

                    // Scale the page down (between MIN_SCALE and 1)
                    scaleX = scaleFactor
                    scaleY = scaleFactor

                    // Fade the page relative to its size.
                    alpha = (MIN_ALPHA +
                            (((scaleFactor - MIN_SCALE) / (1 - MIN_SCALE)) * (1 - MIN_ALPHA)))
                }
                else -> { // (1,+Infinity]
                    // This page is way off-screen to the right.
                    alpha = 0f
                }
            }
        }
    }
}
