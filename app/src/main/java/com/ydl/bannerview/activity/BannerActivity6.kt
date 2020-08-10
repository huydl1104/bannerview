package com.ydl.bannerview.activity

import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.ydl.bannerlib.snap.ScrollLinearHelper
import com.ydl.bannerlib.snap.ScrollPageHelper
import com.ydl.bannerlib.snap.ScrollSnapHelper
import com.ydl.bannerview.R
import com.ydl.bannerview.adapter.SnapAdapter
import kotlinx.android.synthetic.main.activity_six.*
import java.util.*

class BannerActivity6 :AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_six)
        initRecyclerView()
        initRecyclerView2()
        initRecyclerView3()
        initRecyclerView4()

    }
    private fun initRecyclerView() {
        val manager = LinearLayoutManager(this)
        manager.setOrientation(LinearLayoutManager.HORIZONTAL)
        recyclerView1.setLayoutManager(manager)
        val snapHelper = ScrollLinearHelper()
        snapHelper.attachToRecyclerView(recyclerView1)
        val adapter = SnapAdapter()
        recyclerView1.setAdapter(adapter)
        adapter.setData(getData())
    }

    private fun initRecyclerView2() {
        val manager = LinearLayoutManager(this)
        manager.setOrientation(LinearLayoutManager.HORIZONTAL)
        recyclerView2.setLayoutManager(manager)
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView2)
        val adapter = SnapAdapter()
        recyclerView2.setAdapter(adapter)
        adapter.setData(getData())
    }


    private fun initRecyclerView3() {
        val manager = LinearLayoutManager(this)
        manager.setOrientation(LinearLayoutManager.HORIZONTAL)
        recyclerView3.setLayoutManager(manager)
        val snapHelper = ScrollPageHelper(Gravity.START, false)
        snapHelper.attachToRecyclerView(recyclerView3)
        val adapter = SnapAdapter()
        recyclerView3.setAdapter(adapter)
        adapter.setData(getData())
    }


    private fun initRecyclerView4() {
        val manager = LinearLayoutManager(this)
        manager.setOrientation(LinearLayoutManager.HORIZONTAL)
        recyclerView4.setLayoutManager(manager)
        val snapHelper = ScrollSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView4)
        val adapter = SnapAdapter()
        recyclerView4.setAdapter(adapter)
        adapter.setData(getData())
    }

    private val data = ArrayList<String>()
    private fun getData(): ArrayList<String> {
        for (a in 0..19) {
            data.add("测试数据$a")
        }
        return data
    }
}
