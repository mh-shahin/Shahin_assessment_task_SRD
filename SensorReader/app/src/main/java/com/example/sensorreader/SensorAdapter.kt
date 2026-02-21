package com.example.sensorreader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SensorAdapter(
    private val sensors: List<SensorInfo>,
    private val onSensorClick: (SensorInfo) -> Unit
) : RecyclerView.Adapter<SensorAdapter.SensorViewHolder>() {

    inner class SensorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvSensorName)
        val tvType: TextView = itemView.findViewById(R.id.tvSensorType)
        val tvVendor: TextView = itemView.findViewById(R.id.tvSensorVendor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sensor, parent, false)
        return SensorViewHolder(view)
    }

    override fun onBindViewHolder(holder: SensorViewHolder, position: Int) {
        val info = sensors[position]
        holder.tvName.text = info.name
        holder.tvType.text = info.typeName
        holder.tvVendor.text = holder.itemView.context.getString(R.string.vendor_format, info.vendor)
        holder.itemView.setOnClickListener { onSensorClick(info) }
    }

    override fun getItemCount() = sensors.size
}
