package com.ydl.bannerview.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ydl.bannerlib.adapter.StaticPagerAdapter
import com.ydl.bannerlib.hintview.TextHintView
import com.ydl.bannerlib.view.CustomBannerView
import com.ydl.bannerview.R
import com.ydl.bannerview.util.ImageBitmapUtils
import kotlinx.android.synthetic.main.activity_four.*

class BannerActivity4 :AppCompatActivity() {

    private val imgs = intArrayOf(
        R.drawable.icon111,
        R.drawable.icon222,
        R.drawable.icon333,
        R.drawable.icon444,
        R.drawable.icon555,
        R.drawable.icon6666
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_four)
        banner4.setHintView(TextHintView(this))
        banner4.setAdapter(ImageNormalAdapter())
        banner4.setOnBannerClickListener(object : CustomBannerView.OnBannerClickListener {
            override fun onItemClick(position: Int) {
                Toast.makeText(this@BannerActivity4, position.toString() + "被点击呢", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    override fun onPause() {
        super.onPause()
        if (banner4 != null) {
            banner4.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (banner4 != null) {
            banner4.resume()
        }
    }


    private inner class ImageNormalAdapter : StaticPagerAdapter() {
        override fun getView(container: ViewGroup?, position: Int): View {
            val view = ImageView(container?.context)
            view.scaleType = ImageView.ScaleType.CENTER_CROP
            view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            val bitmap = BitmapFactory.decodeResource(getResources(), imgs.get(position))
            val bitmap1: Bitmap? = ImageBitmapUtils.compressByQuality(bitmap!!, 50, false)
            view.setImageBitmap(bitmap1!!)
            return view
        }

        override fun getCount() :Int = imgs.size
    }

}