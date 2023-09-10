package com.example.bdfuv.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bdfuv.model.AreaModel
import com.example.bdfuv.model.CheckEditableInterface

class MeasureAdapter(val items: ArrayList<AreaModel>) :
    RecyclerView.Adapter<MeasureAdapter.ViewHolder>() {
    var selectedIndex = 0
    var doneCount = 0

    var onItemClickListener: ((enable: Int) -> Unit)? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = com.example.bdfuv.databinding.MeasureItemBinding.inflate(inflater, parent, false)

        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
//        holder.binding.indicator.isHighlighted = selectedIndex == holder.adapterPosition

        holder.binding.indicator.isDone = position < doneCount
        holder.binding.indicator.isHighlighted = selectedIndex == position
    }


    inner class ViewHolder(val binding: com.example.bdfuv.databinding.MeasureItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CheckEditableInterface) {
            binding.item = item
            binding.executePendingBindings()
        }

        init {
            this.itemView.setOnClickListener {
                if (binding.indicator.isDone || adapterPosition == doneCount) {
                    val oldSelected = selectedIndex
                    val position = adapterPosition
                    selectedIndex = position
                    notifyItemChanged(oldSelected)
                    notifyItemChanged(selectedIndex)
                    onItemClickListener?.invoke(position)
                }
            }
        }
    }

    fun switchIndicator() {
        if (doneCount < items.size) {
            //if selectedIndex in not done before add 1 to doneCount
            if(selectedIndex >= doneCount){
                doneCount++
            }
            selectedIndex = doneCount
        }else if(doneCount == items.size){
            selectedIndex = -1
        }
        //TODO
        notifyDataSetChanged()
    }
}