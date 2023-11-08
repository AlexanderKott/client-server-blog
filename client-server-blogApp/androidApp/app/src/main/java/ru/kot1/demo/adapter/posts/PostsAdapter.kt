package ru.kot1.demo.adapter.posts

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButton
import ru.kot1.demo.R
import ru.kot1.demo.adapter.loadX
import ru.kot1.demo.databinding.CardPostBinding
import ru.kot1.demo.dto.Post
import ru.kot1.demo.enumeration.AttachmentType
import ru.kot1.demo.model.FeedModel
import ru.kot1.demo.model.PostModel
import java.text.ParseException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

interface OnInteractionListener {
    fun onLike(post: Post)
    fun onEdit(post: Post, position: Int) {}
    fun onRemove(post: Post) {}
    fun onShare(post: Post)
    fun onMediaPrepareClick(post: Post)
    fun onMediaReadyClick(post: Post)
    fun onPlaceClick(post: Post)
    fun onNotLogined(post: Post)
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener,
) : PagingDataAdapter<FeedModel, RecyclerView.ViewHolder>(PostDiffCallback()) {


    private fun updateItem(position: Int) {
        val post = (snapshot().items[position] as PostModel)
        post.post.isLoading = true
        notifyItemChanged(position)
    }

    fun disablePost(position: Int) {
        val handler = android.os.Handler(Looper.getMainLooper())

        val updateTask: Runnable = object : Runnable {
            override fun run() {
                if (snapshot().items.isEmpty()) {
                    handler.postDelayed(
                        this, 3000
                    )
                    return@run
                }
                updateItem(position)
            }
        }

        if (snapshot().items.isEmpty()) {
            handler.postDelayed(updateTask, 3000)
        } else {
            updateItem(position)
        }

    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is PostModel -> R.layout.card_post
            else -> error("unsupported type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            R.layout.card_post -> {
                val binding =
                    CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return PostViewHolder(binding, onInteractionListener)
            }
            else -> error("no such viewholder")
        }


    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PostViewHolder -> {
                val item = getItem(position) as PostModel
                holder.bind(item.post)
            }
        }
    }
}

data class PostState(val id: Long, val like: Boolean)

fun convertLongToTime(time: String?): String {
    return try {
        val ax = Instant.parse(time)
        val ldt: LocalDateTime = LocalDateTime.ofInstant(ax, ZoneId.systemDefault())
        "${ldt.dayOfMonth} ${ldt.month} ${ldt.year}, ${ldt.hour}:${ldt.minute}:${ldt.second}"
    } catch (e: ParseException) {
        e.printStackTrace()
        "unknown date :("
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {


    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = convertLongToTime(post.published)
             avatar.loadX(
                post.authorAvatar,
                RequestOptions()
                    .transform(CircleCrop())
                    .placeholder(R.drawable.ic_baseline_user_placeholder)
                    .error(R.drawable.ic_baseline_error_placeholder)
                    .timeout(7_000)
            )

            //remove like animation
            val tag = root.tag
            if (tag is ObjectAnimator) {
                tag.cancel()
                if (post.likedByMe) {
                     tag.start()
                }
            }

            if (post.isLoading) {
                progressWall.isVisible = true
                menu.isGone = true
                content.isGone = true
                like.isGone = true
                share.isGone = true
                openPlace.isGone = true
                mediaAttach.isGone = true
                postPicture.isGone = true
                return
            }

            content.text = post.content

            like.isChecked = post.likedByMe

           if (post.logined) {
               like.setOnClickListener {
                   root.tag = PostState(post.id, !like.isChecked)

                   val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1F, 1F, 1F)
                   val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1F, 1.23F, 1F)
                   val objAnim = ObjectAnimator.ofPropertyValuesHolder(it, scaleX, scaleY)
                   root.tag = objAnim
                   objAnim.apply {
                       duration = 2_000
                       repeatCount = 1
                       interpolator = BounceInterpolator()
                   }
                       .start()
                   onInteractionListener.onLike(post)
               }
           } else {
               like.setOnClickListener {
                   like.isChecked = !like.isChecked
                   onInteractionListener.onNotLogined(post)
               }
           }



            //MENU
            menu.visibility = if (post.ownedByMe) View.VISIBLE else View.INVISIBLE

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    menu.setGroupVisible(R.id.owned, post.ownedByMe)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener.onEdit(post, absoluteAdapterPosition)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            //SHARE
            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }





            //Attach
           if (post.attachment != null) {


               //label text:
               if (post.downloadingProgress == null || post.downloadingProgress == 0.toByte()){
                   mediaAttach.setText(R.string.download)
               } else if (post.downloadingProgress == 100.toByte()) {
                   mediaAttach.setText(R.string.openFile)
               }else{
                   mediaAttach.text = "${post.downloadingProgress}%"
               }


               //setup buttons
                when(AttachmentType.valueOf(post.attachment.type)){
                    AttachmentType.IMAGE -> {
                        postPicture.isVisible = true
                        mediaAttach.isGone = true
                        postPicture.loadX(
                            post.attachment.url,
                                RequestOptions()
                                    .optionalFitCenter()
                                    .placeholder(R.drawable.ic_baseline_user_placeholder)
                                    .error(R.drawable.ic_baseline_error_placeholder)
                                    .timeout(7_000)
                            )
                        postPicture.prepareOnClick(post)
                    }

                    AttachmentType.VIDEO -> {
                        mediaAttach.isVisible = true
                        postPicture.isGone = true
                        mediaAttach.setIconResource(R.drawable.ic_video)
                        mediaAttach.prepareOnClick(post)
                    }

                    AttachmentType.AUDIO -> {
                        mediaAttach.isVisible = true
                        postPicture.isGone = true
                        mediaAttach.setIconResource(R.drawable.ic_music)
                        mediaAttach.prepareOnClick(post)
                    }

                }



            } else {
               mediaAttach.isGone = true
               postPicture.isGone = true
            }



            //Coords
          if (post.coords != null){
                openPlace.isVisible = true
                openPlace.setOnClickListener {
                        onInteractionListener.onPlaceClick(post)
                }
            } else {
                openPlace.isGone = true
            }



        }
    }

    private fun MaterialButton.prepareOnClick(post: Post) {
         setOnClickListener {
            if (post.downloadingProgress == null || post.downloadingProgress == 0.toByte()) {
                 setText(R.string.downloading)
                onInteractionListener.onMediaPrepareClick(post)
                 setOnClickListener(null)
            } else if (post.downloadingProgress == 100.toByte()) {
                onInteractionListener.onMediaReadyClick(post)
            }
        }
    }

    private fun ImageView.prepareOnClick(post: Post) {
        setOnClickListener {
            if (post.downloadingProgress == null || post.downloadingProgress == 0.toByte()) {
                onInteractionListener.onMediaPrepareClick(post)
                setOnClickListener(null)
            } else if (post.downloadingProgress == 100.toByte()) {
                onInteractionListener.onMediaReadyClick(post)
            }
        }
    }


}


class PostDiffCallback : DiffUtil.ItemCallback<FeedModel>() {
    override fun areItemsTheSame(oldItem: FeedModel, newItem: FeedModel): Boolean {
        if (oldItem.javaClass != newItem.javaClass) {
            return false
        }

        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedModel, newItem: FeedModel): Boolean {
        return oldItem == newItem
    }

}

