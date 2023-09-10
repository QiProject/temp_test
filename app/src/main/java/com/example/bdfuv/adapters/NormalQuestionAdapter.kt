package com.example.bdfuv.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bdfuv.databinding.CheckboxEditTextItemBinding
import com.example.bdfuv.databinding.QuestionItemBinding
import com.example.bdfuv.model.AreaModel
import com.example.bdfuv.model.CheckEditableInterface
import com.example.bdfuv.model.QuestionModel

class NormalQuestionAdapter(val items: ArrayList<QuestionModel>) :
    RecyclerView.Adapter<NormalQuestionAdapter.ViewHolder>() {
    var onItemClickListener: ((question: QuestionModel) -> Unit)? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = QuestionItemBinding.inflate(inflater, parent, false)

        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ViewHolder(val binding: QuestionItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CheckEditableInterface) {
            binding.item = item
            binding.executePendingBindings()
        }

        init {
            this.itemView.setOnClickListener {

                val value = !items[adapterPosition].isChecked
                items[adapterPosition].isChecked = value
                binding.indicator.isDone = value

                onItemClickListener?.invoke(items[adapterPosition])
            }
        }
    }
}