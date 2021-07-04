package com.example.instagramclone.adapter

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ProgressBar
import com.example.instagramclone.R
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener

class GridAdapter(
    private val cntxt: Context,
    private val layoutResource: Int,
    private val imagesUrl: List<String>
): ArrayAdapter<String>(cntxt, layoutResource, imagesUrl) {

    class ViewHolder {
        var image: ImageView? = null
        var progressBar: ProgressBar? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var viewHolder: ViewHolder?
        var layoutView: View? = null

        if (layoutView == null) {

            viewHolder = ViewHolder()
            val layoutInflater = cntxt.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            layoutView = layoutInflater.inflate(layoutResource, parent, false)

            viewHolder.progressBar = layoutView.findViewById(R.id.progressBarProfileGrid)
            viewHolder.image = layoutView.findViewById(R.id.imageViewProfileGrid)

            layoutView.tag = viewHolder

        }else {

            viewHolder = layoutView?.tag as ViewHolder?

        }

        // get images data
        val imageUrl = getItem(position)

        val imageLoader = ImageLoader.getInstance()
        imageLoader.displayImage(
            imageUrl,
            viewHolder?.image!!,
            object: ImageLoadingListener {
                override fun onLoadingStarted(imageUri: String?, view: View?) {}

                override fun onLoadingFailed(imageUri: String?, view: View?, failReason: FailReason?) {}

                override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap?) {
                    viewHolder.progressBar?.visibility = View.GONE
                }

                override fun onLoadingCancelled(imageUri: String?, view: View?) {
                    viewHolder.progressBar?.visibility = View.GONE
                }

        })

        return layoutView!!
    }

}