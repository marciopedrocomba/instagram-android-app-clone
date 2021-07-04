package com.example.instagramclone.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagramclone.R
import com.example.instagramclone.model.User
import de.hdodenhof.circleimageview.CircleImageView

class SearchAdapter(
    val context: Context,
    private val usersList: List<User>
): RecyclerView.Adapter<SearchAdapter.MyViewHolder>() {

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val image: CircleImageView = view.findViewById(R.id.imageViewSearchUserImage)
        val name: TextView = view.findViewById(R.id.textViewSearchUserName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.search_user_adapter, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val user = usersList[position]

        holder.name.text = user.name

        if (user.photo != null) {
            val uri: Uri = Uri.parse(user.photo)
            Glide.with(context).load(uri).into(holder.image)
        }else {
            holder.image.setImageResource(R.drawable.avatar)
        }

    }

    override fun getItemCount() = usersList.size


}