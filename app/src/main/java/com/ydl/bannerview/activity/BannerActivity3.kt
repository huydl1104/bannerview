package com.ydl.banner3view.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ydl.bannerlib.adapter.LoopPagerAdapter
import com.ydl.bannerlib.view.CustomBannerView
import com.ydl.bannerview.R
import com.ydl.bannerview.util.ImageBitmapUtils
import kotlinx.android.synthetic.main.activity_third.*

class BannerActivity3 :AppCompatActivity() {

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
        setContentView(R.layout.activity_third)

        banner3.setHintColor(Color.GRAY)
        banner3.setHintGravity(Gravity.RIGHT)
        banner3.setAnimationDuration(1000)
        banner3.setPlayDelay(2000)
        banner3.setAdapter(ImageNormalAdapter(banner3))
        banner3.setOnBannerClickListener(object : CustomBannerView.OnBannerClickListener {
            override fun onItemClick(position: Int) {
                Toast.makeText(this@BannerActivity3, position.toString() + "被点击呢", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private  inner class ImageNormalAdapter
        internal constructor(viewPager: CustomBannerView) : LoopPagerAdapter(viewPager) {
        override fun getView(container: ViewGroup?, position: Int): View {
            val view = ImageView(container?.context)
            view.scaleType = ImageView.ScaleType.CENTER_CROP
            view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            val bitmap = BitmapFactory.decodeResource(resources, imgs[position])
            val bitmap1: Bitmap = ImageBitmapUtils.compressByQuality1(bitmap, 1024, false)!!
            view.setImageBitmap(bitmap1)
            return view
        }

        override val realCount: Int get() = imgs.size
    }

}