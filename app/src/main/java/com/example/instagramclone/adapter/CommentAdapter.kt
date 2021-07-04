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
import com.example.instagramclone.model.Comment
import de.hdodenhof.circleimageview.CircleImageView

class CommentAdapter(
    val context: Context,
    private val commentsList: List<Comment>
): RecyclerView.Adapter<CommentAdapter.MyViewHolder>() {

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val photo: CircleImageView = view.findViewById(R.id.imageViewCommentUserPhoto)
        val username: TextView = view.findViewById(R.id.textViewCommentUsername)
        val comment: TextView = view.findViewById(R.id.textViewCommentBody)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.comment_adapter, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val comment = commentsList[position]

        // set comment owner image
        val userImageUrl = Uri.parse(comment.userPhoto)
        if (userImageUrl == null) {
            holder.photo.setImageResource(R.drawable.avatar)
        }else {
            Glide.with(context).load(userImageUrl).into(holder.photo)
        }

        holder.username.text = comment.username
        holder.comment.text = comment.comment

    }

    override fun getItemCount() = commentsList.size

}