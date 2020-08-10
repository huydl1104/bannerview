package com.ydl.bannerview.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ydl.bannerlib.gallery.GalleryLayoutManager
import com.ydl.bannerlib.gallery.GalleryRecyclerView
import com.ydl.bannerlib.gallery.GalleryScaleTransformer
import com.ydl.bannerview.R
import com.ydl.bannerview.adapter.Snap3Adapter
import com.ydl.bannerview.util.CustomBlur
import kotlinx.android.synthetic.main.activity_seven.*
import java.util.*

class BannerActivity7 :AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seven)
        initRecyclerView()
        initRecyclerView2()
    }

    private fun initRecyclerView() {
        val adapter = Snap3Adapter(this)
        adapter.setData(getData())
        recyclerView.setDelayTime(3000)
            .setFlingSpeed(10000)
            .setDataAdapter(adapter)
            .setSelectedPosition(100)
            .setCallbackInFling(false)
            .setOnItemSelectedListener(object : GalleryRecyclerView.OnItemSelectedListener {
                override fun onItemSelected(recyclerView: RecyclerView?, item: View?, position: Int) {
                    Log.e("onItemSelected-----", position.toString() + "")
                    //设置高斯模糊背景
                    setBlurImage(true)
                }
            })
            .setSize(adapter.data!!.size)
            .setUp()
    }


    private fun initRecyclerView2() {
        val manager = GalleryLayoutManager(this, LinearLayoutManager.HORIZONTAL)
        manager.attach(recyclerView2, 100)
        manager.setItemTransformer(GalleryScaleTransformer(0.2f, 30))
        recyclerView2.layoutManager = manager
        val adapter = Snap3Adapter(this)
        adapter.setData(getData())
        recyclerView2.adapter = adapter
    }

    private var data = ArrayList<Int>()
    private fun getData(): ArrayList<Int> {
        val bannerImage = resources.obtainTypedArray(R.array.banner_image)
        for (i in 0..11) {
            val image = bannerImage.getResourceId(i, R.drawable.beauty1)
            data.add(image)
        }
        bannerImage.recycle()
        return data
    }


    /**
     * 获取虚化背景的位置
     */
    private var mLastDraPosition = -1
    private val mTSDraCacheMap: MutableMap<String, Drawable> = HashMap()
    private val KEY_PRE_DRAW = "key_pre_draw"
    /**
     * 设置背景高斯模糊
     */
    fun setBlurImage(forceUpdate: Boolean) {
        val adapter=  recyclerView.adapter as Snap3Adapter
        val mCurViewPosition: Int = recyclerView.getCurrentItem()
        val isSamePosAndNotUpdate =
            mCurViewPosition == mLastDraPosition && !forceUpdate
        if (recyclerView == null || isSamePosAndNotUpdate) {
            return
        }
        recyclerView.post {
            // 获取当前位置的图片资源ID
            val resourceId: Int = adapter.data!![mCurViewPosition % adapter.data!!.size]
            // 将该资源图片转为Bitmap
            val resBmp = BitmapFactory.decodeResource(resources, resourceId)
            // 将该Bitmap高斯模糊后返回到resBlurBmp
            val resBlurBmp: Bitmap? = CustomBlur.apply(recyclerView.context, resBmp, 10)
            // 再将resBlurBmp转为Drawable
            val resBlurDrawable: Drawable = BitmapDrawable(resBlurBmp!!)
            // 获取前一页的Drawable
            val preBlurDrawable =
                if (mTSDraCacheMap[KEY_PRE_DRAW] == null) resBlurDrawable else mTSDraCacheMap[KEY_PRE_DRAW]!!
            /* 以下为淡入淡出效果 */
            val drawableArr =
                arrayOf(preBlurDrawable, resBlurDrawable)
            val transitionDrawable = TransitionDrawable(drawableArr)
            fl_container.setBackgroundDrawable(transitionDrawable)
            transitionDrawable.startTransition(500)
            // 存入到cache中
            mTSDraCacheMap[KEY_PRE_DRAW] = resBlurDrawable
            // 记录上一次高斯模糊的位置
            mLastDraPosition = mCurViewPosition
        }
    }

}