package com.ydl.bannerview.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.ydl.bannerview.R
import java.util.*

class Snap3Adapter internal constructor(private val mContext: Context) :
    RecyclerView.Adapter<Snap3Adapter.MyViewHolder>() {
    private var urlList: MutableList<Int>? = ArrayList()
    fun setData(list: MutableList<Int>?) {
        urlList!!.clear()
        urlList = list
    }

    val data: List<Int>?
        get() = urlList

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val view: View =
            LayoutInflater.from(mContext).inflate(R.layout.item_snap2, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int
    ) {
        if (urlList == null || urlList!!.isEmpty()) return
        val url = urlList!![position % urlList!!.size]
        //        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), url);
//        Bitmap bitmap1 = ImageBitmapUtils.compressByQuality(bitmap, 60,false);
//        holder.imageView.setImageBitmap(bitmap1);
        holder.imageView.setBackgroundResource(url)
    }

    inner class MyViewHolder(itemView: View) :
        ViewHolder(itemView) {
        var imageView: ImageView

        init {
            imageView = itemView.findViewById(R.id.iv_image)
        }
    }

    override fun getItemCount(): Int {
        return if (urlList!!.size != 1) {
            Log.e("getItemCount", "getItemCount---------")
            Int.MAX_VALUE // 无限轮播
        } else {
            Log.e("getItemCount", "getItemCount++++----")
            urlList!!.size
        }
    }

}