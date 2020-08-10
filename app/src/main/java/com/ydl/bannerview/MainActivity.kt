package com.ydl.bannerview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.ydl.banner3view.activity.BannerActivity3
import com.ydl.bannerview.activity.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initListener()
    }

    private fun initListener() {
        button1.setOnClickListener(this)
        button2.setOnClickListener(this)
        button3.setOnClickListener(this)
        button4.setOnClickListener(this)
        button5.setOnClickListener(this)
        button6.setOnClickListener(this)
        button7.setOnClickListener(this)
        button8.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button1 -> {
                startActivity(Intent(this, BannerActivity1::class.java))
            }
            R.id.button2 -> {
                startActivity(Intent(this, BannerActivity2::class.java))
            }
            R.id.button3 -> {
                startActivity(Intent(this, BannerActivity3::class.java))
            }
            R.id.button4 -> {
                startActivity(Intent(this, BannerActivity4::class.java))
            }
            R.id.button5 -> {
                startActivity(Intent(this, SplashActivity::class.java))
            }
            R.id.button6 -> {
                startActivity(Intent(this, BannerActivity5::class.java))
            }
            R.id.button7 -> {
                startActivity(Intent(this, BannerActivity6::class.java))
            }
            R.id.button8 -> {
                startActivity(Intent(this, BannerActivity7::class.java))
            }
        }

    }
}
