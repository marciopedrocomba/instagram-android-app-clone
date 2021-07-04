package com.example.instagramclone.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagramclone.R
import com.example.instagramclone.activity.CommentActivity
import com.example.instagramclone.helper.FirebaseConfig
import com.example.instagramclone.helper.FirebaseUser
import com.example.instagramclone.model.Feed
import com.example.instagramclone.model.LikedFeed
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.like.LikeButton
import com.like.OnLikeListener
import de.hdodenhof.circleimageview.CircleImageView

class FeedAdapter(
    val context: Context,
    private val feedsList: List<Feed>
): RecyclerView.Adapter<FeedAdapter.MyViewHolder>() {

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val circularImageView: CircleImageView = view.findViewById(R.id.imageViewPostFeedImage)
        val imageView: ImageView = view.findViewById(R.id.imageViewPostFeedMainImage)
        val textViewPostProfileName: TextView = view.findViewById(R.id.textViewPostFeedName)
        val textViewPostProfileLikes: TextView = view.findViewById(R.id.textViewPostFeedLikes)
        val textViewPostProfileDescription: TextView = view.findViewById(R.id.textViewPostFeedDescription)
        val likeButton: LikeButton = view.findViewById(R.id.feed_like_button)
        val commentImageView: ImageView = view.findViewById(R.id.imageViewFeedCommentImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.feed_adapter, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val feed = feedsList[position]
        val loggedUser = FirebaseUser.getLoggedUserData()

        // load feeds data
        val uriUserPhoto = Uri.parse(feed.userPhoto)
        val uriPhoto = Uri.parse(feed.photo)

        if (uriUserPhoto == null) {
            holder.circularImageView.setImageResource(R.drawable.avatar)
        }else {
            Glide.with(context).load(uriUserPhoto).into(holder.circularImageView)
        }

        Glide.with(context).load(uriPhoto).into(holder.imageView)

        holder.textViewPostProfileName.text = feed.username
        holder.textViewPostProfileDescription.text = feed.description

        // add comments click event
        holder.commentImageView.setOnClickListener {
            val intent = Intent(context, CommentActivity::class.java)
            intent.putExtra(CommentActivity.ID, feed.id)
            context.startActivity(intent)
        }

        val likesRef = FirebaseConfig.database
            .child("liked-posts")
            .child(feed.id.toString())
        likesRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var likes = 0
                if (snapshot.hasChild("likes")) {
                    val likedFeed = snapshot.getValue(LikedFeed::class.java)
                    likes = likedFeed?.likes!!
                }

                val liked = LikedFeed()
                liked.feed = feed
                liked.user = loggedUser
                liked.likes = likes

                // verify if the post was already liked
                holder.likeButton.isLiked = snapshot.hasChild(loggedUser.id.toString())

                // add event to like a post
                holder.likeButton.setOnLikeListener(object: OnLikeListener {
                    override fun liked(likeButton: LikeButton?) {
                        liked.save()
                        holder.textViewPostProfileLikes.text = "${liked.likes} Likes"
                    }

                    override fun unLiked(likeButton: LikeButton?) {
                        liked.removeLike()
                        holder.textViewPostProfileLikes.text = "${liked.likes} Likes"
                    }

                })

                holder.textViewPostProfileLikes.text = "${liked.likes} Likes"

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    override fun getItemCount() = feedsList.size

}