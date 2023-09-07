package ru.kot1.demo.adapter.users

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import ru.kot1.demo.BuildConfig
import ru.kot1.demo.R
import ru.kot1.demo.adapter.loadX
import ru.kot1.demo.databinding.CardUserBinding
import ru.kot1.demo.dto.User
import ru.kot1.demo.view.loadCircleCrop

interface OnUsersInteractionListener {
    fun onWall(post: User) {}
    fun onJobs(post: User) {}
    fun onEvents(post: User) {}
}

class UsersAdapter(
    private val onUsersInteractionListener: OnUsersInteractionListener,
) : PagingDataAdapter<User, RecyclerView.ViewHolder>(PostDiffCallback()) {


    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is User -> R.layout.card_user
            else -> error("unsupported type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            R.layout.card_user -> {
                val binding =
                    CardUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return UserViewHolder(binding, onUsersInteractionListener)
            }


            else -> error("no such viewholder")
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is UserViewHolder -> {
                val item = getItem(position)
                if (item != null) {
                    holder.bind(item)
                } else {
                    Log.e("exc", "users onBindViewHolder error")
                }
            }

        }
    }
}


class UserViewHolder(
    private val binding: CardUserBinding,
    private val onInteractionListener: OnUsersInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(user: User) {
        binding.apply {
            userInfo.text = user.name
            userid.text = "#${user.id}"
            avatar.loadX(
                user.avatar,
                RequestOptions()
                    .transform(CircleCrop())
                    .placeholder(R.drawable.ic_baseline_user_placeholder)
                    .error(R.drawable.ic_baseline_error_placeholder)
                    .timeout(7_000)
            )


            toEvents.setOnClickListener {
                onInteractionListener.onEvents(user)
            }

            toWall.setOnClickListener {
                onInteractionListener.onWall(user)
            }

            toJobs.setOnClickListener {
                onInteractionListener.onJobs(user)
            }
        }
    }
}

    class PostDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }

