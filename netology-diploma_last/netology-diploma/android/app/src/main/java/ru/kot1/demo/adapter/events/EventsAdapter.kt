package ru.kot1.demo.adapter.events

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.graphics.drawable.StateListDrawable
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
import ru.kot1.demo.BuildConfig
import ru.kot1.demo.R
import ru.kot1.demo.adapter.loadX
import ru.kot1.demo.databinding.CardEventBinding
import ru.kot1.demo.databinding.CardPostBinding
import ru.kot1.demo.dto.Event
import ru.kot1.demo.dto.Post
import ru.kot1.demo.enumeration.AttachmentType
import ru.kot1.demo.model.AdModel
import ru.kot1.demo.view.loadCircleCrop
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

interface OnEventsInteractionListener {
    fun onLike(event: Event)
    fun onEdit(event: Event) {}
    fun onRemove(event: Event) {}
    fun onShare(event: Event)
    fun notLogined(event: Event)
    fun participate(event: Event)
    fun onMediaPrepareClick(event: Event)
    fun onMediaReadyClick(event: Event)
    fun onLinkClick(event: Event)
}

fun convertLongToTime(time: Long): String {
    val date = Date(time)
    val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
    return format.format(date)
}


class EventsAdapter(
    private val onInteractionListener: OnEventsInteractionListener,
) : PagingDataAdapter<Event, RecyclerView.ViewHolder>(PostDiffCallback()) {


    override fun getItemViewType(position: Int): Int {
        return R.layout.card_event
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            R.layout.card_event -> {
                val binding =
                    CardEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return EventsViewHolder(binding, onInteractionListener)
            }
            else -> error("no such viewholder")
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is EventsViewHolder -> {
                val item = getItem(position) as Event
                holder.bind(item)
            }

        }
    }
}

data class StateX(val id: Long, val like: Boolean)

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

class EventsViewHolder(
    private val binding: CardEventBinding,
    private val onInteractionListener: OnEventsInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(event: Event) {
        binding.apply {

            author.text = event.author
            published.text = convertLongToTime(event.published)
            content.text = event.content
            avatar.loadX(event.authorAvatar,
                RequestOptions()
                    .transform(CircleCrop())
                    .placeholder(R.drawable.ic_baseline_user_placeholder)
                    .error(R.drawable.ic_baseline_error_placeholder)
                    .timeout(7_000))

            if  (event.link.isNullOrBlank()){
               linkOfLinkD.isGone = true
               linkOfLink.isGone = true
           } else {
               linkOfLinkD.isGone = false
               linkOfLink.isGone = false
                linkOfLink.setText(event.link)
                linkOfLink.setOnClickListener {
                    onInteractionListener.onLinkClick(event)
                }

           }


            format.setText(this.content.resources.getString(R.string.format, event.type))
            hosts.setText(this.content.resources.getString(R.string.organizators,
                event.speakerNames?.joinToString("\n")))


            val x = root.tag

            if (x is ObjectAnimator) {
                x.cancel()
            }

            like.isChecked = event.likedByMe

            if (event.logined) {
                like.setOnClickListener {
                    root.tag = StateX(event.id.toLong(), !like.isChecked)

                    val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1F, 1.25F, 1F)
                    val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1F, 1.25F, 1F)
                    val x = ObjectAnimator.ofPropertyValuesHolder(it, scaleX, scaleY)
                    root.tag = x
                    x.apply {
                        duration = 1_500
                        repeatCount = 1
                        interpolator = BounceInterpolator()
                    }.start()
                    onInteractionListener.onLike(event)
                }
            } else {
                like.setOnClickListener {
                    like.isChecked = !like.isChecked
                    onInteractionListener.notLogined(event)
                }
            }


            share.setOnClickListener {
                onInteractionListener.onShare(event)
            }


            participate.isChecked = event.participatedByMe

            if (event.participatedByMe) {
                participate.setText(R.string.you_going_to)
                participate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check, 0, 0, 0);
            } else {
                participate.icon = null
                participate.setText(R.string.toParticipate)
            }

            if (event.logined) {
                participate.setOnClickListener {
                    onInteractionListener.participate(event)
                }
            } else {
                participate.setOnClickListener {
                    onInteractionListener.notLogined(event)
                }
            }

            //MENU
            menu.visibility = if (event.belongsToMe == true) View.VISIBLE else View.INVISIBLE

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    menu.setGroupVisible(R.id.owned, event.belongsToMe ?: false)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(event)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener.onEdit(event)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }


            //Attach
            if (event.attachment != null) {
                //label text:
                if (event.downloadingProgress == null || event.downloadingProgress == 0.toByte()){
                    mediaAttach.setText(R.string.download)
                } else if (event.downloadingProgress == 100.toByte()) {
                    mediaAttach.setText(R.string.openFile)
                }else{
                    mediaAttach.text = "${event.downloadingProgress}%"
                }


                //setup buttons
                when(AttachmentType.valueOf(event.attachment.type)){
                    AttachmentType.IMAGE -> {
                        postPicture.isVisible = true
                        mediaAttach.isGone = true
                        postPicture.loadX(
                            event.attachment.url,
                            RequestOptions()
                                .optionalFitCenter()
                                .placeholder(R.drawable.ic_baseline_user_placeholder)
                                .error(R.drawable.ic_baseline_error_placeholder)
                                .timeout(7_000)
                        )
                        postPicture.prepareOnClick(event)
                    }

                    AttachmentType.VIDEO -> {
                        mediaAttach.isVisible = true
                        postPicture.isGone = true
                        mediaAttach.setIconResource(R.drawable.ic_video)
                        mediaAttach.prepareOnClick(event)
                    }

                    AttachmentType.AUDIO -> {
                        mediaAttach.isVisible = true
                        postPicture.isGone = true
                        mediaAttach.setIconResource(R.drawable.ic_music)
                        mediaAttach.prepareOnClick(event)
                    }

                }

            } else {
                mediaAttach.isGone = true
                postPicture.isGone = true
            }




        }
    }



    private fun MaterialButton.prepareOnClick(event: Event) {
        setOnClickListener {
            if (event.downloadingProgress == null || event.downloadingProgress == 0.toByte()) {
                setText(R.string.downloading)
                onInteractionListener.onMediaPrepareClick(event)
                setOnClickListener(null)
            } else if (event.downloadingProgress == 100.toByte()) {
                onInteractionListener.onMediaReadyClick(event)
            }
        }
    }

    private fun ImageView.prepareOnClick(event: Event) {
        setOnClickListener {
            if (event.downloadingProgress == null || event.downloadingProgress == 0.toByte()) {
                onInteractionListener.onMediaPrepareClick(event)
                setOnClickListener(null)
            } else if (event.downloadingProgress == 100.toByte()) {
                onInteractionListener.onMediaReadyClick(event)
            }
        }
    }

}


class PostDiffCallback : DiffUtil.ItemCallback<Event>() {
    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
        if (oldItem.javaClass != newItem.javaClass) {
            return false
        }

        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem == newItem
    }
}

