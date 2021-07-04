package com.example.instagramclone.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.R
import com.zomato.photofilters.utils.ThumbnailItem

class FilterAdapter(
    val context: Context,
    private val filtersList: List<ThumbnailItem>
): RecyclerView.Adapter<FilterAdapter.MyViewHolder>(){

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.imageViewFilter)
        val name: TextView = view.findViewById(R.id.textViewFilterName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.filters_adapter, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = filtersList[position]
        holder.name.text = item.filterName
        holder.image.setImageBitmap(item.image)
    }

    override fun getItemCount() = filtersList.size
}