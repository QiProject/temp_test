package com.example.bdfuv.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bdfuv.databinding.CheckboxEditTextItemBinding
import com.example.bdfuv.model.CheckEditableInterface

class AppConfigAdapter(val items: ArrayList<CheckEditableInterface>) :
    RecyclerView.Adapter<AppConfigAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CheckboxEditTextItemBinding.inflate(inflater, parent, false)

        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ViewHolder(val binding: CheckboxEditTextItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CheckEditableInterface) {
            binding.item = item
            binding.executePendingBindings()
        }
    }
}