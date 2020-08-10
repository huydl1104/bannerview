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
import com.ydl.bannerlib.hintview.IconHintView
import com.ydl.bannerlib.view.CustomBannerView
import com.ydl.bannerview.R
import com.ydl.bannerview.util.ImageBitmapUtils
import kotlinx.android.synthetic.main.activity_five.*

class BannerActivity5 :AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_five)
        initBanner()
    }

    private val imgs = intArrayOf(
        R.drawable.icon111,
        R.drawable.icon222,
        R.drawable.icon333,
        R.drawable.icon444,
        R.drawable.icon555
//        R.drawable.icon6666
    )

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        if (banner6 != null) {
            banner6.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (banner6 != null) {
            banner6.resume()
        }
    }

    private fun initBanner() {
        banner6.setAnimationDuration(1000)
        banner6.setHintGravity(1)
        banner6.setPlayDelay(2000)
        banner6.setHintView(IconHintView(this, R.drawable.point_focus, R.drawable.point_normal))
        banner6.setAdapter(ImageNormalAdapter())
        banner6.setOnBannerClickListener(object : CustomBannerView.OnBannerClickListener {
            override fun onItemClick(position: Int) {
                Toast.makeText(this@BannerActivity5, position.toString() + "被点击呢", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }


    private inner class ImageNormalAdapter : StaticPagerAdapter() {
        override fun getView(container: ViewGroup?, position: Int): View {
            val view = ImageView(container?.context)
            view.scaleType = ImageView.ScaleType.CENTER_CROP
            view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            val bitmap = BitmapFactory.decodeResource(resources, imgs[position])
            val bitmap1: Bitmap? = ImageBitmapUtils.compressByScale(bitmap, 2.0f, 2.0f, false)
            view.setImageBitmap(bitmap1!!)
            return view
        }

        override fun getCount(): Int = imgs.size
    }
}