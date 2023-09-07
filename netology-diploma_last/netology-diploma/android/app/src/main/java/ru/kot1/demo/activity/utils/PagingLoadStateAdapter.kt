package ru.kot1.demo.activity.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.kot1.demo.databinding.CardLoadstateBinding

class PagingLoadStateAdapter(private val click : (()-> Unit)) : LoadStateAdapter<PagingLoadStateAdapter.PagingLoadStateAdapterViewHolder>() {

    override fun onBindViewHolder(holder: PagingLoadStateAdapterViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): PagingLoadStateAdapterViewHolder {
        val binding = CardLoadstateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PagingLoadStateAdapterViewHolder(binding, click)
    }

    class PagingLoadStateAdapterViewHolder(private val binding: CardLoadstateBinding, private val click : (()-> Unit))
        : RecyclerView.ViewHolder(binding.root){

    fun bind(state : LoadState){
        with (binding){
            retry.isVisible =  state is LoadState.Error
            errorLabel.isVisible =  state is LoadState.Error
            progressBar.isVisible =  state is LoadState.Loading

            retry.setOnClickListener {
                click()
            }
        }
    }

}
}