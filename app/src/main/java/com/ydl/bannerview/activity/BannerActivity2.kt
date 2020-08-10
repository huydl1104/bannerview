package com.ydl.bannerview.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.ydl.bannerlib.adapter.LoopPagerAdapter
import com.ydl.bannerlib.view.CustomBannerView
import com.ydl.bannerview.R
import com.ydl.bannerview.util.ImageBitmapUtils
import kotlinx.android.synthetic.main.activity_second.*

class BannerActivity2 :AppCompatActivity() {
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
        setContentView(R.layout.activity_second)

        banner2.setAdapter(ImageNormalAdapter(banner2))
        banner2.setHintGravity(1)
        banner2.setPlayDelay(2000)
        banner2.setOnBannerClickListener(object : CustomBannerView.OnBannerClickListener {
            override fun onItemClick(position: Int) {}
        })
    }

     inner class ImageNormalAdapter(viewPager: CustomBannerView) : LoopPagerAdapter(viewPager) {
        override fun getView(container: ViewGroup?, position: Int): View {
            val view = ImageView(container?.context)
            view.scaleType = ImageView.ScaleType.CENTER_CROP
            view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            val bitmap = BitmapFactory.decodeResource(resources, imgs[position])
            val bitmap1: Bitmap = ImageBitmapUtils.compressByQuality(bitmap, 50, false)!!
            view.setImageBitmap(bitmap1)
            return view
        }

         override  val realCount: Int get() = imgs.size
    }

}