package com.ydl.bannerview.activity

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ydl.bannerlib.adapter.LoopPagerAdapter
import com.ydl.bannerlib.view.CustomBannerView
import com.ydl.bannerlib.view.CustomBannerView.OnBannerClickListener
import com.ydl.bannerlib.view.CustomBannerView.OnPageListener
import com.ydl.bannerview.R
import com.ydl.bannerview.util.ImageBitmapUtils
import kotlinx.android.synthetic.main.activity_first.*

class BannerActivity1 : AppCompatActivity() {

    private val imgs = intArrayOf(
        R.drawable.icon111,
        R.drawable.icon222,
        R.drawable.icon333,
        R.drawable.icon444,
        R.drawable.icon555,
        R.drawable.icon6666
    )

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        if (banner != null) {
            banner!!.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (banner != null) { //开始轮播
            banner!!.resume()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)
        initBanner()
    }

    private fun initBanner() {
        //设置轮播时间
        banner!!.setPlayDelay(1000)
        //设置轮播图适配器，必须
        banner!!.setAdapter(ImageNormalAdapter(banner))
        //设置位置
        banner!!.setHintGravity(1)
        //判断轮播是否进行
        val playing = banner!!.isPlaying
        //轮播图点击事件
        banner!!.setOnBannerClickListener(object : OnBannerClickListener {
            override fun onItemClick(position: Int) {
                Toast.makeText(this@BannerActivity1, position.toString() + "被点击呢", Toast.LENGTH_SHORT).show()
            }
        })
        //轮播图滑动事件
        banner!!.setOnPageListener(object : OnPageListener {
            override fun onPageChange(position: Int) {}
        })
    }

    private inner class ImageNormalAdapter(viewPager: CustomBannerView) :
        LoopPagerAdapter(viewPager) {
        override fun getView(container: ViewGroup?, position: Int): View {
            val view = ImageView(container!!.context)
            view.scaleType = ImageView.ScaleType.FIT_XY
            view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            val bitmap = BitmapFactory.decodeResource(resources, imgs[position])
            val bitmap1 = ImageBitmapUtils.compressByQuality(bitmap, 50, false)
            view.setImageBitmap(bitmap1)
            return view
        }

        override val realCount: Int
            get() = imgs.size
    }
}