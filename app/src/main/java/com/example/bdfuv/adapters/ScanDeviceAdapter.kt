package com.example.bdfuv.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bdfuv.databinding.ScanDeviceItemBinding
import com.example.bdfuv.manager.BLEManager

class ScanDeviceAdapter(private val items: ArrayList<BLEManager.DeviceWithRssi>) :
    RecyclerView.Adapter<ScanDeviceAdapter.ViewHolder>() {

    var onItemClick: ((BLEManager.DeviceWithRssi) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ScanDeviceItemBinding.inflate(inflater, parent, false)

        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ViewHolder(val binding: com.example.bdfuv.databinding.ScanDeviceItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BLEManager.DeviceWithRssi) {
            binding.item = item
            binding.executePendingBindings()
        }

        init {
            this.itemView.setOnClickListener {
                onItemClick?.invoke(items[adapterPosition])
            }
        }
    }
}