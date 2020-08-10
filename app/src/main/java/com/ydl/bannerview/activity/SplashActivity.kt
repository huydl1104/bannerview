package com.ydl.bannerview.activity

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ydl.bannerlib.adapter.DynamicPagerAdapter
import com.ydl.bannerlib.view.CustomBannerView
import com.ydl.bannerview.R
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity :AppCompatActivity() {


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
        setContentView(R.layout.activity_splash)

        banner5.setAdapter(ImageNormalAdapter())
        banner5.setPlayDelay(0)
        banner5.setHintGravity(1)
        banner5.setOnBannerClickListener(object : CustomBannerView.OnBannerClickListener {
            override fun onItemClick(position: Int) {}
        })
        banner5.setOnPageListener(object : CustomBannerView.OnPageListener {
            override fun onPageChange(position: Int) {
                if (position == imgs.size - 1) {
                    btn_splash.setVisibility(View.VISIBLE)
                } else {
                    btn_splash.setVisibility(View.GONE)
                }
            }
        })
        btn_splash.setOnClickListener(View.OnClickListener {
           Toast.makeText(this@SplashActivity,"跳转到 主界面 中 ",Toast.LENGTH_SHORT).show()
        })
    }

    private inner class ImageNormalAdapter : DynamicPagerAdapter() {
        override fun getView(container: ViewGroup?, position: Int): View {
            val view = ImageView(container?.context)
            view.scaleType = ImageView.ScaleType.CENTER_CROP
            view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            view.setImageResource(imgs[position])
            return view
        }

       override fun getCount(): Int = imgs.size
    }
}