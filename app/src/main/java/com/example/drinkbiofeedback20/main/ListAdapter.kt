package com.example.drinkbiofeedback20.main


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.drinkbiofeedback20.R
import com.example.drinkbiofeedback20.main.database.DrinkVolume
import kotlinx.android.synthetic.main.history_layout.view.*

class ListAdapter : RecyclerView.Adapter<ListAdapter.MyViewHolder>() {

    private var volumeList = emptyList<DrinkVolume>()

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.history_layout, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
     val currentItem = volumeList[position]
        holder.itemView.id_volume.text = currentItem.volumeId.toString()
        holder.itemView.start_time.text = currentItem.dateString
        holder.itemView.finish_time.text = currentItem.timeString
        holder.itemView.volume.text = currentItem.liquidVolume.toString()
    }
    override fun getItemCount(): Int {
      return volumeList.size
    }

    fun setData(volume: List<DrinkVolume>){
        this.volumeList = volume
        notifyDataSetChanged()
    }
}